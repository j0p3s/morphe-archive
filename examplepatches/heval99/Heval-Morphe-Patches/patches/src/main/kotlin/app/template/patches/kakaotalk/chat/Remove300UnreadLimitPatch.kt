// Ported from AmpleReVanced/revanced-patches (GPL-3.0), layout/chatroom/Remove300PlusLimitPatch.kt,
// written for KakaoTalk 26.7.2. Changes for 26.8.3: only the "count < 300" branch is turned into a
// goto (Ample turned every if-lt in the method), and every edit checks the exact clamp pattern.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.getReference
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction10t
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction22t
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val UNREAD_LIMIT = 300

@Suppress("unused")
val remove300UnreadLimitPatch = bytecodePatch(
    name = "Remove 300+ unread limit",
    description = "Shows the real unread message count in the chat list instead of \"300+\".",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. The regular and OpenChat room list items always take the "print the number" branch.
        Limit300PlusBaseChatRoomFingerprint.method.alwaysPrintUnreadCount()
        Limit300PlusOpenChatRoomFingerprint.method.alwaysPrintUnreadCount()

        // 2. The room's unread count getter no longer clamps with Math.min(count, 300):
        // const/16 vL, 300; invoke-static {vC, vL}, Math.min; move-result vC are removed.
        GetUnreadCountFingerprint.method.apply {
            val clampIndex = instructions.indexOfFirst { it.isConst16(UNREAD_LIMIT) }
            val min = instructions.getOrNull(clampIndex + 1)?.getReference<MethodReference>()
            if (clampIndex < 0 ||
                min?.definingClass != "Ljava/lang/Math;" || min.name != "min" ||
                instructions.getOrNull(clampIndex + 2)?.opcode != Opcode.MOVE_RESULT
            ) {
                throw PatchException("No Math.min(count, 300) clamp in $definingClass->$name")
            }
            removeInstructions(clampIndex, 3)
        }
    }
}

internal fun Instruction.isConst16(value: Int) =
    opcode == Opcode.CONST_16 && (this as NarrowLiteralInstruction).narrowLiteral == value

// Replaces "if-lt count, limit" (right after const/16 limit, 300) with a goto to its target.
private fun MutableMethod.alwaysPrintUnreadCount() {
    val index = instructions.indices.firstOrNull { i ->
        val limit = instructions[i]
        val branch = instructions.getOrNull(i + 1)
        limit.isConst16(UNREAD_LIMIT) && branch is BuilderInstruction22t && branch.opcode == Opcode.IF_LT &&
            branch.registerB == (limit as OneRegisterInstruction).registerA
    }?.plus(1) ?: throw PatchException("No 'count < 300' branch in $definingClass->$name")
    val branch = instructions[index] as BuilderInstruction22t
    replaceInstruction(index, BuilderInstruction10t(Opcode.GOTO, branch.target))
}
