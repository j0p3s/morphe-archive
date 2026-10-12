package app.morphe.patches.hoichoi.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.extension.SHARED_UTILS_EXTENSION_CLASS
import app.morphe.patches.hoichoi.extension.hooks.hoichoiActivityOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoiApplicationOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoitvActivityOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoitvApplicationOnCreateHook
import app.morphe.patches.hoichoi.extension.sharedExtensionPatch
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOI
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOITV
import app.morphe.patches.shared.Type
import app.morphe.patches.shared.morphe.okhttp3.MediaType
import app.morphe.patches.shared.morphe.okhttp3.OkHttpClient
import app.morphe.patches.shared.morphe.okhttp3.ResponseBody
import app.morphe.patches.shared.patches.dex.obfuscateExtensionFieldReferenceFingerprintPatch
import app.morphe.util.matchSingle
import app.morphe.util.p0Register
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction35c
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableMethodReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableTypeReference

private const val EXTENSION_CLASS =
    "Lapp/morphe/extension/hoichoi/interceptors/EntitlementInterceptor;"


@Suppress("unused")
val freeContentPatch = bytecodePatch(
    name = "Free content",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOICHOI, COMPATIBILITY_HOICHOITV)

    dependsOn(
        sharedExtensionPatch, obfuscateExtensionFieldReferenceFingerprintPatch(
            mapOf(
                MediaType.toString() to mapOf("Companion" to MediaTypeCompanionFieldAccessFingerprint),
                ResponseBody.toString() to mapOf("Companion" to ResponseBodyCompanionFieldAccessFingerprint),
            )
        )
    )

    execute {
        GetMonetizationModelFingerprint.matchAll().forEach {
            it.method.returnEarly("FREE")
        }

        IsFreeContentFingerprint.matchAll().forEach {
            it.method.returnEarly(true)
        }

        IsLoginRequiredFingerprint.matchAll().forEach {
            it.method.returnEarly(false)
        }

        OkHttpClient.Builder.BuildFingerprint.matchSingle().method.apply {
            addInstructions(
                0, listOf(
                    BuilderInstruction21c(
                        Opcode.NEW_INSTANCE,
                        0,
                        ImmutableTypeReference(EXTENSION_CLASS),
                    ),
                    BuilderInstruction35c(
                        Opcode.INVOKE_DIRECT,
                        1,
                        0,
                        0,
                        0,
                        0,
                        0,
                        ImmutableMethodReference(
                            EXTENSION_CLASS,
                            "<init>",
                            emptyList(),
                            Type.void,
                        ),
                    ),
                    BuilderInstruction35c(
                        Opcode.INVOKE_VIRTUAL,
                        2,
                        p0Register,
                        0,
                        0,
                        0,
                        0,
                        OkHttpClient.Builder.AddInterceptorFingerprint.matchSingle().originalMethod,
                    ),
                )
            )
        }
    }

    finalize {
        if (COMPATIBILITY_HOICHOI.targets.any { target -> target.version == packageMetadata.versionName }) {
            hoichoiActivityOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
            hoichoiApplicationOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
        } else if (COMPATIBILITY_HOICHOITV.targets.any { target -> target.version == null }) {
            hoichoitvActivityOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
            hoichoitvApplicationOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
        }
    }
}
