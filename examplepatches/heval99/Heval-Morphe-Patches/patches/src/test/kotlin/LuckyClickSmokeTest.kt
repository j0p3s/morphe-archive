import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "com.peter890331.luckyclick.app"
private const val GMA = "Lcom/google/android/gms/ads/"
private const val AD_FREE_PERMANENT = "AD_FREE_PERMANENT"

class LuckyClickSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Instruction.methodRef() = (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun Instruction.isAdFreePermanentString() =
        (opcode == Opcode.CONST_STRING || opcode == Opcode.CONST_STRING_JUMBO) &&
            ((this as ReferenceInstruction).reference as StringReference).string == AD_FREE_PERMANENT

    private fun Instruction.isGetBoolean() = methodRef()?.let {
        it.definingClass == "Landroid/content/SharedPreferences;" && it.name == "getBoolean"
    } == true

    // Same search as the patch: getBoolean calls whose key register was last loaded with
    // "AD_FREE_PERMANENT". Returns their indices.
    private fun Method.adFreePermanentReads(): List<Int> {
        val insns = instructions()
        return insns.indices.filter { index ->
            val call = insns[index]
            if (!call.isGetBoolean()) return@filter false
            val key = (call as FiveRegisterInstruction).registerD
            insns.subList(0, index).lastOrNull {
                it.opcode.setsRegister() && it is OneRegisterInstruction && it.registerA == key
            }?.isAdFreePermanentString() == true
        }
    }

    @Test
    fun `license check, ad-free reads, purchase restore and GMA are patched`() {
        val root = repoRoot()
        val apk = File(root, "apks/luckyclick/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk, workDir = workDir, pkg = PKG, version = "3.28",
            patchNames = setOf("Disable license check", "Disable ads"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        fun cls(type: String) = classes.firstOrNull { it.type == type } ?: error("$type not found in emitted dexes")

        // Disable license check: PairIP.
        assertReturnsEarlyVoid(
            cls("Lcom/pairip/licensecheck/LicenseClient;").method("checkLicense", listOf("Landroid/content/Context;")),
            label = "LicenseClient.checkLicense(Context)",
        )

        // Disable ads, step 1: every getBoolean("AD_FREE_PERMANENT") result is overwritten with 1,
        // including MainActivity.onCreate's (the startup read that sets the ad-free state).
        val reads = classes.flatMap { c -> c.methods.flatMap { m -> m.adFreePermanentReads().map { Triple(c, m, it) } } }
        assertTrue(
            reads.any { (_, m, _) -> m.name == "onCreate" && m.parameterTypes.map(CharSequence::toString) == listOf("Landroid/os/Bundle;") },
            "no onCreate(Bundle) read of $AD_FREE_PERMANENT; reads in ${reads.map { "${it.first.type}.${it.second.name}" }}",
        )
        for ((owner, method, index) in reads) {
            val insns = method.instructions()
            val label = "${owner.type}.${method.name} getBoolean(\"$AD_FREE_PERMANENT\") at $index"
            val result = insns[index + 1]
            assertEquals(Opcode.MOVE_RESULT, result.opcode, "$label result is not moved")
            val forced = insns[index + 2]
            assertTrue(
                (forced.opcode == Opcode.CONST_4 || forced.opcode == Opcode.CONST_16) &&
                    (forced as OneRegisterInstruction).registerA == (result as OneRegisterInstruction).registerA &&
                    (forced as NarrowLiteralInstruction).narrowLiteral == 1,
                "$label is not forced to true; next is ${forced.opcode}",
            )
        }

        // Step 2: the purchase restore stores Boolean.TRUE as the ad-free state, found like the
        // fingerprint (app PurchasesResponseListener that writes AD_FREE_PERMANENT).
        val restores = classes.filter { !it.type.startsWith("Lcom/android/billingclient/") }.flatMap { c ->
            c.methods.filter { m ->
                m.name == "onQueryPurchasesResponse" &&
                    m.instructions().any { it.isAdFreePermanentString() } &&
                    m.instructions().any { it.methodRef()?.name == "putBoolean" }
            }
        }
        assertEquals(1, restores.size, "expected one purchase restore, found ${restores.size}")
        val restore = restores.single().instructions()
        val valueOf = restore.indexOfFirst {
            it.opcode == Opcode.INVOKE_STATIC && it.methodRef()?.definingClass == "Ljava/lang/Boolean;" &&
                it.methodRef()?.name == "valueOf"
        }
        assertTrue(valueOf >= 0, "purchase restore has no Boolean.valueOf")
        val boxed = (restore[valueOf + 1] as OneRegisterInstruction).registerA
        val forcedTrue = restore[valueOf + 2]
        val field = (forcedTrue as? ReferenceInstruction)?.reference as? FieldReference
        assertTrue(
            forcedTrue.opcode == Opcode.SGET_OBJECT && (forcedTrue as OneRegisterInstruction).registerA == boxed &&
                field?.definingClass == "Ljava/lang/Boolean;" && field.name == "TRUE",
            "purchase restore does not replace its ad-free Boolean with TRUE; found ${forcedTrue.opcode} $field",
        )
        assertEquals("setValue", restore[valueOf + 3].methodRef()?.name, "purchase restore no longer stores the Boolean")

        // Step 3: rule 9, every MobileAds static void is a no-op and first-party id answers false;
        // interstitial and banner loads return at once.
        assertMobileAdsStaticsNeutralised(cls("${GMA}MobileAds;"))
        assertReturnsEarlyVoid(
            cls("${GMA}interstitial/InterstitialAd;").method(
                "load",
                listOf(
                    "Landroid/content/Context;", "Ljava/lang/String;", "${GMA}AdRequest;",
                    "${GMA}interstitial/InterstitialAdLoadCallback;",
                ),
            ),
            label = "InterstitialAd.load",
        )
        assertReturnsEarlyVoid(
            cls("${GMA}BaseAdView;").method("loadAd", listOf("${GMA}AdRequest;")),
            label = "BaseAdView.loadAd(AdRequest)",
        )
    }
}
