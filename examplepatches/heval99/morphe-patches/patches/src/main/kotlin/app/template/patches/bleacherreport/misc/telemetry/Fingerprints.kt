package app.template.patches.bleacherreport.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.string

// comScore keeps its public API names.
internal const val COMSCORE_ANALYTICS = "Lcom/comscore/Analytics;"

// Branch's PrefHelper key for "tracking disabled" (TrackingController). Branch keeps its
// preference keys; the readers (11.12.0.33: io.branch.referral.d.<init>, the lifecycle callback
// io.branch.referral.e and BranchUniversalObject.generateShortUrl in io.branch.indexing.a) are
// renamed.
internal const val BRANCH_TRACKING_STATE = "bnc_tracking_state"

// Crashlytics DataCollectionArbiter.isAutomaticDataCollectionEnabled()Z
// (11.12.0.33: com.google.firebase.crashlytics.internal.common.e0.a).
internal object CrashlyticsDataCollectionFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(string("firebase_crashlytics_collection_enabled manifest flag", StringComparisonType.STARTS_WITH)),
)

// Every reader and writer of the Branch tracking-state preference.
internal object BranchTrackingStateFingerprint : Fingerprint(
    filters = listOf(string(BRANCH_TRACKING_STATE, StringComparisonType.EQUALS)),
)

// Warner Bros. Discovery Beam instrumentation SDK: PersistentEventQueue.add(entity, Continuation)
// (11.12.0.33: com.wbd.beam...infrastructure.events.t.f). Every Beam event is queued here.
internal object BeamEventQueueAddFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(string("EnrichedEventQueue.add(): exception", StringComparisonType.STARTS_WITH)),
    custom = { method, _ -> method.parameterTypes.size == 2 },
)

// Beam EventQueueMonitor.onTaskExecution(Continuation) (11.12.0.33: events.l.a): the periodic
// job that sends queued events.
internal object BeamEventQueueMonitorFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(string("onTaskExecution skipped, mode is ", StringComparisonType.STARTS_WITH)),
    custom = { method, _ -> method.parameterTypes.size == 1 },
)

// CanonicalEventInteractorImpl.dispatch(event, Continuation) (11.12.0.33:
// domain.interactor.instrumentation.impl.b.a): forwards app events to Braze when the
// "br-braze-event-forwarding" flag is on. The flag model's <clinit> holds the same key.
internal object BrazeEventForwardingFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(string("br-braze-event-forwarding", StringComparisonType.EQUALS)),
    custom = { method, _ -> method.parameterTypes.size == 2 },
)
