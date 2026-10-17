import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "ru.wasiliysoft.ircodefindernec"
private const val LINEAR_LAYOUT = "Landroid/widget/LinearLayout;"
private const val BANNER_AD_VIEW = "Lcom/yandex/mobile/ads/banner/BannerAdView;"
private const val INTERSTITIAL_AD_LOADER = "Lcom/yandex/mobile/ads/interstitial/InterstitialAdLoader;"

class IrCodeFinderSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Instruction.methodRef(): MethodReference? =
        (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun Instruction.fieldRef(): FieldReference? =
        (this as? ReferenceInstruction)?.reference as? FieldReference

    private fun Instruction.stringRef(): String? =
        ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

    private fun Method.params(): List<String> = parameterTypes.map(CharSequence::toString)

    private fun Method.label() = "$definingClass->$name(${params().joinToString("")})"

    private fun Method.calls(definingClass: String, name: String) = instructions().any {
        val ref = it.methodRef()
        ref?.definingClass == definingClass && ref.name == name
    }

    @Test
    fun `Disable ads hides the banner containers and skips the interstitial load`() {
        val root = repoRoot()
        val apk = File(root, "apks/ircodefinder/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "14.17.0 build 206 InAPP",
            patchNames = setOf("Disable ads"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        val methods = classes.flatMap(ClassDef::getMethods)

        // 1. The banner helper's constructor, found like the patch does: <init>(String,
        // LinearLayout, Z, I) that builds the BannerAdView (and sizes it with BannerAdSize$a.sticky).
        val ctor = methods.singleOrNull {
            it.name == "<init>" &&
                it.params() == listOf("Ljava/lang/String;", LINEAR_LAYOUT, "Z", "I") &&
                it.calls(BANNER_AD_VIEW, "<init>") &&
                it.calls("Lcom/yandex/mobile/ads/banner/BannerAdSize\$a;", "sticky")
        } ?: error("no single (String, LinearLayout, Z, I) constructor builds a BannerAdView")
        val insns = ctor.instructions()
        val registers = ctor.implementation!!.registerCount
        val thisRegister = registers - 5
        val containerRegister = registers - 3

        // The super-constructor call stays first.
        val superCall = insns.first()
        assertTrue(
            superCall.opcode == Opcode.INVOKE_DIRECT && superCall.methodRef()?.name == "<init>" &&
                (superCall as FiveRegisterInstruction).registerC == thisRegister,
            "${ctor.label()} no longer starts with the super-constructor call; found ${superCall.opcode} ${superCall.methodRef()}"
        )

        // Right after the first LinearLayout store: const/16 8, container.setVisibility(GONE),
        // return-void.
        val store = insns.indexOfFirst {
            it.opcode == Opcode.IPUT_OBJECT && it.fieldRef()?.type == LINEAR_LAYOUT
        }
        assertTrue(store > 0, "${ctor.label()} has no LinearLayout store after the super call")
        assertTrue(
            (insns[store] as TwoRegisterInstruction).registerB == thisRegister,
            "${ctor.label()}'s LinearLayout store does not write to this"
        )
        val gone = insns.getOrNull(store + 1)
        val hide = insns.getOrNull(store + 2)
        val hideRef = hide?.methodRef()
        assertTrue(
            gone?.opcode == Opcode.CONST_16 && (gone as NarrowLiteralInstruction).narrowLiteral == 8,
            "${ctor.label()} does not load GONE after the container store; found ${gone?.opcode}"
        )
        assertTrue(
            hide?.opcode == Opcode.INVOKE_VIRTUAL &&
                hideRef?.definingClass == "Landroid/view/View;" && hideRef.name == "setVisibility" &&
                (hide as FiveRegisterInstruction).registerC == containerRegister &&
                hide.registerD == (gone as OneRegisterInstruction).registerA,
            "${ctor.label()} does not call container.setVisibility(GONE); found ${hide?.opcode} $hideRef"
        )
        assertTrue(
            insns.getOrNull(store + 3)?.opcode == Opcode.RETURN_VOID,
            "${ctor.label()} does not return after hiding the container; found ${insns.getOrNull(store + 3)?.opcode}"
        )
        // No BannerAdView is created before the early return.
        assertTrue(
            insns.take(store + 4).none { it.methodRef()?.definingClass == BANNER_AD_VIEW },
            "${ctor.label()} touches BannerAdView before returning"
        )

        // 2. kotlin.Unit, found like the patch does: the class whose toString() returns "kotlin.Unit".
        val unitClass = classes.singleOrNull { classDef ->
            classDef.methods.any { method ->
                method.name == "toString" && method.parameterTypes.isEmpty() &&
                    method.returnType == "Ljava/lang/String;" &&
                    method.instructions().any { it.stringRef() == "kotlin.Unit" }
            }
        } ?: error("no single class has toString() returning \"kotlin.Unit\"")
        val unitField = unitClass.fields.singleOrNull {
            AccessFlags.STATIC.isSet(it.accessFlags) && it.type == unitClass.type
        }

        // The interstitial loader: (?, String, J, ?)Object with the "InterstitialAdLoadResult" tag
        // that builds an InterstitialAdLoader. It returns Unit (or null) at index 0.
        val loader = methods.singleOrNull {
            it.returnType == "Ljava/lang/Object;" && it.parameterTypes.size == 4 &&
                it.params()[1] == "Ljava/lang/String;" && it.params()[2] == "J" &&
                it.instructions().any { insn -> insn.stringRef() == "InterstitialAdLoadResult" } &&
                it.calls(INTERSTITIAL_AD_LOADER, "<init>")
        } ?: error("no single interstitial loader builds an InterstitialAdLoader")
        val load = loader.instructions().getOrNull(0)
        val ret = loader.instructions().getOrNull(1)
        val loadsUnit = load?.opcode == Opcode.SGET_OBJECT && unitField != null &&
            load.fieldRef()?.definingClass == unitClass.type && load.fieldRef()?.name == unitField.name
        val loadsNull = load?.opcode == Opcode.CONST_4 && (load as NarrowLiteralInstruction).narrowLiteral == 0
        assertTrue(
            loadsUnit || loadsNull,
            "${loader.label()} does not load kotlin.Unit (${unitClass.type}) or null first; found ${load?.opcode} ${load?.fieldRef()}"
        )
        assertTrue(unitField == null || loadsUnit, "${loader.label()} fell back to null although ${unitClass.type} has a singleton")
        assertTrue(
            ret?.opcode == Opcode.RETURN_OBJECT &&
                (ret as OneRegisterInstruction).registerA == (load as OneRegisterInstruction).registerA,
            "${loader.label()} does not return the loaded value; second instruction is ${ret?.opcode}"
        )
    }
}
