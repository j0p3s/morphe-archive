import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.Patch
import app.morphe.patcher.patch.loadPatchesFromJar
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import java.io.File
import java.nio.ByteBuffer

/**
 * Shared helpers for the per-app unlock smoke tests. Each test applies the app's patch to the
 * APK in the gitignored `apks/` directory and asserts on the emitted bytecode.
 */
fun repoRoot(): File {
    var dir = File(System.getProperty("user.dir")).absoluteFile
    while (dir != null && !File(dir, "settings.gradle.kts").exists()) dir = dir.parentFile
    return dir ?: File(System.getProperty("user.dir")).absoluteFile
}

fun newestPatchBundle(root: File): File =
    File(root, "patches/build/libs").walkTopDown()
        .filter {
            it.isFile && it.extension == "mpp" &&
                !it.name.contains("sources") && !it.name.contains("javadoc")
        }
        .maxByOrNull { it.lastModified() }
        ?: error("no patch bundle (.mpp) found in patches/build/libs")

fun loadAllPatches(bundle: File): Set<Patch<*>> = loadPatchesFromJar(setOf(bundle))

fun Method.instructions(): List<Instruction> = implementation?.instructions?.toList().orEmpty()

fun ClassDef.method(name: String, parameters: List<String>? = null): Method =
    methods.firstOrNull { method ->
        method.name == name &&
            (parameters == null ||
                method.parameterTypes.map(CharSequence::toString) == parameters)
    } ?: error("$type.$name(${parameters?.joinToString()}) not found")

fun assertForcedBoolean(method: Method, expected: Boolean, label: String, diagnostics: String = "") {
    val insns = method.instructions()
    val first = insns.getOrNull(0)
    val second = insns.getOrNull(1)
    assertTrue(
        first is NarrowLiteralInstruction &&
            first.opcode == Opcode.CONST_4 &&
            first.narrowLiteral.toInt() == if (expected) 1 else 0,
        "$label was not forced to $expected; first instruction is ${first?.opcode}\n$diagnostics"
    )
    assertTrue(
        second != null && second.opcode == Opcode.RETURN,
        "$label does not return immediately; second instruction is ${second?.opcode}\n$diagnostics"
    )
    assertTrue(
        (second as OneRegisterInstruction).registerA ==
            (first as OneRegisterInstruction).registerA,
        "$label returns a different register than the one set\n$diagnostics"
    )
}

fun assertReturnsLicensedEnum(method: Method, label: String, diagnostics: String = "") {
    val insns = method.instructions()
    val first = insns.getOrNull(0) as? ReferenceInstruction
    val ref = first?.reference as? FieldReference
    assertTrue(
        first != null &&
            first.opcode == Opcode.SGET_OBJECT &&
            ref != null &&
            ref.definingClass == "Lcom/joaomgcd/taskerm/licensing/LicenseStatus;" &&
            ref.name == "Licensed",
        "$label does not return LicenseStatus.Licensed; first instruction is ${insns.getOrNull(0)?.opcode}\n$diagnostics"
    )
    assertTrue(
        insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT,
        "$label does not return immediately; second instruction is ${insns.getOrNull(1)?.opcode}\n$diagnostics"
    )
}

fun assertReturnsInt(method: Method, expected: Int, label: String, diagnostics: String = "") {
    val insns = method.instructions()
    val first = insns.getOrNull(0)
    assertTrue(
        first is NarrowLiteralInstruction && first.narrowLiteral.toInt() == expected,
        "$label was not forced to $expected; first instruction is ${first?.opcode}\n$diagnostics"
    )
    assertTrue(
        insns.getOrNull(1)?.opcode == Opcode.RETURN,
        "$label does not return immediately; second instruction is ${insns.getOrNull(1)?.opcode}\n$diagnostics"
    )
}

fun assertReturnsMethodCall(
    method: Method,
    targetClass: String,
    targetName: String,
    label: String,
    diagnostics: String = "",
) {
    val insns = method.instructions()
    val first = insns.getOrNull(0) as? ReferenceInstruction
    val ref = first?.reference as? MethodReference
    assertTrue(
        ref != null && ref.definingClass == targetClass && ref.name == targetName,
        "$label does not call $targetClass.$targetName first; first instruction is ${first?.opcode} ${ref}\n$diagnostics"
    )
    assertTrue(
        insns.getOrNull(2)?.opcode == Opcode.RETURN_OBJECT,
        "$label does not return the call result; third instruction is ${insns.getOrNull(2)?.opcode}\n$diagnostics"
    )
}

/** Asserts the method was replaced by an immediate `return-void`. */
fun assertReturnsEarlyVoid(method: Method, label: String, diagnostics: String = "") {
    assertTrue(
        method.instructions().firstOrNull()?.opcode == Opcode.RETURN_VOID,
        "$label does not return immediately; first instruction is ${method.instructions().firstOrNull()?.opcode}\n$diagnostics"
    )
}

/** The unobfuscated MobileAds static voids that throw (or start the SDK) without initialize(). */
val GMA_THROWING_STATICS = listOf("initialize", "setAppVolume", "setAppMuted", "openDebugMenu", "setPlugin")

/**
 * AGENTS.md rule 9: every concrete static void on Google Mobile Ads' MobileAds returns at once
 * (each of [required] must be one of them) and putPublisherFirstPartyIdEnabled, when present,
 * answers false.
 */
fun assertMobileAdsStaticsNeutralised(mobileAds: ClassDef, required: List<String> = GMA_THROWING_STATICS) {
    val staticVoids = mobileAds.methods.filter {
        AccessFlags.STATIC.isSet(it.accessFlags) && it.returnType == "V" &&
            it.implementation != null && it.name != "<clinit>"
    }
    for (name in required) {
        assertTrue(staticVoids.any { it.name == name }, "${mobileAds.type} has no concrete static void $name")
    }
    assertTrue(staticVoids.isNotEmpty(), "${mobileAds.type} has no concrete static void")
    staticVoids.forEach {
        assertReturnsEarlyVoid(it, label = "MobileAds.${it.name}(${it.parameterTypes.joinToString()})")
    }
    mobileAds.methods.filter {
        it.name == "putPublisherFirstPartyIdEnabled" && it.returnType == "Z" && it.implementation != null
    }.forEach { assertForcedBoolean(it, expected = false, label = "MobileAds.putPublisherFirstPartyIdEnabled(Z)") }
}

/**
 * Asserts every concrete void method called one of [names] on [type] returns at once, and that
 * each name has at least one (an SDK hook that matched nothing must fail the test).
 */
fun assertAllReturnEarly(classes: List<ClassDef>, type: String, vararg names: String) {
    val owner = classes.firstOrNull { it.type == type } ?: error("$type not found in emitted dexes")
    for (name in names) {
        val targets = owner.methods.filter { it.name == name && it.returnType == "V" && it.implementation != null }
        assertTrue(targets.isNotEmpty(), "$type has no concrete void $name")
        targets.forEach { assertReturnsEarlyVoid(it, label = "$type->$name(${it.parameterTypes.joinToString()})") }
    }
}

/**
 * AppLovin MAX banner views stay GONE (shared/misc/ads/MaxGmaAdHooks.kt hideMaxAdView):
 * setVisibility(I) first loads GONE into its argument, and every constructor return-void is
 * preceded by `const 0x8` + `invoke-virtual setVisibility(I)V`.
 */
fun assertMaxAdViewHidden(classes: List<ClassDef>) {
    val type = "Lcom/applovin/mediation/ads/MaxAdView;"
    val view = classes.firstOrNull { it.type == type } ?: error("$type not found in emitted dexes")

    val setVisibility = view.method("setVisibility", listOf("I"))
    val first = setVisibility.instructions().firstOrNull()
    val p1 = setVisibility.implementation!!.registerCount - 1
    assertTrue(
        first is NarrowLiteralInstruction && first.narrowLiteral == 8 &&
            (first as OneRegisterInstruction).registerA == p1,
        "MaxAdView.setVisibility does not force GONE; first instruction is ${first?.opcode}"
    )

    val constructors = view.methods.filter { it.name == "<init>" && it.implementation != null }
    assertTrue(constructors.isNotEmpty(), "MaxAdView has no constructor")
    for (constructor in constructors) {
        val insns = constructor.instructions()
        val returns = insns.withIndex().filter { it.value.opcode == Opcode.RETURN_VOID }.map { it.index }
        assertTrue(returns.isNotEmpty(), "MaxAdView constructor has no return-void")
        for (index in returns) {
            val call = (insns.getOrNull(index - 1) as? ReferenceInstruction)?.reference as? MethodReference
            val gone = insns.getOrNull(index - 2)
            assertTrue(
                call?.definingClass == type && call.name == "setVisibility" &&
                    gone is NarrowLiteralInstruction && gone.narrowLiteral == 8,
                "MaxAdView.<init>(${constructor.parameterTypes.joinToString()}) returns at $index without hiding the view"
            )
        }
    }
}

/** Asserts the method was replaced by `return FlowKt.flowOf(Boolean.TRUE)`. */
fun assertReturnsTrueFlow(method: Method, label: String, diagnostics: String = "") {
    val insns = method.instructions()
    val first = insns.getOrNull(0) as? ReferenceInstruction
    val field = first?.reference as? FieldReference
    assertTrue(
        first?.opcode == Opcode.SGET_OBJECT &&
            field?.definingClass == "Ljava/lang/Boolean;" &&
            field.name == "TRUE",
        "$label does not start with Boolean.TRUE; first instruction is ${first?.opcode} ${field}\n$diagnostics"
    )
    val second = insns.getOrNull(1) as? ReferenceInstruction
    val call = second?.reference as? MethodReference
    assertTrue(
        second?.opcode == Opcode.INVOKE_STATIC &&
            call?.definingClass == "Lkotlinx/coroutines/flow/FlowKt;" &&
            call.name == "flowOf",
        "$label does not call FlowKt.flowOf; second instruction is ${second?.opcode} ${call}\n$diagnostics"
    )
    assertTrue(
        insns.getOrNull(2)?.opcode == Opcode.MOVE_RESULT_OBJECT,
        "$label does not move the flow result; third instruction is ${insns.getOrNull(2)?.opcode}\n$diagnostics"
    )
    assertTrue(
        insns.getOrNull(3)?.opcode == Opcode.RETURN_OBJECT,
        "$label does not return the flow; fourth instruction is ${insns.getOrNull(3)?.opcode}\n$diagnostics"
    )
}

/**
 * Applies [patchNames] (filtered to patches compatible with [pkg]) to [apk] and returns every
 * class from the emitted dex files. Pass [filterByCompatibility] = false for universal patches
 * that declare no compatibility at all (they apply to any package).
 */
fun applyPatches(
    apk: File,
    workDir: File,
    pkg: String,
    version: String,
    patchNames: Set<String>,
    allPatches: Set<Patch<*>>,
    filterByCompatibility: Boolean = true,
): List<ClassDef> {
    val target = allPatches.filter { patch ->
        patch.name in patchNames &&
            (!filterByCompatibility || patch.compatibility.orEmpty().any { it.packageName == pkg })
    }.toList()

    assertTrue(
        target.map { it.name }.toSet() == patchNames,
        "expected patches $patchNames in the bundle, found ${target.map { it.name }}"
    )

    val config = PatcherConfig(apk, workDir.resolve("out"), pkg, version)

    // Patcher.invoke() only executes the patches. Patcher.get() finalizes the bytecode context
    // and emits the patched dex files - without it you just re-read the unpatched input dex.
    val result = Patcher(config).use { patcher ->
        patcher += target.toSet()
        val results = runBlocking { patcher.invoke().toList() }
        val ours = results.filter { it.patch.name in patchNames }
        assertTrue(
            ours.size == patchNames.size,
            "every patch must execute exactly once; got ${results.map { it.patch.name }}"
        )
        for (r in ours) {
            assertNull(r.exception, "patch '${r.patch.name}' failed: ${r.exception}")
        }
        patcher.get()
    }

    assertTrue(result.dexFiles.isNotEmpty(), "patcher produced no patched dex files")

    // Drain every stream up front (temp files stay open otherwise) and load from memory;
    // path-backed dex files stay locked on Windows.
    return result.dexFiles.flatMap { dexFile ->
        val bytes = dexFile.stream.use { it.readBytes() }
        DexBackedDexFile(Opcodes.forApi(36), ByteBuffer.wrap(bytes)).classes.toList()
    }
}
