/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.download

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import hoodles.morphe.patches.soundcloud.shared.Constants
import hoodles.morphe.patches.soundcloud.shared.soundcloudCorePatch

private const val DOWNLOAD_HOOK = "Lapp/morphe/extension/soundcloud/DownloadButtonHook;"

private fun injectReturnVoidShare(
    fingerprint: Fingerprint,
    keyRegister: Int,
    contextMatchIndex: Int,
    extensionMethod: String
) {
    val contextCall = fingerprint.instructionMatches[contextMatchIndex]
    val moveResultIndex = contextCall.index + 1
    val contextRegister =
        fingerprint.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
    val continuationTarget = fingerprint.method.getInstruction<Instruction>(moveResultIndex + 1)

    fingerprint.method.addInstructionsWithLabels(
        moveResultIndex + 1,
        """
            invoke-static {v$contextRegister, v$keyRegister}, $DOWNLOAD_HOOK->$extensionMethod
            move-result v$contextRegister
            if-eqz v$contextRegister, :morphe_continue
            return-void
        """.trimIndent(),
        ExternalLabel("morphe_continue", continuationTarget)
    )
}

private fun injectReturnUnitShare(
    fingerprint: Fingerprint,
    keyMatchIndex: Int,
    contextMatchIndex: Int,
    extensionMethod: String
) {
    val keyCall = fingerprint.instructionMatches[keyMatchIndex]
    val keyRegister =
        fingerprint.method.getInstruction<OneRegisterInstruction>(keyCall.index + 1).registerA
    val contextCall = fingerprint.instructionMatches[contextMatchIndex]
    val fragmentRegister = (contextCall.instruction as FiveRegisterInstruction).registerC
    val moveResultIndex = contextCall.index + 1
    val contextRegister =
        fingerprint.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
    val continuationTarget = fingerprint.method.getInstruction<Instruction>(moveResultIndex + 1)

    fingerprint.method.addInstructionsWithLabels(
        moveResultIndex + 1,
        """
            invoke-static {v$contextRegister, v$keyRegister}, $DOWNLOAD_HOOK->$extensionMethod
            move-result v$contextRegister
            if-eqz v$contextRegister, :morphe_continue
            invoke-virtual {v$fragmentRegister}, Landroidx/fragment/app/DialogFragment;->dismissAllowingStateLoss()V
            sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
            return-object v0
        """.trimIndent(),
        ExternalLabel("morphe_continue", continuationTarget)
    )
}

private fun injectTrackMenuShare(
    fingerprint: Fingerprint,
    urlFieldMatchIndex: Int,
    contextMatchIndex: Int
) {
    val urlRegister =
        (fingerprint.instructionMatches[urlFieldMatchIndex].instruction as TwoRegisterInstruction).registerA
    val contextCall = fingerprint.instructionMatches[contextMatchIndex]
    val fragmentRegister = (contextCall.instruction as FiveRegisterInstruction).registerC
    val moveResultIndex = contextCall.index + 1
    val contextRegister =
        fingerprint.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
    val continuationTarget = fingerprint.method.getInstruction<Instruction>(moveResultIndex + 1)

    fingerprint.method.addInstructionsWithLabels(
        moveResultIndex + 1,
        """
            invoke-static {v$contextRegister, v$urlRegister}, $DOWNLOAD_HOOK->tryShare(Landroid/content/Context;Ljava/lang/String;)Z
            move-result v$contextRegister
            if-eqz v$contextRegister, :morphe_continue
            invoke-virtual {v$fragmentRegister}, Landroidx/fragment/app/DialogFragment;->dismissAllowingStateLoss()V
            sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
            return-object v0
        """.trimIndent(),
        ExternalLabel("morphe_continue", continuationTarget)
    )
}

private fun patchMenuSwitch(fingerprint: Fingerprint, addStringField: String) {
    val match = fingerprint.instructionMatches[0]
    val iconRegister = (match.instruction as OneRegisterInstruction).registerA
    val nameRegister =
        fingerprint.method.getInstruction<OneRegisterInstruction>(match.index - 1).registerA
    val scratchRegister = 2

    require(scratchRegister != nameRegister && scratchRegister != iconRegister) {
        "Morphe menu hook scratch register v2 collided with target registers"
    }

    val continuationTarget = fingerprint.method.getInstruction<Instruction>(match.index + 1)
    fingerprint.method.addInstructionsWithLabels(
        match.index + 1,
        """
            invoke-static {}, $DOWNLOAD_HOOK->isEnabled()Z
            move-result v$scratchRegister
            if-eqz v$scratchRegister, :morphe_native
            sget v$nameRegister, $addStringField
            sget v$iconRegister, Lcom/soundcloud/android/ui/components/R\$drawable;->ic_actions_download_initial:I
        """.trimIndent(),
        ExternalLabel("morphe_native", continuationTarget)
    )
}

val soundcloudDownloadHookPatch = bytecodePatch(
    name = "SoundCloud custom download routing",
    description = "Routes track and playlist download actions to an external downloader package."
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(soundcloudCorePatch)

    execute {
        // Hide the native offline-sync setting while the external download hook is enabled.
        OfflineSettingsFingerprint.let { fingerprint ->
            val match = fingerprint.instructionMatches[0]
            val visibilityRegister = (match.instruction as TwoRegisterInstruction).registerA
            fingerprint.method.addInstructions(
                match.index + 1,
                """
                    invoke-static {v$visibilityRegister}, $DOWNLOAD_HOOK->filterNativeDownloadUiVisibility(Z)Z
                    move-result v$visibilityRegister
                """.trimIndent()
            )
        }

        // Remember the current track URL by TrackUrn.
        TrackRememberFingerprint.let { fingerprint ->
            val keyRegister =
                (fingerprint.instructionMatches[0].instruction as TwoRegisterInstruction).registerA
            val urlCall = fingerprint.instructionMatches[1]
            val urlRegister =
                fingerprint.method.getInstruction<OneRegisterInstruction>(urlCall.index + 1).registerA
            fingerprint.method.addInstructions(
                urlCall.index + 2,
                "invoke-static {v$keyRegister, v$urlRegister}, $DOWNLOAD_HOOK->rememberUrl(Ljava/lang/Object;Ljava/lang/String;)V"
            )
        }

        // Remember the current playlist URL by PlaylistUrn.
        PlaylistRememberFingerprint.let { fingerprint ->
            val urnCall = fingerprint.instructionMatches[0]
            val urlCall = fingerprint.instructionMatches[1]
            val urnRegister =
                fingerprint.method.getInstruction<OneRegisterInstruction>(urnCall.index + 1).registerA
            val urlRegister =
                fingerprint.method.getInstruction<OneRegisterInstruction>(urlCall.index + 1).registerA
            fingerprint.method.addInstructions(
                urlCall.index + 2,
                "invoke-static {v$urnRegister, v$urlRegister}, $DOWNLOAD_HOOK->rememberUrl(Ljava/lang/Object;Ljava/lang/String;)V"
            )
        }

        // Track page download click.
        TrackClickFingerprint.let { fingerprint ->
            val urnRegister =
                (fingerprint.instructionMatches[0].instruction as TwoRegisterInstruction).registerA
            injectReturnVoidShare(
                fingerprint,
                urnRegister,
                1,
                "tryShareRemembered(Landroid/content/Context;Ljava/lang/Object;)Z"
            )
        }

        // Normalize native track and playlist download state to the Morphe action state.
        listOf(TrackStateFingerprint, PlaylistStateFingerprint).forEach { fingerprint ->
            val match = fingerprint.instructionMatches[0]
            val stateRegister = (match.instruction as FiveRegisterInstruction).registerD
            fingerprint.method.addInstructions(
                match.index,
                """
                    invoke-static {v$stateRegister}, $DOWNLOAD_HOOK->normalizeDownloadState(Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$State;)Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$State;
                    move-result-object v$stateRegister
                """.trimIndent()
            )
        }

        // Turn downloaded menu items back into download actions while the external hook is active.
        patchMenuSwitch(
            TrackMenuSwitchFingerprint,
            "Lcom/soundcloud/android/features/bottomsheet/track/R\$string;->menu_add_to_downloads:I"
        )
        patchMenuSwitch(
            PlaylistMenuSwitchFingerprint,
            "Lcom/soundcloud/android/features/bottomsheet/playlist/R\$string;->download_playlist:I"
        )

        // Library Downloads row visibility.
        LibraryDownloadsFingerprint.let { fingerprint ->
            val match = fingerprint.instructionMatches[1]
            val viewRegister = (match.instruction as TwoRegisterInstruction).registerA
            fingerprint.method.addInstructions(
                match.index + 1,
                "invoke-static {v$viewRegister}, $DOWNLOAD_HOOK->applyLibraryDownloadsVisibility(Landroid/view/View;)V"
            )
        }

        // Patch the later playlist-bottom-sheet branch first to preserve original match indexes.
        PlaylistBottomSheetClicksFingerprint.let { fingerprint ->
            injectReturnUnitShare(
                fingerprint,
                2,
                3,
                "tryShareRemembered(Landroid/content/Context;Ljava/lang/Object;)Z"
            )
            injectReturnUnitShare(
                fingerprint,
                0,
                1,
                "tryShareRemembered(Landroid/content/Context;Ljava/lang/Object;)Z"
            )
        }

        // Track bottom-sheet actions, anchored on the URL field and requireContext calls.
        TrackBottomSheetClicksFingerprint.let { fingerprint ->
            injectTrackMenuShare(fingerprint, 2, 3)
            injectTrackMenuShare(fingerprint, 0, 1)
        }

        // Playlist detail-page download click.
        PlaylistDetailsClickFingerprint.let { fingerprint ->
            val urlCall = fingerprint.instructionMatches[0]
            val urlRegister =
                fingerprint.method.getInstruction<OneRegisterInstruction>(urlCall.index + 1).registerA
            val contextCall = fingerprint.instructionMatches[1]
            val moveResultIndex = contextCall.index + 1
            val contextRegister =
                fingerprint.method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            val continuationTarget =
                fingerprint.method.getInstruction<Instruction>(moveResultIndex + 1)

            fingerprint.method.addInstructionsWithLabels(
                moveResultIndex + 1,
                """
                    invoke-static {v$contextRegister, v$urlRegister}, $DOWNLOAD_HOOK->tryShare(Landroid/content/Context;Ljava/lang/String;)Z
                    move-result v$contextRegister
                    if-eqz v$contextRegister, :morphe_continue
                    return-void
                """.trimIndent(),
                ExternalLabel("morphe_continue", continuationTarget)
            )
        }
    }
}
