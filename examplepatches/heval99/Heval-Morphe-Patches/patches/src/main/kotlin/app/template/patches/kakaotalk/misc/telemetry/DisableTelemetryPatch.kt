// Partly ported from AmpleReVanced/revanced-patches (GPL-3.0): DisableSentry, DisableS2Event and
// DisableTalkShareLog, merged with new Tiara and Crashlytics hooks into one always-on patch.

package app.template.patches.kakaotalk.misc.telemetry

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val CRASHLYTICS_VOID_METHODS = setOf("sendUnsentReports", "recordException", "log", "setUserId", "setCustomKey", "setCustomKeys")
private val DISABLED_FLAGS = listOf("USE_S2_EVENTS", "USE_TALK_SHARE_LOG")

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Stops Kakao Tiara analytics, S2 event and share logging, Sentry and Crashlytics reports.",
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // AbuseDetectUtil / StatApi collection is integrity, not telemetry, and is left alone.

        // 1. Tiara: logs are never queued and the queue is never flushed.
        TiaraEnqueueFingerprint.method.returnEarly()
        TiaraFlushFingerprint.method.returnEarly()

        // 2. Sentry: SentryAndroid.init does nothing, so the SDK stays a no-op hub.
        SentryAndroidInitFingerprint.method.returnEarly()

        // 3. Crashlytics: no reports, exceptions, breadcrumbs or keys are recorded or sent.
        val crashlytics = mutableClassDefBy(CRASHLYTICS).methods.filter {
            it.name in CRASHLYTICS_VOID_METHODS && it.returnType == "V" && it.implementation != null
        }
        if (crashlytics.none { it.name == "sendUnsentReports" }) {
            throw PatchException("$CRASHLYTICS has no sendUnsentReports()")
        }
        crashlytics.forEach { it.returnEarly() }

        // 4. Remote flags USE_S2_EVENTS and USE_TALK_SHARE_LOG resolve to false; every other flag
        // still goes through the resolver.
        val flagEnum = FeatureFlagEnumFingerprint.originalClassDef
        DISABLED_FLAGS.forEach { flag ->
            if (flagEnum.fields.none { it.name == flag && it.type == flagEnum.type }) {
                throw PatchException("${flagEnum.type} has no $flag")
            }
        }
        val resolverCall = S2EventsFlagFingerprint.instructionMatches[1]
            .getInstruction<ReferenceInstruction>().reference as MethodReference
        if (resolverCall.parameterTypes.first().toString() != flagEnum.type) {
            throw PatchException("USE_S2_EVENTS is not resolved through $resolverCall")
        }
        val resolver = mutableClassDefBy(resolverCall.definingClass).methods.singleOrNull {
            it.name == resolverCall.name && it.implementation != null &&
                it.parameterTypes.map(CharSequence::toString) == resolverCall.parameterTypes.map(CharSequence::toString)
        } ?: throw PatchException("Could not resolve the feature flag resolver $resolverCall")

        // p1 is the flag; v0 is the first local, which the method writes before reading.
        resolver.addInstructionsWithLabels(
            0,
            """
                sget-object v0, ${flagEnum.type}->USE_S2_EVENTS:${flagEnum.type}
                if-eq p1, v0, :flag_disabled
                sget-object v0, ${flagEnum.type}->USE_TALK_SHARE_LOG:${flagEnum.type}
                if-ne p1, v0, :resolve_flag
                :flag_disabled
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """,
            ExternalLabel("resolve_flag", resolver.getInstruction(0)),
        )
    }
}
