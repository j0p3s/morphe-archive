// Based on o0b/morphe-patches (GPL-3.0), patches/src/main/kotlin/app/template/patches/businesscalendar2/ads/
// Fingerprints.kt: the FairBid backstop in StoreUtil (initializeMobileAds, preloadInterstitialAd,
// showInterstitialAd) and the Facebook Audience Network auto-init provider.
package app.template.patches.bizcal.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

private const val STORE_UTIL = "Lcom/appgenix/bizcal/util/StoreUtil;"
private const val FAIRBID = "Lcom/fyber/FairBid;"
private const val INTERSTITIAL = "Lcom/fyber/fairbid/ads/Interstitial;"

// The one user-level ad gate. AdsUtil.showAdsForUser(Context) is consulted before the mediation
// SDKs are initialized (MainActivity.onCreate -> StoreUtil.initializeMobileAds) and before any
// interstitial is preloaded or shown. Forcing it false keeps the ad SDKs from starting and stops the
// interstitial path. The app's own code is readable, so the names are stable.
object ShowAdsForUserFingerprint : Fingerprint(
    definingClass = "Lcom/appgenix/bizcal/util/AdsUtil;",
    name = "showAdsForUser",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)

// AdsUtil.showAdsAccordingToFirebase(Context) returns the Remote Config "show ads" flag
// (FirebaseUtil.showAds). Besides feeding showAdsForUser, it decides whether a free user gets the
// "ads or Pro" dialog (AdvertOrProDialogFragment.showDialog), whose accept button starts FairBid.
// Its only other readers (ProFeatureGroup, GoProRecyclerViewAdapter, GoProSimplePurchasedScreen)
// list the "No ads" Pro feature. The FirebaseUtil call pins the body.
object ShowAdsAccordingToFirebaseFingerprint : Fingerprint(
    definingClass = "Lcom/appgenix/bizcal/util/AdsUtil;",
    name = "showAdsAccordingToFirebase",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/appgenix/bizcal/util/firebase/FirebaseUtil;", name = "showAds"),
    ),
)

// StoreUtil.initializeMobileAds(Activity) starts FairBid. In 2.55.7 the method itself has no ad
// gate: MainActivity checks showAdsForUser first, but AdvertOrProDialogFragment calls it directly
// once the user agrees to see ads. The FairBid calls pin the Play-flavor body.
object InitializeMobileAdsFingerprint : Fingerprint(
    definingClass = STORE_UTIL,
    name = "initializeMobileAds",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
    filters = listOf(
        methodCall(definingClass = FAIRBID, name = "configureForAppId"),
        methodCall(definingClass = FAIRBID, name = "start"),
    ),
)

// StoreUtil.preloadInterstitialAd(Activity): the gated FairBid interstitial request.
object PreloadInterstitialAdFingerprint : Fingerprint(
    definingClass = STORE_UTIL,
    name = "preloadInterstitialAd",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;"),
    filters = listOf(
        methodCall(definingClass = INTERSTITIAL, name = "request"),
    ),
)

// StoreUtil.showInterstitialAd(Activity, String): the gated FairBid interstitial show. Callers
// already handle false ("no ad shown"), the path pro users take.
object ShowInterstitialAdFingerprint : Fingerprint(
    definingClass = STORE_UTIL,
    name = "showInterstitialAd",
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "Ljava/lang/String;"),
    filters = listOf(
        methodCall(definingClass = INTERSTITIAL, name = "show"),
    ),
)

// Facebook Audience Network registers its own ContentProvider, which runs at process start and
// bootstraps the FB ad SDK (DynamicLoaderFactory.initialize) outside the FairBid flow. The provider
// class and the framework onCreate override are not obfuscated (same anchor as Sofascore). It
// already returns false normally, and its authority is never queried.
object AudienceNetworkContentProviderFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/ads/AudienceNetworkContentProvider;",
    name = "onCreate",
    returnType = "Z",
    parameters = listOf(),
)
