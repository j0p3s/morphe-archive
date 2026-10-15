// Ported from AmpleReVanced/revanced-patches (GPL-3.0), interaction/chatlog/Remove99ClampPatch.kt,
// written for KakaoTalk 26.7.2. Changes: the Settings toggle (and its extension) is dropped, so the
// clamp is removed unconditionally; both edits check the exact "const/16 99; if-le" pattern.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private const val READ_COUNT_LIMIT = 99

@Suppress("unused")
val remove99UnreadLimitPatch = bytecodePatch(
    name = "Remove 99+ unread limit",
    description = "Shows the real number of members who have not read a message in large group " +
        "chats instead of capping it at 99.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. Chat log item: "if (count > 99) return 99" returns the count instead.
        ProcessWatermarkCountFingerprint.method.apply {
            val (count, limit, next) = clampAt()
            val capped = instructions[next]
            if (capped.opcode != Opcode.RETURN || (capped as OneRegisterInstruction).registerA != limit) {
                throw PatchException("No 'return 99' after the clamp in $definingClass->$name")
            }
            replaceInstruction(next, "return v$count")
        }

        // 2. Watermark cache: "if (count > 99) count = 99" loses its assignment.
        GetWatermarkCountFromCacheFingerprint.method.apply {
            val (count, limit, next) = clampAt()
            val capped = instructions[next]
            if (capped.opcode != Opcode.MOVE || (capped as TwoRegisterInstruction).registerA != count ||
                capped.registerB != limit
            ) {
                throw PatchException("No 'count = 99' after the clamp in $definingClass->$name")
            }
            removeInstruction(next)
        }
    }
}

// Finds "const/16 vL, 99; if-le vC, vL" and returns (vC, vL, index after the if-le).
private fun MutableMethod.clampAt(): Triple<Int, Int, Int> {
    val index = instructions.indices.firstOrNull { i ->
        val limit = instructions[i]
        val branch = instructions.getOrNull(i + 1)
        limit.isConst16(READ_COUNT_LIMIT) && branch?.opcode == Opcode.IF_LE &&
            (branch as TwoRegisterInstruction).registerB == (limit as OneRegisterInstruction).registerA
    } ?: throw PatchException("No 99 clamp in $definingClass->$name")
    val branch = instructions[index + 1] as TwoRegisterInstruction
    return Triple(branch.registerA, branch.registerB, index + 2)
}
