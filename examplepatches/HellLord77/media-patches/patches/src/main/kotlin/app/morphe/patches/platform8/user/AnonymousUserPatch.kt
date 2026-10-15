package app.morphe.patches.platform8.user

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.platform8.extension.sharedExtensionPatch
import app.morphe.patches.platform8.shared.Constants.COMPATIBILITY_PLATFORM8
import app.morphe.patches.platform8.shared.morphe.com.digivive.nexgtv.activities.SplashActivity
import app.morphe.patches.platform8.shared.morphe.com.digivive.nexgtv.application.NexgTvApplication
import app.morphe.util.matchAllMethodIndicesForEach
import app.morphe.util.matchSingle
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference

private const val EXTENSION_CLASS = "Lapp/morphe/extension/platform8/patches/AnonymousUserPatch;"

@Suppress("unused")
val anonymousUserPatch =
    bytecodePatch(
        name = "Anonymous user",
        description = "Log in as anonymous user.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_PLATFORM8)

        dependsOn(sharedExtensionPatch)

        execute {
            IsProfileCompleteFieldAccessFingerprint.matchAllMethodIndicesForEach {
                val instruction = getInstruction<TwoRegisterInstruction>(it)

                replaceInstruction(
                    it,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instruction.registerA,
                        ImmutableStringReference("1"),
                    ),
                )
            }

            IsAdditionalProfileFieldSkippedFieldAccessFingerprint.matchAllMethodIndicesForEach {
                val instruction = getInstruction<TwoRegisterInstruction>(it)

                replaceInstruction(
                    it,
                    BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instruction.registerA,
                        ImmutableStringReference("1"),
                    ),
                )
            }

            CallNextActivityMethodCallFingerprint.matchAllMethodIndicesForEach {
                val instruction = getInstruction<FiveRegisterInstruction>(it)

                replaceInstruction(
                    it,
                    BuilderInstruction35c(
                        Opcode.INVOKE_DIRECT,
                        1,
                        instruction.registerC,
                        0,
                        0,
                        0,
                        0,
                        SplashActivity.GoToMainActivityFingerprint.matchSingle().originalMethod,
                    ),
                )
            }

            NexgTvApplication.OnCreateFingerPrint.matchSingle().method.apply {
                addInstruction(
                    0,
                    BuilderInstruction35c(
                        Opcode.INVOKE_STATIC,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        ImmutableMethodReference(EXTENSION_CLASS, name, parameters, returnType),
                    ),
                )
            }
        }
    }
