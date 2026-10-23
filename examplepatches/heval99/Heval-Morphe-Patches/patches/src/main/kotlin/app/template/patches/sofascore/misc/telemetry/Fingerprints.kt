package app.template.patches.sofascore.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// Firebase Analytics' documented manifest switch. The stock manifest does not set it; the read
// is forced to true in the bytecode instead of in AndroidManifest.xml, so no resource patch
// re-encodes the whole resource table (see DisableTelemetryPatch).
internal const val ANALYTICS_DEACTIVATED_KEY = "firebase_analytics_collection_deactivated"

// The measurement SDK reads its manifest flags through one config helper, Boolean get(String)
// (zzal.j0 in 26.10.05; null when the manifest does not set the key). The deactivated flag is
// read exactly once (zzal.k0), which this fingerprint finds together with the read's call shape.
internal object AnalyticsDeactivatedReadFingerprint : Fingerprint(
    filters = listOf(
        string(ANALYTICS_DEACTIVATED_KEY),
        methodCall(parameters = listOf("Ljava/lang/String;"), returnType = "Ljava/lang/Boolean;"),
    ),
)

// Firebase Analytics: in this build FirebaseAnalytics no longer declares logEvent, so
// every event funnels through the underlying Google Measurement SDK entry point.
object GmsMeasurementLogEventFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/measurement/api/AppMeasurementSdk;",
    name = "logEvent",
)
