package app.aeong.patches.twitch.stuff

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val defaultFollowingPatch = bytecodePatch(
    name = "Open Following page by default",
    description = "no more shorts-like page at launch!",
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
        val method = DefaultHomePageFingerprint.method

        method.addInstructions(
            0,
            """
                sget-object v0, $FOLLOWING_PAGE_TYPE->INSTANCE:$FOLLOWING_PAGE_TYPE
                return-object v0
            """
        )
    }
}