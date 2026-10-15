package app.template.patches.expertplayer.ads

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_EXPERTPLAYER

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Stops the Google Mobile Ads SDK and its mediation networks from starting, " +
        "so the app's banner and interstitial ads never load, and skips PairIP's Play license " +
        "check that otherwise kills the app when it is not installed from Play. Playback and " +
        "playlists are unchanged."
) {
    compatibleWith(COMPATIBILITY_EXPERTPLAYER)

    execute {
        PairipCheckLicenseFingerprint.method.returnEarly()
        MobileAdsInitRunnableFingerprint.method.returnEarly()
    }
}
