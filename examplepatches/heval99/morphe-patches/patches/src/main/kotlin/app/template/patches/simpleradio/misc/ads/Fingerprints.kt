package app.template.patches.simpleradio.misc.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

// AdsExperiment (class name kept, members obfuscated: the getter is s2() on 6.2.0, x2() on
// 6.2.4) loads its Remote Config keys into static fields. The Boolean stored right after
// reading "android_search_nativeads_show" is the native-ad slot switch that the station
// lists consult (with the IAB premium check) before reserving the "Ad" row.
internal object NativeAdsShowLoaderFingerprint : Fingerprint(
    definingClass = "Lcom/streema/simpleradio/experiment/AdsExperiment;",
    filters = listOf(string("android_search_nativeads_show")),
)
