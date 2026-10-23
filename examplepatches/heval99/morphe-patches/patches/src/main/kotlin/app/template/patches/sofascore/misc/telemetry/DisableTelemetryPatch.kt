package app.template.patches.sofascore.misc.telemetry

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_SOFASCORE
import app.template.patches.shared.addInstructionChecked
import app.template.patches.shared.requireNoBranchTargetAt
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// The concrete AppsFlyer implementation (AFa1tSDK in 26.10.05) subclasses the public
// AppsFlyerLib, which only declares abstract methods. These are the entry points that send
// data; the push APIs (sendPushNotificationData, ...) are left alone.
private const val APPSFLYER_LIB = "Lcom/appsflyer/AppsFlyerLib;"
private val APPSFLYER_METHODS = setOf("start", "logEvent", "logSession", "logAdRevenue")

// FirebaseCrashlytics is R8-renamed in this build (the public class no longer exists),
// so it is located at patch time by the SharedPreferences key its settings class uses.
private const val CRASHLYTICS_COLLECTION_KEY = "firebase_crashlytics_collection_enabled"

/**
 * A fingerprint can match an abstract declaration (the AppsFlyer SDK ships one per
 * logEvent overload); calling returnEarly() on a method without a body crashes the
 * patcher, so only patch methods that actually have an implementation.
 */
private fun MutableMethod?.returnEarlyIfImplemented() =
    this?.takeIf { it.implementation != null }?.returnEarly()

private fun Instruction.string(): String? =
    ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

/** The argument registers of an invoke, in order, or null for other instructions. */
private fun Instruction.arguments(): List<Int>? = when (this) {
    is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
    is FiveRegisterInstruction -> listOf(registerC, registerD, registerE, registerF, registerG).take(registerCount)
    else -> null
}

/**
 * Forces the "firebase_analytics_collection_deactivated" read to answer Boolean.TRUE, the
 * bytecode approach the Truecaller patch uses: in the single method the fingerprint matches,
 * the const-string key is passed as the first argument (after the config object) of an instance
 * call returning Boolean from a single String parameter, and the value that lands in the
 * move-result-object's register right after it is overwritten with Boolean.TRUE. Measurement
 * then never starts, so no automatic first_open / session_start / screen_view events are sent
 * either, and no AndroidManifest.xml edit re-encodes the whole resource table.
 */
private fun BytecodePatchContext.forceAnalyticsDeactivated() {
    val method = AnalyticsDeactivatedReadFingerprint.method
    val where = "${method.definingClass}->${method.name}"
    val insns = method.implementation!!.instructions.toList()

    val reads = insns.indices.filter { insns[it].string() == ANALYTICS_DEACTIVATED_KEY }
    if (reads.size != 1) {
        throw PatchException("Disable telemetry: $where holds ${reads.size} \"$ANALYTICS_DEACTIVATED_KEY\" strings")
    }
    val keyRegister = (insns[reads.single()] as OneRegisterInstruction).registerA

    // The first instruction after the const-string that uses or replaces the key register.
    val use = (reads.single() + 1 until insns.size).firstOrNull { index ->
        insns[index].arguments()?.contains(keyRegister) == true ||
            (
                insns[index].opcode.setsRegister() &&
                    (insns[index] as? OneRegisterInstruction)?.registerA == keyRegister
                )
    } ?: throw PatchException("Disable telemetry: $where never uses \"$ANALYTICS_DEACTIVATED_KEY\"")
    val call = (insns[use] as? ReferenceInstruction)?.reference as? MethodReference
    if (call == null ||
        call.returnType != "Ljava/lang/Boolean;" ||
        call.parameterTypes.map(CharSequence::toString) != listOf("Ljava/lang/String;") ||
        insns[use].arguments()?.getOrNull(1) != keyRegister
    ) {
        throw PatchException("Disable telemetry: $where does not read \"$ANALYTICS_DEACTIVATED_KEY\" through a Boolean get(String)")
    }
    if ((reads.single()..use).any { insns[it].location.labels.isNotEmpty() }) {
        throw PatchException("Disable telemetry: $where has a branch target between \"$ANALYTICS_DEACTIVATED_KEY\" and its read")
    }
    val moveResult = insns.getOrNull(use + 1)
    if (moveResult?.opcode != Opcode.MOVE_RESULT_OBJECT) {
        throw PatchException("Disable telemetry: $where does not keep the \"$ANALYTICS_DEACTIVATED_KEY\" value")
    }
    method.requireNoBranchTargetAt(use + 2, "the \"$ANALYTICS_DEACTIVATED_KEY\" overwrite")
    method.addInstructionChecked(
        use + 2,
        "sget-object v${(moveResult as OneRegisterInstruction).registerA}, " +
            "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
    )
}

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables AppsFlyer and Firebase Analytics (including its automatic events), " +
        "plus Crashlytics crash reporting."
) {
    compatibleWith(COMPATIBILITY_SOFASCORE)

    execute {
        // 1. AppsFlyer: start, every logEvent overload, logSession and logAdRevenue (the
        // Flashscore patch's approach), so the SDK never starts sending.
        var appsFlyerStart = 0
        classDefForEach { classDef ->
            if (classDef.superclass != APPSFLYER_LIB) return@classDefForEach
            mutableClassDefBy(classDef).methods
                .filter { it.name in APPSFLYER_METHODS && it.returnType == "V" && it.implementation != null }
                .forEach {
                    if (it.name == "start") appsFlyerStart++
                    it.returnEarly()
                }
        }
        if (appsFlyerStart == 0) throw PatchException("No AppsFlyerLib implementation of start() found")

        // 2. Firebase Analytics: the manifest switch "firebase_analytics_collection_deactivated"
        // reads as true (see forceAnalyticsDeactivated), and every remaining app event funnels
        // through the Google Measurement SDK entry point.
        forceAnalyticsDeactivated()
        GmsMeasurementLogEventFingerprint.method.returnEarlyIfImplemented()

        // 3. Crashlytics: two classes reference the collection-enabled pref key - the
        // settings holder (boolean getter that decides whether anything is collected)
        // and the FirebaseCrashlytics instance itself (log/recordException methods).
        // Disable collection and neuter the non-fatal reporting entry points.
        for (classDef in classDefByStrings(CRASHLYTICS_COLLECTION_KEY)) {
            val crashlytics = mutableClassDefBy(classDef)
            crashlytics.methods
                .filter { it.implementation != null }
                .filter { it.returnType == "Z" && it.parameterTypes.isEmpty() }
                .forEach { it.returnEarly(false) }
            crashlytics.methods
                .filter { it.implementation != null }
                .filter {
                    it.returnType == "V" &&
                        it.parameterTypes.size == 1 &&
                        it.parameterTypes.first().toString() == "Ljava/lang/Throwable;"
                }
                .forEach { it.returnEarly() }
        }
    }
}
