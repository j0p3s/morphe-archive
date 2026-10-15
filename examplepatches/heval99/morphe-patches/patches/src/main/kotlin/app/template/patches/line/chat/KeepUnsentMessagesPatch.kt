// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/keepunsent/KeepUnsentMessagesPatch.kt:
// the guard, row, message id and database discovery by instruction shape, and the placeholder
// insert. Always on when selected (no settings toggle); the extension lives in extensions/line.

package app.template.patches.line.chat

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.util.getReference
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

private const val SQLITE_DATABASE = "Landroid/database/sqlite/SQLiteDatabase;"
private const val KEEP_UNSENT_MESSAGES = "Lapp/template/extension/line/KeepUnsentMessages;"

/** Highest register the 4-bit operands of invoke (35c) and iget (22c) can address. */
private const val MAX_NIBBLE_REGISTER = 15

@Suppress("unused")
val keepUnsentMessagesPatch = bytecodePatch(
    name = "Keep unsent messages",
    description = "Keeps messages that others unsend in 1:1 and group chats, with LINE's " +
        "\"unsent a message\" notice below each one. OpenChat is not covered.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    extendWith("extensions/line.mpe")

    // The unsend lambda rewrites the row to an UNSENT type and deletes its content, search entry,
    // reactions and image mapping, all behind one guard:
    //
    //     invoke-virtual {vG}, <message type>->isUnsentMessage()Z   # already a tombstone?
    //     move-result vG
    //     if-eqz vG, :destroy                                       # no -> run the unsend
    //     goto/16 :return_row                                       # yes -> keep the row
    //
    // Forcing vG to 1 keeps the message intact. LINE then never shows that it was recalled, so the
    // extension inserts LINE's own UNSENT row right below it.
    execute {
        UnsendMessageDbWriteFingerprint.method.apply {
            // 1. The guard: a no-argument boolean call whose result is branched on at once, with
            //    an unconditional jump as the fall-through. No other call here has that shape.
            val guardIndex = instructions.indices.firstOrNull { index ->
                val call = instructions[index].getReference<MethodReference>()
                instructions[index].opcode == Opcode.INVOKE_VIRTUAL &&
                    call?.returnType == "Z" && call.parameterTypes.isEmpty() &&
                    instructions.getOrNull(index + 1)?.opcode == Opcode.MOVE_RESULT &&
                    instructions.getOrNull(index + 2)?.opcode == Opcode.IF_EQZ &&
                    instructions.getOrNull(index + 3)?.opcode in setOf(Opcode.GOTO, Opcode.GOTO_16, Opcode.GOTO_32)
            } ?: throw PatchException("Already-unsent guard not found in $definingClass")
            val guardRegister = (instructions[guardIndex + 1] as OneRegisterInstruction).registerA

            // 2. The fetched row: the object the guard's receiver was read from.
            val guardReceiver = (instructions[guardIndex] as FiveRegisterInstruction).registerC
            val receiverRead = (guardIndex - 1 downTo 0).firstOrNull { index ->
                instructions[index].opcode == Opcode.IGET_OBJECT &&
                    (instructions[index] as TwoRegisterInstruction).registerA == guardReceiver
            } ?: throw PatchException("Guard receiver read not found in $definingClass")
            val rowRegister = (instructions[receiverRead] as TwoRegisterInstruction).registerB

            // 3. The message id: a long read off that row between the row read and the guard.
            //    Local or server id both work; the extension's WHERE clause accepts either.
            val messageIdRegister = (receiverRead + 1 until guardIndex).firstOrNull { index ->
                instructions[index].opcode == Opcode.IGET_WIDE &&
                    (instructions[index] as TwoRegisterInstruction).registerB == rowRegister
            }?.let { (instructions[it] as TwoRegisterInstruction).registerA }
                ?: throw PatchException("Message id read not found in $definingClass")

            // 4. The transaction's SQLiteDatabase: the first such field read whose holder register
            //    was check-cast to the field's class before the guard, so it still holds that
            //    object where we inject.
            val castTypes = (0 until guardIndex).mapNotNull { index ->
                val instruction = instructions[index]
                if (instruction.opcode != Opcode.CHECK_CAST) return@mapNotNull null
                val type = instruction.getReference<TypeReference>()?.type ?: return@mapNotNull null
                (instruction as OneRegisterInstruction).registerA to type
            }.toMap()
            val (databaseField, holderRegister) = instructions.indices.firstNotNullOfOrNull { index ->
                val field = instructions[index].getReference<FieldReference>()
                if (instructions[index].opcode != Opcode.IGET_OBJECT || field?.type != SQLITE_DATABASE) {
                    return@firstNotNullOfOrNull null
                }
                val holder = (instructions[index] as TwoRegisterInstruction).registerB
                if (castTypes[holder] != field.definingClass) null else field to holder
            } ?: throw PatchException("SQLiteDatabase field read not found in $definingClass")

            val highest = maxOf(guardRegister, holderRegister, messageIdRegister + 1)
            if (highest > MAX_NIBBLE_REGISTER) {
                throw PatchException("Unsend registers spilled past v$MAX_NIBBLE_REGISTER (v$highest) in $definingClass")
            }

            // 5. Before the if-eqz: a row that already is a tombstone keeps the stock path. Any
            //    other row gets the notice row, then the guard reads 1 and the unsend is skipped.
            //    The guard register is free here: the skipped block writes it before reading it.
            addInstructionsWithLabels(
                guardIndex + 2,
                """
                    if-nez v$guardRegister, :stock
                    iget-object v$guardRegister, v$holderRegister, ${databaseField.definingClass}->${databaseField.name}:${databaseField.type}
                    invoke-static {v$guardRegister, v$messageIdRegister, v${messageIdRegister + 1}}, $KEEP_UNSENT_MESSAGES->insertPlaceholder(${SQLITE_DATABASE}J)V
                    const/16 v$guardRegister, 0x1
                """,
                ExternalLabel("stock", getInstruction(guardIndex + 2)),
            )
        }
    }
}
