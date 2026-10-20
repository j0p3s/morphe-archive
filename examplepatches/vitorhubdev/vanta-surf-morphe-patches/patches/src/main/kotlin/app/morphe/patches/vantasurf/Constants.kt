package app.morphe.patches.vantasurf

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

/**
 * Vanta Surf (`com.vvv.velvetreel.treel`) is a lightweight WebView browser.
 *
 * The app bundles the short drama player, which shows interstitial ads between
 * episodes, injects native ads into the feed and gates playback behind a
 * "watch an ad to keep watching" popup.
 */
val VantaSurfCompatibility = Compatibility(
    packageName = "com.vvv.velvetreel.treel",
    name = "Vanta Surf",
    description = "Lightweight browser with a built in ad blocker.",
    appIconColor = 0x1E88E5,
    targets = listOf(
        AppTarget(
            version = "5.0.2",
            versionCode = 50002,
        ),
        AppTarget(
            version = "5.0.0",
            versionCode = 50000,
        ),
    ),
)
