package app.template.patches.transfermarkt.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// admob-plus-cordova-webview-ad's Plugin.initialize$lambda$2(Plugin, CordovaWebView): on the UI
// thread it hands the app's WebView to MobileAds.registerWebView (the Google WebView API for
// Ads, which lets the web ad tags talk to the GMA SDK), reloads the page and logs "Integrated
// the WebView API for Ads in <url>". The GMA call and the log prefix pin it.
internal object WebViewAdIntegrationFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(definingClass = "Lcom/google/android/gms/ads/MobileAds;", name = "registerWebView"),
        string("Integrated the WebView API for Ads in ", StringComparisonType.STARTS_WITH),
    ),
)
