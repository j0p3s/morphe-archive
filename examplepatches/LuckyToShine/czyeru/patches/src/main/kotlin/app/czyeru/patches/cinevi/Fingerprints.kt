package app.czyeru.patches.cinevi

import app.morphe.patcher.Fingerprint

private const val AD_INFO_ENTRY = "Lcom/mgs/carparking/netbean/AdInfoEntry;"
private const val WX_SDK = "Lcom/wangxiong/sdk/WxSDK;"

/**
 * The server-provided ad config ([AD_INFO_ENTRY]) exposes one list of ads per placement
 * (splash, home, search, rank, video pause, banners...). There is no `ad_position_14`.
 */
internal val AD_POSITIONS = (1..26).filter { it != 14 }

internal fun adPositionFingerprint(position: Int) = Fingerprint(
    definingClass = AD_INFO_ENTRY,
    name = "getAd_position_$position",
    returnType = "Ljava/util/List;",
    parameters = listOf()
)

/**
 * Facade over the ad engine (com.yk.e). The app preloads banner, interstitial, native
 * and reward ads at startup through these.
 */
internal val WX_SDK_PRELOAD_NAMES = listOf(
    "initAdCache",
    "preloadBannerAd",
    "preloadInterstitialAd",
    "preloadNativeAd",
    "preloadRewardAd"
)

internal fun wxSdkFingerprint(name: String) = Fingerprint(
    definingClass = WX_SDK,
    name = name,
    returnType = "V"
)
