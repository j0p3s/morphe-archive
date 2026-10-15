package app.morphe.patches.platform8.content

import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.platform8.shared.Constants.COMPATIBILITY_PLATFORM8
import app.morphe.util.matchAllMethodIndicesForEach
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_PLATFORM8)

        execute {
            ContentAvailabilityFieldAccessFingerprint.matchAllMethodIndicesForEach {
                val instruction = getInstruction<TwoRegisterInstruction>(it)

                replaceInstruction(
                    it,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instruction.registerA,
                        ImmutableStringReference("FREE"),
                    ),
                )
            }
        }
    }
