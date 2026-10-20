package app.columnpatches.column

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.getcapacitor.BridgeWebViewClient is the WebViewClient used by the Capacitor
 * bridge. Android asks it for every single sub-resource the web app loads
 * (scripts, images, XHR/fetch, beacons). Returning a response from here means
 * "do not hit the network", which is what the ad blocker below relies on.
 *
 * Reference (jadx, classes3.dex):
 *
 *   public android.webkit.WebResourceResponse shouldInterceptRequest(
 *           android.webkit.WebView webView,
 *           android.webkit.WebResourceRequest webResourceRequest) {
 *       return this.bridge.getLocalServer().shouldInterceptRequest(webResourceRequest);
 *   }
 */
object WebViewShouldInterceptRequestFingerprint : Fingerprint(
    definingClass = "Lcom/getcapacitor/BridgeWebViewClient;",
    name = "shouldInterceptRequest",
    returnType = "Landroid/webkit/WebResourceResponse;",
    parameters = listOf(
        "Landroid/webkit/WebView;",
        "Landroid/webkit/WebResourceRequest;",
    ),
)

/**
 * capacitor.plugin.appsflyer.sdk.AppsFlyerPlugin.initSDK(PluginCall)
 *
 * Starts the AppsFlyer attribution SDK. Used by the optional privacy patch.
 */
object AppsFlyerInitFingerprint : Fingerprint(
    definingClass = "Lcapacitor/plugin/appsflyer/sdk/AppsFlyerPlugin;",
    name = "initSDK",
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/PluginCall;"),
)

/**
 * capacitor.plugin.appsflyer.sdk.AppsFlyerPlugin.startSDK(PluginCall)
 *
 * Sends the first session / attribution payload. Used by the optional privacy patch.
 */
object AppsFlyerStartFingerprint : Fingerprint(
    definingClass = "Lcapacitor/plugin/appsflyer/sdk/AppsFlyerPlugin;",
    name = "startSDK",
    returnType = "V",
    parameters = listOf("Lcom/getcapacitor/PluginCall;"),
)

/**
 * com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(Context)
 *
 * The Google advertising identifier (GAID). Replacing the returned Info with a
 * synthetic one removes the device level identifier from every ad and measurement
 * request the app makes.
 *
 * Reference (dexdump -d classes3.dex):
 *
 *   public static AdvertisingIdClient.Info getAdvertisingIdInfo(Context);
 *   registers = 10, ins = 1   ->  v0..v8 are free temporaries
 *
 * AdvertisingIdClient.Info.<init>(String, boolean) is a public constructor, so the
 * replacement instance can be built without touching any private state.
 */
object AdvertisingIdFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/identifier/AdvertisingIdClient;",
    name = "getAdvertisingIdInfo",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Lcom/google/android/gms/ads/identifier/AdvertisingIdClient\$Info;",
    parameters = listOf("Landroid/content/Context;"),
)
