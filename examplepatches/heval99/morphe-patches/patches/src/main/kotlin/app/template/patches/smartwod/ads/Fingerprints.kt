package app.template.patches.smartwod.ads

import app.morphe.patcher.Fingerprint

// react-native-google-mobile-ads' native module. JS mobileAds().initialize() lands here: it calls
// MobileAds.initialize(context, listener), resolves the JS promise from the listener, then calls
// MobileAds.setAppMuted(true). React Native registers the module reflectively, so the class and
// @ReactMethod names are kept by R8 and stable.
object RnMobileAdsInitializeFingerprint : Fingerprint(
    definingClass = "Lio/invertase/googlemobileads/ReactNativeGoogleMobileAdsModule;",
    name = "initialize",
    returnType = "V",
    parameters = listOf("Lcom/facebook/react/bridge/Promise;"),
)
