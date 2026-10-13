/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.settings

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import hoodles.morphe.patches.soundcloud.shared.Constants
import hoodles.morphe.patches.soundcloud.shared.soundcloudCorePatch
import hoodles.morphe.patches.soundcloud.shared.soundcloudSettingsManifestPatch

val soundcloudSettingsPatch = bytecodePatch(
    name = "SoundCloud Morphe Settings",
    description = "Adds Morphe Settings menu directly into SoundCloud settings screen."
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(soundcloudCorePatch, soundcloudSettingsManifestPatch)

    execute {
        val match = SettingsScreenFingerprint.instructionMatches[0]
        val composerRegister = (match.instruction as FiveRegisterInstruction).registerC
        SettingsScreenFingerprint.method.addInstructions(
            match.index + 1,
            "invoke-static {v$composerRegister}, Lapp/morphe/extension/soundcloud/MorpheSettingsCompose;->render(Landroidx/compose/runtime/Composer;)V"
        )
    }
}
