package app.morphe.patches.vantasurf

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Removes the native ads that are injected into the feed.
 *
 * The host asks the ad manager to show a native ad in a given view.
 */
@Suppress("unused")
val removeNativeAdsPatch = bytecodePatch(
    name = "Remove native ads",
    description = "Removes the native ads injected into the feed.",
    default = true,
) {
    compatibleWith(VantaSurfCompatibility)

    execute {
        ShowNativeAdFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
    }
}
