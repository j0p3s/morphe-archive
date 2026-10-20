package app.morphe.patches.vantasurf

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes the ads that interrupt playback.
 *
 * The player asks the ad manager whether it should intercept the playback of an
 * episode. Forcing that answer to "no" removes the interstitial ads without
 * touching the ad SDKs themselves.
 */
@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes the interstitial ads shown between episodes.",
    default = true,
) {
    compatibleWith(VantaSurfCompatibility)

    execute {
        InterceptBeforePlayFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
