package app.cinemaniacpatches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {

    /**
     * Cinemaniac (it.papalillo.moviestowatch) by Antonio Papalillo.
     *
     * Confirmed target: versionName 5.0.7 / versionCode 172, minSdk 24,
     * downloaded as a plain APK.
     *
     * What the decompilation shows:
     *  - The app is a React Native application. The whole UI and business logic live
     *    in assets/index.android.bundle, which is Hermes bytecode, so there is no
     *    JavaScript level code to patch with a dex patcher.
     *  - The native layer contains only libraries. The only app specific native code
     *    is the Google Mobile Ads SDK, driven by io.invertase.googlemobileads
     *    (react-native-google-mobile-ads).
     *  - The "PRO" tier (remove ads, custom categories, favourites, streaming
     *    providers, themes, random selection) is validated server side against
     *    api.cinemaniac.app/v1/validate/user. PRO_LAST_VALIDATED is only a local
     *    cache with an expiry, not the source of truth.
     *
     * Because of that, the ads are the only part of the paid tier that can be
     * influenced from the client, and that is exactly what these patches do.
     * The PRO entitlement itself is never read, written or spoofed.
     */
    val CINEMANIAC = Compatibility(
        name = "Cinemaniac",
        packageName = "it.papalillo.moviestowatch",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x0B4F9E,
        targets = listOf(
            AppTarget(version = "5.0.7", minSdk = 24),
            AppTarget(version = null, isExperimental = true),
        )
    )
}
