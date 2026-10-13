package app.czyeru.patches.cinevi

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.czyeru.patches.shared.Constants.COMPATIBILITY_CINEVI

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Removes ads (splash, banners, popups, interstitials and native ads).",
    default = true
) {
    compatibleWith(COMPATIBILITY_CINEVI)

    execute {
        // Every ad placement is read from AdInfoEntry. Returning an empty list makes all
        // callers skip the ad. They already handle this case (for example the splash
        // screen goes straight to the main screen when ad_position_1 is empty).
        AD_POSITIONS.forEach { position ->
            adPositionFingerprint(position).method.addInstructions(
                0,
                """
                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                """
            )
        }

        // Stop the ad engine from preloading ads at startup.
        WX_SDK_PRELOAD_NAMES.forEach { name ->
            wxSdkFingerprint(name).method.addInstructions(0, "return-void")
        }
    }
}
