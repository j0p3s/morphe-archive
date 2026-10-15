// Partly ported from AmpleReVanced/revanced-patches (GPL-3.0): the Sentry init anchor is Ample's
// DisableSentryFingerprint, and gating USE_S2_EVENTS / USE_TALK_SHARE_LOG follows its
// DisableS2Event and DisableTalkShareLog patches, re-anchored on the Available2.kt resolver for
// 26.8.3. The Tiara and Crashlytics anchors are new.

package app.template.patches.kakaotalk.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val CRASHLYTICS = "Lcom/google/firebase/crashlytics/FirebaseCrashlytics;"

// Kakao Tiara (analytics SDK, R8-renamed TiaraTracker): enqueue(TiaraLog), keyed by its overflow
// log line, and flush(), keyed by its two log lines.
internal object TiaraEnqueueFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Lcom/kakao/tiara/data/TiaraLog;"),
    strings = listOf("log queue overflow, queue size: "),
    custom = { _, classDef -> classDef.sourceFile == "TiaraTracker.java" },
)

internal object TiaraFlushFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(),
    strings = listOf("failed to flush, network is not available.", "log queue is empty."),
    custom = { _, classDef -> classDef.sourceFile == "TiaraTracker.java" },
)

// SentryAndroid.init(Context, ILogger, OptionsConfiguration) (R8-renamed in io.sentry.android.core).
internal object SentryAndroidInitFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Lio/sentry/ILogger;", "L"),
    strings = listOf("Fatal error during SentryAndroid.init(...)", "Failed to initialize Sentry's SDK"),
)

// The "Available2" remote feature flags: the enum of flag names, and the suspend resolver
// isEnabled(flag) that every flag check (USE_S2_EVENTS, USE_TALK_SHARE_LOG, ...) goes through.
internal object FeatureFlagEnumFingerprint : Fingerprint(
    name = "<clinit>",
    strings = listOf("USE_S2_EVENTS", "USE_TALK_SHARE_LOG"),
    custom = { _, classDef -> classDef.sourceFile == "Available2.kt" },
)

// The USE_S2_EVENTS flag getter, which calls the resolver with the flag constant.
internal object S2EventsFlagFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(name = "USE_S2_EVENTS", opcode = Opcode.SGET_OBJECT),
        methodCall(parameters = listOf("L", "Lkotlin/coroutines/Continuation;"), returnType = "Ljava/lang/Object;"),
    ),
    custom = { _, classDef -> classDef.sourceFile == "Available2.kt" },
)
