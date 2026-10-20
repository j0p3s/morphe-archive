package app.template.patches.firefoxfocus.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

internal const val USAGE_REPORTING_API = "Lorg/mozilla/focus/telemetry/GleanUsageReportingApi;"
internal const val SETTINGS = "Lorg/mozilla/focus/utils/Settings;"
internal const val DAILY_USAGE_PING_DELEGATE = "isDailyUsagePingEnabled\$delegate"
internal const val BOOLEAN_PREFERENCE = "Lmozilla/components/support/ktx/android/content/BooleanPreference;"
internal const val NIMBUS = "Lorg/mozilla/experiments/nimbus/Nimbus;"

// Focus is built with -dontobfuscate, so its own class and method names are stable.
// GleanUsageReportingMetricsService.start(): enables the usage-reporting ping, stores a usage
// profile id and registers the lifecycle observer that submits the ping on every foreground and
// background. Called from FocusApplication.onCreate and the Settings switch.
internal object UsageReportingStartFingerprint : Fingerprint(
    definingClass = "Lorg/mozilla/focus/telemetry/GleanUsageReportingMetricsService;",
    name = "start",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(methodCall(definingClass = USAGE_REPORTING_API, name = "setEnabled")),
)

// The Settings constructor builds the "Daily usage ping" delegate (pref_key_daily_usage_ping) as
// BooleanPreference(key, default lambda, persistDefaultIfNotExists = true). The default is
// GleanMetricsService.shouldTelemetryBeEnabledByDefault, inlined: true unless the user turned the old
// telemetry switch off (or on one BlackBerry model). The first read stores it.
internal object SettingsInitFingerprint : Fingerprint(
    definingClass = SETTINGS,
    name = "<init>",
    filters = listOf(fieldAccess(definingClass = SETTINGS, name = DAILY_USAGE_PING_DELEGATE, opcode = Opcode.IPUT_OBJECT)),
)

// Nimbus.fetchExperiments() (application-services, kept names): launches the download of the
// experiment recipes from Remote Settings. Its only caller is Focus's finishNimbusInitialization.
// The AC subclass mozilla.components.service.nimbus.Nimbus does not override it.
internal object NimbusFetchExperimentsFingerprint : Fingerprint(
    definingClass = NIMBUS,
    name = "fetchExperiments",
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)
