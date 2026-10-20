package app.template.patches.truecaller.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// Kept CleverTap wrapper interface; the app's implementation of it is R8-renamed, so the patch
// finds it by this interface.
internal const val CLEVERTAP_MANAGER = "Lcom/truecaller/clevertap/CleverTapManager;"

// AppsFlyer's public API class. It only declares abstract methods; the implementation is an
// SDK-internal subclass whose name AppsFlyer itself rotates (AFa1ySDK here), so the patch finds
// it by its superclass.
internal const val APPSFLYER_LIB = "Lcom/appsflyer/AppsFlyerLib;"

// Server-set master switch of the CleverTap integration, stored in the core SharedPreferences
// (not a feature toggle, so the toggle resolver never sees it).
internal const val CLEVERTAP_FEATURE_KEY = "featureCleverTap"

// The QA menu reads and writes the key too; it is debug UI and is left alone.
internal const val QA_PACKAGE_PREFIX = "Lcom/truecaller/qa/"

// Firebase's documented manifest switches. The stock manifest sets none of them except
// firebase_crashlytics_collection_enabled (true). The patch forces the value each reader gets
// instead of editing the manifest, so the resource table is not re-encoded.
internal const val ANALYTICS_DEACTIVATED_KEY = "firebase_analytics_collection_deactivated"
internal const val ANALYTICS_ADID_KEY = "google_analytics_adid_collection_enabled"
internal const val CRASHLYTICS_COLLECTION_KEY = "firebase_crashlytics_collection_enabled"
internal const val SESSIONS_ENABLED_KEY = "firebase_sessions_enabled"

// Crashlytics' DataCollectionArbiter log line ("Crashlytics automatic data collection ENABLED by
// ..."): together with the collection key it singles out the arbiter (n5e in 26.40.5), whose
// class R8 renames.
internal const val CRASHLYTICS_ARBITER_LOG = "Crashlytics automatic data collection "

// Application.onCreate calls this to switch the app start tracker on; while it stays off every
// onActivityCreate/onServiceCreate/onBroadcastReceive log returns early.
internal object AppStartTrackerEnableTrackingFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/analytics/technical/AppStartTracker;",
    name = "enableTracking",
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)

// Every method that loads "featureCleverTap" and later calls SharedPreferences.getBoolean. In
// 26.40.5 these are the CleverTap SDK init (osa.a) and its second gate in the same class (osa.c),
// the CRM push handler that drops "wzrk_" (CleverTap) pushes while it is off (ata.a) and the
// "CleverTapRefreshWorkAction" work action (j92.b). The writers (the config sync, the QA menu)
// only store it, and the toggle catalogue only lists it; the patch checks that the key really is
// the getBoolean key before forcing anything.
internal object CleverTapFeatureReadFingerprint : Fingerprint(
    filters = listOf(
        string(CLEVERTAP_FEATURE_KEY),
        methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
    ),
)

// The measurement SDK reads its manifest flags through one config helper, Boolean get(String key)
// (zzal.u0 in 26.40.5; null when the manifest does not set the key). The deactivated flag is read
// once (zzal), the advertising ID flag twice (zzgh, and the app-info builder yhb0).
internal object AnalyticsDeactivatedReadFingerprint : Fingerprint(
    filters = listOf(
        string(ANALYTICS_DEACTIVATED_KEY),
        methodCall(parameters = listOf("Ljava/lang/String;"), returnType = "Ljava/lang/Boolean;"),
    ),
)

internal object AnalyticsAdidReadFingerprint : Fingerprint(
    filters = listOf(
        string(ANALYTICS_ADID_KEY),
        methodCall(parameters = listOf("Ljava/lang/String;"), returnType = "Ljava/lang/Boolean;"),
    ),
)

// Firebase Sessions' LocalOverrideSettings.sessionEnabled (tiq.c in 26.40.5): the manifest value,
// or null when unset. It wins over the remote session settings.
internal object SessionsEnabledOverrideFingerprint : Fingerprint(
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf(),
    filters = listOf(
        string(SESSIONS_ENABLED_KEY),
        methodCall(definingClass = "Landroid/os/BaseBundle;", name = "getBoolean"),
    ),
)
