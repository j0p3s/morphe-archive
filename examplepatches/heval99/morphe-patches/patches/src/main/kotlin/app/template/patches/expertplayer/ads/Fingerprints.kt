package app.template.patches.expertplayer.ads

import app.morphe.patcher.Fingerprint

// The Flutter google_mobile_ads plugin starts the ad SDK from an anonymous Thread in
// FlutterMobileAdsWrapper.initialize(Context, listener). That Runnable's run() calls
// MobileAds.initialize, which starts Google Mobile Ads and every mediation network behind it.
// Dart requests the start through the plugin's MethodChannel, so this is the one Java-side point
// where the ad SDK begins.
object MobileAdsInitRunnableFingerprint : Fingerprint(
    definingClass = "Lio/flutter/plugins/googlemobileads/FlutterMobileAdsWrapper\$1;",
    name = "run",
    returnType = "V",
    parameters = emptyList(),
)

// PairIP's license check, run from com.pairip.application.Application.attachBaseContext. This app
// has the license layer only (no VMRunner, SignatureCheck or StartupLauncher). Without Play's
// license the check kills the process. The same no-op is what pairip-disabler applies to this
// layer. The class and method names are kept by PairIP's R8 rules, so they are stable.
object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
