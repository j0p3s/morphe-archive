package app.morphe.patches.bongobdandroidtv.update

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.bongobdandroidtv.shared.Constants.COMPATIBILITY_BONGOANDROIDTV
import app.morphe.patches.shared.extension.kotlin.toInt
import app.morphe.util.matchAllMethodIndicesForEach
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction11n
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

@Suppress("unused")
val disableUpdatePatch = bytecodePatch(
    name = "Disable update",
    description = "Disables forced update.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_BONGOANDROIDTV)

    execute {
        ForceUpdateFieldAccessFingerprint.matchAllMethodIndicesForEach {
            val instruction = getInstruction<TwoRegisterInstruction>(it)

            replaceInstruction(
                it, BuilderInstruction11n(
                    Opcode.CONST_4,
                    instruction.registerA,
                    false.toInt(),
                )
            )
        }
    }
}