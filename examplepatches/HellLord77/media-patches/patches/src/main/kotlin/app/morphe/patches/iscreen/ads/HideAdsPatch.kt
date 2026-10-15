package app.morphe.patches.iscreen.ads

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.iscreen.shared.Constants.COMPATIBILITY_ISCREEN
import app.morphe.patches.iscreen.shared.morphe.com.rockstreamer.iscreen.util.PreferenceUtil

@Suppress("unused")
val hideAdsPatch =
    bytecodePatch(
        name = "Hide ads",
        description = "Hides all ads from app.",
        default = false,
    ) {
        compatibleWith(COMPATIBILITY_ISCREEN)

        dependsOn(PreferenceUtil.isVastShowPatch, PreferenceUtil.isBannerAdsShowPatch)
    }
