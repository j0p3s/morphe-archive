package app.template.patches.firefoxfocus.misc.shared

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.template.patches.shared.addInstructionsChecked
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Focus is built with -dontobfuscate, so its classes and Kotlin property delegate fields keep their
// source names. R8 still inlines the property getters: callers read the delegate field and call
// getValue on it themselves.
private const val FOCUS_PACKAGE = "Lorg/mozilla/focus/"
private const val BOOLEAN_FALSE = "Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;"

// How far after the iget-object the delegate's getValue call may sit (the onCreate read loads the
// KProperty array element first).
private const val GET_VALUE_WINDOW = 8

/** Every register an instruction names, read or written. */
internal fun Instruction.registers(): List<Int> = when (this) {
    is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
    is FiveRegisterInstruction -> listOf(registerC, registerD, registerE, registerF, registerG).take(registerCount)
    is ThreeRegisterInstruction -> listOf(registerA, registerB, registerC)
    is TwoRegisterInstruction -> listOf(registerA, registerB)
    is OneRegisterInstruction -> listOf(registerA)
    else -> emptyList()
}

private fun Instruction.readsField(definingClass: String, name: String): Boolean {
    if (opcode != Opcode.IGET_OBJECT) return false
    val field = (this as ReferenceInstruction).reference as FieldReference
    return field.definingClass == definingClass && field.name == name
}

/**
 * Makes every read of the boolean Kotlin property delegate [definingClass]->[fieldName] in the app's
 * own code answer false: iget-object of the delegate, the first call after it (which must be
 * getValue on that register) and its move-result-object, after which Boolean.FALSE is loaded into
 * the result register. Throws if there is no read at all.
 */
internal fun BytecodePatchContext.forceDelegateReadsFalse(definingClass: String, fieldName: String) {
    var changed = 0
    classDefForEach { classDef ->
        if (!classDef.type.startsWith(FOCUS_PACKAGE)) return@classDefForEach
        val reads = classDef.methods.any { method ->
            method.implementation?.instructions?.any { it.readsField(definingClass, fieldName) } == true
        }
        if (!reads) return@classDefForEach

        mutableClassDefBy(classDef).methods.filter { it.implementation != null }.forEach { method ->
            val label = "${method.definingClass}->${method.name}"
            val sites = method.instructions.indices.filter { method.instructions[it].readsField(definingClass, fieldName) }
            // From the last read back, so earlier indices stay valid after each insert.
            for (read in sites.reversed()) {
                val instructions = method.instructions
                val delegate = (instructions[read] as TwoRegisterInstruction).registerA
                val callIndex = (read + 1..minOf(read + GET_VALUE_WINDOW, instructions.lastIndex)).firstOrNull {
                    (instructions[it] as? ReferenceInstruction)?.reference is MethodReference
                } ?: throw PatchException("$label: no call after the $fieldName read")
                val call = instructions[callIndex]
                val reference = (call as ReferenceInstruction).reference as MethodReference
                if (reference.name != "getValue" || (call as? FiveRegisterInstruction)?.registerC != delegate) {
                    throw PatchException("$label: the first call after the $fieldName read is $reference, not its getValue")
                }
                val moveResult = instructions.getOrNull(callIndex + 1)
                if (moveResult?.opcode != Opcode.MOVE_RESULT_OBJECT) {
                    throw PatchException("$label: $fieldName getValue is not followed by move-result-object")
                }
                // An insert keeps branch labels on the instruction it pushes down, so a jump to the
                // instruction after the move-result would skip the forced value.
                if (instructions.getOrNull(callIndex + 2)?.location?.labels?.isNotEmpty() != false) {
                    throw PatchException("$label: the instruction after the $fieldName read is a branch target")
                }
                val register = (moveResult as OneRegisterInstruction).registerA
                method.addInstructionsChecked(callIndex + 2, "sget-object v$register, $BOOLEAN_FALSE")
                changed++
            }
        }
    }
    if (changed == 0) throw PatchException("No read of $definingClass->$fieldName found")
}
