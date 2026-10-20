package app.cinemaniacpatches.cine

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(Context)
 *
 * The Google advertising identifier (GAID). Replacing the returned Info with a
 * synthetic one removes the device level identifier from every ad, measurement and
 * attribution request the app makes.
 *
 * Reference (dexdump -d classes.dex):
 *
 *   public static AdvertisingIdClient.Info getAdvertisingIdInfo(Context);
 *   registers = 8, ins = 1   ->  v0..v6 are free temporaries
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

/**
 * Every entry point the Google Mobile Ads SDK exposes to actually fetch an ad.
 *
 * The SDK is driven from the React Native layer by io.invertase.googlemobileads, so
 * neutralising these methods stops banner, interstitial, rewarded, rewarded
 * interstitial, app open and native ads at the source: no request is ever made, so
 * nothing is downloaded, nothing is rendered and no impression is reported back.
 *
 * All of them return void, so the patch is a single `return-void` and no register
 * has to be touched. None of them contains a try/catch block, so inserting an
 * instruction at index 0 cannot shift any exception range.
 *
 * Reference (dexdump -d classes.dex) for the exact signatures and register counts.
 */
object AdMobLoadFingerprints {

    val all: List<Fingerprint> = listOf(

        // --- Banner ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
            name = "loadAd",
            returnType = "V",
            parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/admanager/AdManagerAdView;",
            name = "loadAd",
            returnType = "V",
            parameters = listOf("Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;"),
        ),

        // --- Interstitial ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/AdRequest;",
                "Lcom/google/android/gms/ads/interstitial/InterstitialAdLoadCallback;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/admanager/AdManagerInterstitialAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
                "Lcom/google/android/gms/ads/admanager/AdManagerInterstitialAdLoadCallback;",
            ),
        ),

        // --- Rewarded ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/AdRequest;",
                "Lcom/google/android/gms/ads/rewarded/RewardedAdLoadCallback;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
                "Lcom/google/android/gms/ads/rewarded/RewardedAdLoadCallback;",
            ),
        ),

        // --- Rewarded interstitial ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/AdRequest;",
                "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAdLoadCallback;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
                "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAdLoadCallback;",
            ),
        ),

        // --- App open ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/AdRequest;",
                "Lcom/google/android/gms/ads/appopen/AppOpenAd\$AppOpenAdLoadCallback;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/AdRequest;",
                "I",
                "Lcom/google/android/gms/ads/appopen/AppOpenAd\$AppOpenAdLoadCallback;",
            ),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
            name = "load",
            returnType = "V",
            parameters = listOf(
                "Landroid/content/Context;",
                "Ljava/lang/String;",
                "Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;",
                "I",
                "Lcom/google/android/gms/ads/appopen/AppOpenAd\$AppOpenAdLoadCallback;",
            ),
        ),

        // --- Native ads ---
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/AdLoader;",
            name = "loadAd",
            returnType = "V",
            parameters = listOf("Lcom/google/android/gms/ads/AdRequest;"),
        ),
        Fingerprint(
            definingClass = "Lcom/google/android/gms/ads/AdLoader;",
            name = "loadAd",
            returnType = "V",
            parameters = listOf("Lcom/google/android/gms/ads/admanager/AdManagerAdRequest;"),
        ),
    )
}
