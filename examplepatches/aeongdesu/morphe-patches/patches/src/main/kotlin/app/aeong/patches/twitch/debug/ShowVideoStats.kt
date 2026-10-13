package app.aeong.patches.twitch.debug

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val showVideoStatsPatch = bytecodePatch(
    name = "Show video stats",
    description = "Shows video debug stats button in the player.",
    default = true
) {
    compatibleWith(
        Compatibility(
            name = "Twitch",
            packageName = "tv.twitch.android.app",
            appIconColor = 0x9147FF,
            targets = listOf(
                AppTarget("30.7.2")
            )
        )
    )

    execute {
        BottomPlayerOverlayViewModelConstructorFingerprint.method.addInstructions(
            0,
            """
                const/16 p5, 0x1
            """
        )
    }
}
