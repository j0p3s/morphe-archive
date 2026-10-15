package app.morphe.patches.platform8tv.user

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.platform8tv.extension.sharedExtensionPatch
import app.morphe.patches.platform8tv.shared.Constants.COMPATIBILITY_PLATFORM8TV
import app.morphe.patches.platform8tv.shared.morphe.digivive.platform8.androidtv.application.NexgTvApplication
import app.morphe.util.matchSingle
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference

private const val EXTENSION_CLASS = "Lapp/morphe/extension/platform8/patches/AnonymousUserPatch;"

@Suppress("unused")
val anonymousUserPatch =
    bytecodePatch(
        name = "Anonymous user",
        description = "Log in as anonymous user.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_PLATFORM8TV)

        dependsOn(sharedExtensionPatch)

        execute {
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
