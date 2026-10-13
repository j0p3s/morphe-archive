/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.ui

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import hoodles.morphe.patches.soundcloud.shared.Constants
import hoodles.morphe.patches.soundcloud.shared.soundcloudCorePatch

private const val UI = "Lapp/morphe/extension/soundcloud/MorpheUiVisibility;"

val soundcloudNavigationUiPatch = bytecodePatch(
    name = "Hide navigation bar elements",
    description = "Allows hiding Cast, Upload, Messages, and Notification Bell icons from the top bar."
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(soundcloudCorePatch)

    execute {
        fun patchMenuVisibility(fingerprint: Fingerprint, extensionMethod: String) {
            val match = fingerprint.instructionMatches[0]
            val visibilityRegister = (match.instruction as FiveRegisterInstruction).registerD
            fingerprint.method.addInstructions(
                match.index,
                """
                    invoke-static {}, $UI->$extensionMethod()Z
                    move-result v$visibilityRegister
                """.trimIndent()
            )
        }

        patchMenuVisibility(TitleBarActivityFeedFingerprint, "shouldShowNotification")
        patchMenuVisibility(TitleBarInboxFingerprint, "shouldShowInbox")
        patchMenuVisibility(TitleBarUploadFingerprint, "shouldShowUpload")
        patchMenuVisibility(CastMenuItemFingerprint, "shouldShowCast")

        CastButtonFingerprint.let { fingerprint ->
            val match = fingerprint.instructionMatches[0]
            val viewRegister = (match.instruction as FiveRegisterInstruction).registerC
            fingerprint.method.addInstructions(
                match.index + 1,
                "invoke-static {v$viewRegister}, $UI->applyCastButtonVisibility(Landroid/view/View;)V"
            )
        }
    }
}
