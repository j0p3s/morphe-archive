import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "com.shazam.android"
private const val FIREBASE_ANALYTICS = "Lcom/google/firebase/analytics/FirebaseAnalytics;"
private const val CRASHLYTICS = "Lcom/google/firebase/crashlytics/FirebaseCrashlytics;"
private const val OPEN_IN_EXTENSION = "Lapp/template/extension/shazam/OpenInMorpheAppsPatch;"
private const val STREAMING_PROVIDER_TO_STRING = "StreamingProvider(streamingProviderId="
private const val STRING = "Ljava/lang/String;"

class ShazamSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun loadClasses(): List<ClassDef> {
        val root = repoRoot()
        val apk = File(root, "apks/shazam/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        return applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "16.63.0-261008",
            patchNames = setOf("Disable telemetry", "Open in Morphe YouTube apps"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
    }

    @Test
    fun `Shazam patches neuter telemetry and remap Open in YouTube providers`() {
        val classes = loadClasses()
        val byType = classes.associateBy { it.type }

        // --- Disable telemetry ---

        // Firebase Analytics event logging.
        val analytics = byType[FIREBASE_ANALYTICS]
            ?: error("$FIREBASE_ANALYTICS not found in emitted dexes")
        val logEvent = analytics.method("logEvent", listOf("Ljava/lang/String;", "Landroid/os/Bundle;"))
        assertReturnsEarlyVoid(logEvent, label = "FirebaseAnalytics.logEvent()")

        // Crashlytics collection flag forced off.
        val crashlytics = byType[CRASHLYTICS]
            ?: error("$CRASHLYTICS not found in emitted dexes")
        assertForcedBoolean(
            crashlytics.method("isCrashlyticsCollectionEnabled", emptyList()),
            expected = false,
            label = "FirebaseCrashlytics.isCrashlyticsCollectionEnabled()",
        )

        // Every concrete recordException/log overload neutered.
        val reporting = crashlytics.methods.filter { method ->
            method.implementation != null &&
                (method.name == "recordException" || method.name == "log")
        }
        check(reporting.isNotEmpty()) { "no recordException/log methods found on $CRASHLYTICS" }
        for (method in reporting) {
            val first = method.instructions().firstOrNull()
            check(first?.opcode == Opcode.RETURN_VOID) {
                "${crashlytics.type}.${method.name} not neutered; first instruction is ${first?.opcode}"
            }
        }

        // --- Open in Morphe YouTube apps ---

        // The extension class is merged into the emitted dex, with the option defaults injected.
        val extension = byType[OPEN_IN_EXTENSION]
            ?: error("$OPEN_IN_EXTENSION (shazam extension) not merged into the emitted dexes")
        val remap = extension.method("remap", listOf(STRING))
        check(remap.returnType == STRING && remap.implementation != null) {
            "$OPEN_IN_EXTENSION.remap(String) has the wrong shape"
        }
        for ((getter, expected) in listOf(
            "getYouTubeMusicPackage" to "app.morphe.android.apps.youtube.music",
            "getYouTubePackage" to "app.morphe.android.youtube",
        )) {
            val insns = extension.method(getter, emptyList()).instructions()
            val value = ((insns.getOrNull(0) as? ReferenceInstruction)?.reference as? StringReference)?.string
            check(insns.getOrNull(0)?.opcode == Opcode.CONST_STRING && value == expected) {
                "$getter() does not return the injected option value; got ${insns.getOrNull(0)?.opcode} $value"
            }
            check(insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT) {
                "$getter() does not return immediately; second instruction is ${insns.getOrNull(1)?.opcode}"
            }
        }

        // StreamingProvider (R8-renamed) is found the same way the patch finds it: by its toString.
        val provider = classes.singleOrNull { classDef ->
            classDef.methods.any { method ->
                method.instructions().any { insn ->
                    ((insn as? ReferenceInstruction)?.reference as? StringReference)?.string ==
                        STREAMING_PROVIDER_TO_STRING
                }
            }
        } ?: error("no single class contains \"$STREAMING_PROVIDER_TO_STRING\"")

        val constructor = provider.methods.singleOrNull { method ->
            method.name == "<init>" &&
                method.parameterTypes.map(CharSequence::toString).let { params ->
                    params.size == 7 &&
                        params[0].startsWith("L") &&
                        params.subList(1, 6).all { it == STRING } &&
                        params[6] == "Z"
                }
        } ?: error("${provider.type}.<init>(obj, String x5, Z) not found")

        // Parameters are all single-width, so p0 = this sits at registerCount - 8.
        val firstParamRegister = constructor.implementation!!.registerCount - 8
        val insns = constructor.instructions()
        for ((slot, p) in (3..6).withIndex()) {
            val register = firstParamRegister + p
            val invoke = insns.getOrNull(slot * 2)
            val call = (invoke as? ReferenceInstruction)?.reference as? MethodReference
            val invokedRegister = when (invoke) {
                is RegisterRangeInstruction -> invoke.startRegister.takeIf { invoke.registerCount == 1 }
                is FiveRegisterInstruction -> invoke.registerC.takeIf { invoke.registerCount == 1 }
                else -> null
            }
            check(
                (invoke?.opcode == Opcode.INVOKE_STATIC || invoke?.opcode == Opcode.INVOKE_STATIC_RANGE) &&
                    call?.definingClass == OPEN_IN_EXTENSION && call.name == "remap" &&
                    invokedRegister == register
            ) {
                "${provider.type}.<init> instruction ${slot * 2} is not remap(p$p); got ${invoke?.opcode} $call"
            }
            val move = insns.getOrNull(slot * 2 + 1)
            check(move?.opcode == Opcode.MOVE_RESULT_OBJECT && (move as OneRegisterInstruction).registerA == register) {
                "${provider.type}.<init> does not store remap() back into p$p; got ${move?.opcode}"
            }
        }

        // All four remaps happen before the super constructor and the field stores.
        val superCall = insns.indexOfFirst { insn ->
            insn.opcode == Opcode.INVOKE_DIRECT &&
                ((insn as ReferenceInstruction).reference as? MethodReference)?.let {
                    it.definingClass == "Ljava/lang/Object;" && it.name == "<init>"
                } == true
        }
        check(superCall >= 8) { "${provider.type}.<init> calls Object.<init> at $superCall, before the remaps" }

        // Without the <queries> entries the Morphe apps are invisible to getApplicationInfo on
        // Android 11+, so remap() would silently keep the stock packages.
        val manifest = workDir.walkTopDown()
            .filter { it.name == "AndroidManifest.xml" }
            .map { it.readText() }
            .firstOrNull { it.contains("<queries") }
            ?: error("no decoded AndroidManifest.xml with <queries> under $workDir")
        for (pkg in listOf("app.morphe.android.apps.youtube.music", "app.morphe.android.youtube")) {
            check(manifest.contains("<package android:name=\"$pkg\"")) { "<queries> is missing $pkg" }
        }
        check(manifest.contains("<package android:name=\"com.google.android.apps.youtube.music\"")) {
            "<queries> lost the original stock YouTube Music entry"
        }
    }
}
