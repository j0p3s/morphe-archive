package app.renamer.patches.ads

import app.morphe.patcher.Fingerprint

/**
 * Renamer 18.0 (com.aj.renamer) is not obfuscated, so these fingerprints
 * match the app's own classes and methods by name.
 */

/** Loads the banner ad into the AdView at the bottom of the main screen. */
object InitBannerAdFingerprint : Fingerprint(
    definingClass = "Lcom/aj/renamer/MainActivity;",
    name = "initAd",
    returnType = "V",
    parameters = listOf(),
)

/** Receives the loaded interstitial ad. The ad is only shown when this stored it. */
object InterstitialAdLoadedFingerprint : Fingerprint(
    definingClass = "Lcom/aj/renamer/InputFragment\$1;",
    name = "onAdLoaded",
    returnType = "V",
    parameters = listOf("Lcom/google/android/gms/ads/interstitial/InterstitialAd;"),
)
