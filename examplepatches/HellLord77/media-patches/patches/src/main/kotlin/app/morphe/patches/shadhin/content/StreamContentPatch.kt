package app.morphe.patches.shadhin.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shadhin.shared.Constants.COMPATIBILITY_SHADHIN
import app.morphe.patches.shadhin.shared.data.remote.api.model.mainContentModel.isPaidPatch
import app.morphe.util.matchAllMethodIndicesForEach
import app.morphe.util.matchSingle
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

@Suppress("unused")
val streamContentPatch = bytecodePatch(
    name = "Stream content",
    description = "Stream paid content for free.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SHADHIN)

    dependsOn(isPaidPatch)

    execute {
        FetchStreamingUrlFingerprint.matchSingle().originalMethod.apply {
            val parameterIndex = parameters.indexOfFirst { parameter ->
                parameter.annotations.flatMap { it.elements }
                    .any { it.value.toString() == "\"ContentType\"" }
            }

            Fingerprint(filters = listOf(methodCall(this))).matchAllMethodIndicesForEach {
                val register = getInstruction<FiveRegisterInstruction>(it).run {
                    listOf(registerD, registerE, registerF)[parameterIndex]
                }

                addInstruction(
                    it,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        register,
                        ImmutableStringReference("S"),
                    ),
                )
            }
        }
    }
}