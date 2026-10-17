// Based on o0b/morphe-patches (GPL-3.0), patches/src/main/kotlin/app/template/patches/businesscalendar2/telemetry/
// Fingerprints.kt: the Fyber crash-handler provider and the two promo-push methods. Its
// FirebaseInitProvider and FirebaseUtil.<init> kills are not ported: Remote Config drives the
// app's flags, so Firebase stays initialized and only the reporting calls are cut.
package app.template.patches.bizcal.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private const val PUSH_UTIL = "Lcom/appgenix/bizcal/util/PushNotificationsUtil;"

// Fyber FairBid's UncaughtExceptionHandlerContentProvider wraps the default uncaught-exception
// handler at process start, independent of FairBid.start. The framework call pins the body. Its
// other job, refreshing FairBid's ContextReference, is repeated by UserInfo.setGdprConsent at the
// top of StoreUtil.initializeMobileAds, so FairBid still works if Disable ads is off.
object FyberCrashHandlerProviderFingerprint : Fingerprint(
    definingClass = "Lcom/fyber/fairbid/internal/UncaughtExceptionHandlerContentProvider;",
    name = "onCreate",
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Thread;", name = "setDefaultUncaughtExceptionHandler"),
    ),
)

// PushNotificationsUtil.schedulePushNotification(Context) arms the sale promo push through an
// AlertWorker alarm tagged "push".
object SchedulePushNotificationFingerprint : Fingerprint(
    definingClass = PUSH_UTIL,
    name = "schedulePushNotification",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("push"),
        methodCall(definingClass = "Lcom/appgenix/bizcal/reminder/alerts/AlertWorker;", name = "scheduleAlarm"),
    ),
)

// PushNotificationsUtil.showPushNotification(Context) posts the promo notification and arms its
// "clear_push" alarm.
object ShowPushNotificationFingerprint : Fingerprint(
    definingClass = PUSH_UTIL,
    name = "showPushNotification",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/appgenix/bizcal/reminder/alerts/AlertUtils;", name = "notify"),
        string("clear_push"),
    ),
)

// Firebase Analytics: the app's FirebaseUtil logs every event through the public
// FirebaseAnalytics.logEvent(String, Bundle) (concrete in this build, same anchor as Shazam).
object FirebaseAnalyticsLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/analytics/FirebaseAnalytics;",
    name = "logEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
)

// The analytics connector (Crashlytics breadcrumbs, A/B testing) logs through the Google
// Measurement SDK entry point instead (same anchor as Sofascore).
object MeasurementSdkLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/measurement/api/AppMeasurementSdk;",
    name = "logEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Landroid/os/Bundle;"),
)
