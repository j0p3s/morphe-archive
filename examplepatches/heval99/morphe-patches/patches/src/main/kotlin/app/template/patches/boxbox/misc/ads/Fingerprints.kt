package app.template.patches.boxbox.misc.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// AppConfigModel.toString(): a Kotlin data class, so the property names are literal strings.
// Every ad placement (AppLovin up to 5.4.16, Google Mobile Ads from 5.4.17) shows only when
// the plan is Free and the remote config's isAdsEnabled is true. The field read right after
// ", isAdsEnabled=" is the flag.
internal object AppConfigModelToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("AppConfigModel(", StringComparisonType.STARTS_WITH),
        string(", isAdsEnabled="),
    ),
)

// Google Mobile Ads (5.4.17+). R8 renames most GMA classes, so only MobileAds keeps its name;
// the rest is matched by the SDK's own precondition messages and method shape.

// MobileAds.initialize(Context, OnInitializationCompleteListener).
internal object MobileAdsInitializeFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAds;",
    returnType = "V",
    filters = listOf(string("Initializing on calling thread", StringComparisonType.STARTS_WITH)),
    custom = { method, _ -> AccessFlags.STATIC.isSet(method.accessFlags) },
)

// InterstitialAd.load(Context, String, AdRequest, InterstitialAdLoadCallback). Its class is
// abstract, so only concrete matches are patched. RewardedAd.load has the same shape.
internal object FullScreenAdLoadFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "L", "L"),
    filters = listOf(string("AdRequest cannot be null.")),
    custom = { method, _ ->
        AccessFlags.STATIC.isSet(method.accessFlags) && method.implementation != null
    },
)

// BaseAdView.loadAd(AdRequest): the abstract ViewGroup that declares setAdUnitId (public API
// name, kept) and that AdView extends.
internal object BaseAdViewLoadAdFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("L"),
    filters = listOf(string("#008 Must be called on the main UI thread.")),
    custom = { method, classDef ->
        AccessFlags.FINAL.isSet(method.accessFlags) &&
            AccessFlags.ABSTRACT.isSet(classDef.accessFlags) &&
            classDef.superclass == "Landroid/view/ViewGroup;" &&
            classDef.methods.any { it.name == "setAdUnitId" }
    },
)
