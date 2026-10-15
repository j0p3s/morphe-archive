package app.template.patches.truecaller.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

// The three ad views every banner and native placement binds through. Their class names are kept
// (they are inflated from layouts); their bind methods are R8-renamed, so the patch selects them
// by parameter types.
internal const val ADS_CONTAINER = "Lcom/truecaller/ads/ui/AdsContainer;"
internal const val ADS_CONTAINER_LIGHT = "Lcom/truecaller/ads/ui/AdsContainerLight;"
internal const val ADS_SWITCH_VIEW = "Lcom/truecaller/ads/ui/AdsSwitchView;"
internal const val AD_LAYOUT_TYPE = "Lcom/truecaller/ads/AdLayoutTypeX;"

// AdsSwitchView's bind(bannerAd, AdLayoutTypeX). Its first parameter is the (obfuscated) banner
// ad holder type, which the patch reads from here instead of hard-coding it.
internal object AdsSwitchViewBindFingerprint : Fingerprint(
    definingClass = ADS_SWITCH_VIEW,
    returnType = "V",
    parameters = listOf("L", AD_LAYOUT_TYPE),
    custom = { method, _ -> method.parameterTypes.size == 2 },
)

// The interstitial manager's "can show interstitials" check (krk.e() in 26.39.6):
// isEnabled("featureInterstitialAd") && shouldShowAds. Every interstitial placement (Click to
// WhatsApp, call details, block, pay, ...) asks it first and runs the user's action at once when
// it is false; otherwise the action waits until the ad closes. The key is exact-matched; the only
// other readers are the toggle registries, which return lists.
internal object InterstitialAdsEnabledFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(string("featureInterstitialAd")),
)

// The "Opening WhatsApp / the app" screen shown before an app-open ad (kept: declared in the
// manifest). It holds no pending action: the coroutine that starts it runs the action itself after
// a one-second delay.
internal const val AD_APP_OPENING_ACTIVITY = "Lcom/truecaller/ads/interstitial/AdAppOpeningActivity;"
