package app.template.patches.shazam.misc.telemetry

import app.morphe.patcher.Fingerprint

// Firebase Analytics: logEvent(String, Bundle) is public and concrete in this
// build, so anchor on the stable library API directly.
object FirebaseAnalyticsLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/analytics/FirebaseAnalytics;",
    name = "logEvent",
    parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
)

// Crashlytics is fully public in this build (not R8-renamed): the public collection flag
// getter, kept for parity with the public API. The data-collection arbiter that every
// internal reader consults is forced off separately, by the key it reads (see
// DisableTelemetryPatch).
object CrashlyticsCollectionEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/crashlytics/FirebaseCrashlytics;",
    name = "isCrashlyticsCollectionEnabled",
    returnType = "Z",
    parameters = listOf(),
)
