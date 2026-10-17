package app.template.patches.duplila.misc.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal const val GMA = "Lcom/google/android/gms/ads/"

// MainActivity's UMP consent flow, run once the premium check finishes (argument = paid). It
// requests consent info, shows the consent form if required and, for free users, closes the main
// screen when consent is refused ("Ad/Analytics consent is required for the free version") or
// otherwise calls SelectionScreenModel.initAds(), the only setter of canShowAds. It is the app's
// only requestConsentInfoUpdate call site.
internal object ConsentFlowFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/ump/ConsentInformation;",
            name = "requestConsentInfoUpdate",
        ),
    ),
)

// The banner's AndroidView factory lambda in BannerKt: builds the AdView, sets the ad unit id and
// loads it. The only app method that loads an ad; it anchors the class of the BannerView
// composable that hosts it.
internal object BannerAdViewFactoryFingerprint : Fingerprint(
    returnType = "${GMA}AdView;",
    filters = listOf(
        methodCall(definingClass = "${GMA}AdView;", name = "setAdUnitId"),
        methodCall(definingClass = "${GMA}AdView;", name = "loadAd"),
    ),
    custom = { _, classDef -> !classDef.type.startsWith("Lcom/google/") },
)
