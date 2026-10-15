package app.template.patches.truecaller.misc.telemetry

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import com.android.tools.smali.dexlib2.iface.ClassDef

private val CLEVERTAP_PUSH_OVERLOADS = setOf(
    listOf("Ljava/lang/String;"),
    listOf("Ljava/lang/String;", "Ljava/util/Map;"),
)

@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Turns off the app start tracker, CleverTap event tracking and AppsFlyer " +
        "attribution.",
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // 1. App start tracker.
        AppStartTrackerEnableTrackingFingerprint.method.returnEarly()

        // Collect first: mutating classes while iterating them is not allowed.
        val cleverTapManagers = mutableListOf<ClassDef>()
        val appsFlyerImplementations = mutableListOf<ClassDef>()
        classDefForEach { classDef ->
            if (CLEVERTAP_MANAGER in classDef.interfaces) cleverTapManagers += classDef
            if (classDef.superclass == APPSFLYER_LIB) appsFlyerImplementations += classDef
        }

        // 2. CleverTap: both event push overloads of every CleverTapManager implementation.
        // Profile updates and push registration are left alone.
        if (cleverTapManagers.isEmpty()) throw PatchException("No $CLEVERTAP_MANAGER implementation found")
        for (classDef in cleverTapManagers) {
            val pushes = mutableClassDefBy(classDef).methods.filter { method ->
                method.name == "push" && method.returnType == "V" && method.implementation != null &&
                    method.parameterTypes.map(CharSequence::toString) in CLEVERTAP_PUSH_OVERLOADS
            }
            if (pushes.size != CLEVERTAP_PUSH_OVERLOADS.size) {
                throw PatchException("${classDef.type} implements ${pushes.size} of the CleverTap push overloads")
            }
            pushes.forEach { it.returnEarly() }
        }

        // 3. AppsFlyer: every concrete start(Context, ...) overload, so the SDK never starts
        // sending. The abstract AppsFlyerLib declarations have no body and are skipped.
        if (appsFlyerImplementations.isEmpty()) throw PatchException("No $APPSFLYER_LIB subclass found")
        for (classDef in appsFlyerImplementations) {
            val starts = mutableClassDefBy(classDef).methods.filter { method ->
                method.name == "start" && method.returnType == "V" && method.implementation != null &&
                    method.parameterTypes.firstOrNull()?.toString() == "Landroid/content/Context;"
            }
            if (starts.none { it.parameterTypes.size == 1 }) {
                throw PatchException("${classDef.type} has no concrete start(Context)")
            }
            starts.forEach { it.returnEarly() }
        }
    }
}
