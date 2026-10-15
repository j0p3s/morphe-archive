package app.template.patches.bluemail.ads

import app.morphe.patcher.Fingerprint

// react-native-google-mobile-ads' native module. JS mobileAds().initialize() lands here: it calls
// MobileAds.initialize(context, listener) and the listener resolves the JS promise with the
// adapter statuses. React Native registers the module reflectively, so the class and
// @ReactMethod names are kept by R8 and stable.
object RnMobileAdsInitializeFingerprint : Fingerprint(
    definingClass = "Lio/invertase/googlemobileads/ReactNativeGoogleMobileAdsModule;",
    name = "initialize",
    returnType = "V",
    parameters = listOf("Lcom/facebook/react/bridge/Promise;"),
)
