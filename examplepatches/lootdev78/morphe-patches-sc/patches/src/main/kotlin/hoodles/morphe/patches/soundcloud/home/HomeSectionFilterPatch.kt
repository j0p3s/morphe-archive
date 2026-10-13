/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.home

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import hoodles.morphe.patches.soundcloud.shared.Constants
import hoodles.morphe.patches.soundcloud.shared.soundcloudCorePatch

val soundcloudHomeSectionFilterPatch = bytecodePatch(
    name = "Filter home sections",
    description = "Allows hiding customizable sections and carousels on the SoundCloud Home feed."
) {
    compatibleWith(Constants.COMPATIBILITY)

    dependsOn(soundcloudCorePatch)

    execute {
        val match = SDUIToHomeResultsFingerprint.instructionMatches[0]
        val listRegister = (match.instruction as TwoRegisterInstruction).registerA
        SDUIToHomeResultsFingerprint.method.addInstructions(
            match.index + 1,
            """
                invoke-static {v$listRegister}, Lapp/morphe/extension/soundcloud/HomeSectionFilter;->filter(Ljava/util/List;)Ljava/util/List;
                move-result-object v$listRegister
            """.trimIndent()
        )
    }
}
