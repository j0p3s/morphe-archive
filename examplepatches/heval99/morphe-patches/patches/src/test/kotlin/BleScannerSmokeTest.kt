import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

private const val PKG = "com.macdom.ble.blescanner"
private const val BANNER_UNIT = "ca-app-pub-9113721270828121/8819258755"
private const val INTERSTITIAL_UNIT = "ca-app-pub-9113721270828121/2787645901"
private const val CONTEXT = "Landroid/content/Context;"
private const val RELATIVE_LAYOUT = "Landroid/widget/RelativeLayout;"

class BleScannerSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Instruction.methodRef(): MethodReference? =
        (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun Instruction.stringRef(): String? =
        ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

    private fun Method.params(): List<String> = parameterTypes.map(CharSequence::toString)

    private fun Method.label() = "$definingClass->$name(${params().joinToString("")})"

    /** The ad helper, found like the patch does: the class whose no-arg String getter returns [unit]. */
    private fun List<ClassDef>.byLiveUnit(unit: String): ClassDef = singleOrNull { classDef ->
        classDef.methods.any { method ->
            method.returnType == "Ljava/lang/String;" && method.parameterTypes.isEmpty() &&
                method.instructions().any { it.stringRef() == unit }
        }
    } ?: error("no single class returns the live unit $unit")

    /** The unpatched classes, read straight from the APK's dex files. */
    private fun originalClasses(apk: File): List<ClassDef> = ZipFile(apk).use { zip ->
        zip.entries().toList()
            .filter { Regex("classes\\d*\\.dex").matches(it.name) }
            .flatMap { entry ->
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                DexBackedDexFile(Opcodes.forApi(36), ByteBuffer.wrap(bytes)).classes.toList()
            }
    }

    @Test
    fun `Disable ads hides the banners and stops the interstitial loads but keeps the show paths`() {
        val root = repoRoot()
        val apk = File(root, "apks/blescanner/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "4.8",
            patchNames = setOf("Disable ads"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        val original = originalClasses(apk).associateBy { it.type }

        // 1. AdmobBanner.loadBanner(Context, RelativeLayout) starts with const/16 8,
        // setVisibility(GONE) on the container (p2), return-void.
        val banner = classes.byLiveUnit(BANNER_UNIT)
        val loadBanner = banner.methods.singleOrNull {
            it.returnType == "V" && it.params() == listOf(CONTEXT, RELATIVE_LAYOUT)
        } ?: error("${banner.type} has no single (Context, RelativeLayout)V loader")
        val insns = loadBanner.instructions()
        val gone = insns.getOrNull(0)
        val hide = insns.getOrNull(1)
        val hideRef = hide?.methodRef()
        assertTrue(
            gone?.opcode == Opcode.CONST_16 && (gone as NarrowLiteralInstruction).narrowLiteral == 8,
            "${loadBanner.label()} does not start with const/16 0x8; found ${gone?.opcode}"
        )
        // p2 (the container) is the last register.
        val containerRegister = loadBanner.implementation!!.registerCount - 1
        assertTrue(
            hide?.opcode == Opcode.INVOKE_VIRTUAL &&
                hideRef?.definingClass == RELATIVE_LAYOUT && hideRef.name == "setVisibility" &&
                (hide as FiveRegisterInstruction).registerC == containerRegister &&
                hide.registerD == (gone as OneRegisterInstruction).registerA &&
                gone.registerA != containerRegister,
            "${loadBanner.label()} does not call container.setVisibility(GONE); found ${hide?.opcode} $hideRef"
        )
        assertTrue(
            insns.getOrNull(2)?.opcode == Opcode.RETURN_VOID,
            "${loadBanner.label()} does not return after hiding; third instruction is ${insns.getOrNull(2)?.opcode}"
        )

        // 2. AdmobInterstitial.loadInterstitialAds(Context) returns at once.
        val interstitial = classes.byLiveUnit(INTERSTITIAL_UNIT)
        val loadInterstitial = interstitial.methods.singleOrNull {
            it.returnType == "V" && it.params() == listOf(CONTEXT)
        } ?: error("${interstitial.type} has no single (Context)V loader")
        assertReturnsEarlyVoid(loadInterstitial, label = loadInterstitial.label())

        // 3. HomeActivity's no-arg caller of the loader (4.8: loadInterADS) returns at once.
        val homeActivity = classes.singleOrNull { it.type.endsWith("/HomeActivity;") }
            ?: error("HomeActivity not found in emitted dexes")
        val loadInterAds = homeActivity.methods.singleOrNull { method ->
            method.returnType == "V" && method.parameterTypes.isEmpty() &&
                method.instructions().any {
                    val ref = it.methodRef()
                    ref?.definingClass == interstitial.type && ref.name == loadInterstitial.name
                }
        } ?: error("${homeActivity.type} has no single no-arg caller of ${loadInterstitial.label()}")
        assertReturnsEarlyVoid(loadInterAds, label = loadInterAds.label())

        // The show paths are byte-for-byte stock: AdmobInterstitial's (Activity, String) pair
        // (showInterstitialAds and its no-ad fallback gotoNext) and HomeActivity's
        // showAd/showAdForHelp/showAdHistoryData fall through to the feature when no ad is
        // loaded, so they must keep running.
        val showPaths = interstitial.methods.filter {
            it.returnType == "V" && it.params() == listOf("Landroid/app/Activity;", "Ljava/lang/String;")
        } + homeActivity.methods.filter { method ->
            method.instructions().any {
                val ref = it.methodRef()
                ref?.definingClass == interstitial.type && ref.params() == listOf("Landroid/app/Activity;", "Ljava/lang/String;")
            }
        }
        assertTrue(
            showPaths.size == 5 && showPaths.any { it.name == "showInterstitialAds" },
            "expected showInterstitialAds, gotoNext and 3 HomeActivity show paths; found ${showPaths.map { it.label() }}"
        )
        for (method in showPaths) {
            val stock = original[method.definingClass]?.methods?.singleOrNull {
                it.name == method.name && it.params() == method.params()
            } ?: error("${method.label()} not in the stock APK")
            assertTrue(
                stock.instructions().map { it.opcode } == method.instructions().map { it.opcode },
                "${method.label()} changed; the show paths must stay untouched"
            )
        }
    }

    private fun MethodReference.params(): List<String> = parameterTypes.map(CharSequence::toString)
}
