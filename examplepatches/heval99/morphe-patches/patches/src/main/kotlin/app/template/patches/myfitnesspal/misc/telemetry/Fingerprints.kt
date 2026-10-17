package app.template.patches.myfitnesspal.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

internal const val OBSERVABILITY = "Lcom/myfitnesspal/platform/observability/datadog/"
internal const val RUM_START_DECISION = "${OBSERVABILITY}DatadogRumStartDecision;"
internal const val SKIP_REASON = "${OBSERVABILITY}DatadogRumStartDecision\$SkipReason;"

/**
 * 26.41.0 added the Datadog SDK. R8 inlined its public facade (Datadog.initialize, Rum.enable)
 * into the app's own startup, which first asks this decision function whether to start at all:
 * missing credentials, no cached feature-flag treatment, the flag off or a bad sample rate give
 * `Skip(reason)`; only a `Start` result initializes the SDK core, RUM and network tracing. The
 * kept `DatadogRumStartDecision` return type and its `SkipReason.FLAG_DISABLED` branch
 * (`new Skip(FLAG_DISABLED)`) identify it.
 */
internal object DatadogRumStartDecisionFingerprint : Fingerprint(
    returnType = RUM_START_DECISION,
    filters = listOf(
        fieldAccess(definingClass = SKIP_REASON, name = "FLAG_DISABLED", opcode = Opcode.SGET_OBJECT),
        methodCall(
            name = "<init>",
            parameters = listOf(SKIP_REASON),
            opcode = Opcode.INVOKE_DIRECT,
            location = MatchAfterImmediately(),
        ),
    ),
)
