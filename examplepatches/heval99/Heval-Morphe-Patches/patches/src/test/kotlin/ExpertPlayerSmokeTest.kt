import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "app.expert.player"
private const val GMA = "Lcom/google/android/gms/ads/"
private const val AD_RUNNABLE = "Lio/flutter/plugins/googlemobileads/FlutterMobileAdsWrapper\$1;"
private const val PAIRIP_LICENSE = "Lcom/pairip/licensecheck/LicenseClient;"
private val GMA_FULL_SCREEN = listOf(
    "interstitial/InterstitialAd;", "admanager/AdManagerInterstitialAd;", "rewarded/RewardedAd;",
    "rewardedinterstitial/RewardedInterstitialAd;", "appopen/AppOpenAd;",
)

class ExpertPlayerSmokeTest {

    @TempDir
    lateinit var workDir: File

    @Test
    fun `Disable ads fails every plugin ad load and skips the PairIP license check`() {
        val root = repoRoot()
        val apk = File(root, "apks/expertplayer/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "11.2.0",
            patchNames = setOf("Disable ads"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // Step 1: PairIP's license check returns at once.
        val license = classes.firstOrNull { it.type == PAIRIP_LICENSE }
            ?: error("$PAIRIP_LICENSE not found in emitted dexes")

        assertReturnsEarlyVoid(
            license.method("checkLicense", listOf("Landroid/content/Context;")),
            label = "LicenseClient.checkLicense(Context)",
        )

        // Step 2: every google_mobile_ads plugin load reports NO_FILL to Dart.
        val loads = assertFlutterGmaPluginLoadsFail(classes)
        assertTrue(loads.isNotEmpty(), "no plugin load methods found")

        // Step 3: every GMA load entry point returns at once (backstop).
        assertAllReturnEarly(classes, "${GMA}BaseAdView;", "loadAd")
        assertAllReturnEarly(classes, "${GMA}admanager/AdManagerAdView;", "loadAd")
        assertAllReturnEarly(classes, "${GMA}AdLoader;", "loadAd", "loadAds")
        for (owner in GMA_FULL_SCREEN) assertAllReturnEarly(classes, GMA + owner, "load")

        // MobileAds.initialize still runs from the plugin's init thread, so the Dart init Future
        // completes through the SDK's own listener.
        val runnable = classes.firstOrNull { it.type == AD_RUNNABLE }
            ?: error("$AD_RUNNABLE not found in emitted dexes")
        val run = runnable.method("run", emptyList()).instructions()
        assertTrue(
            run.firstOrNull()?.opcode != Opcode.RETURN_VOID && run.any {
                val ref = (it as? ReferenceInstruction)?.reference as? MethodReference
                ref?.definingClass == "${GMA}MobileAds;" && ref.name == "initialize"
            },
            "FlutterMobileAdsWrapper\$1.run() must still call MobileAds.initialize",
        )
    }

    @Test
    fun `Enable Premium marks the RevenueCat entitlement active`() {
        val root = repoRoot()
        val apk = File(root, "apks/expertplayer/base.apk")

        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "11.2.0",
            patchNames = setOf("Enable Premium"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // The entitlement class is R8-renamed, so find its constructor by the RevenueCat parameter list.
        val ctor = classes.flatMap { cls ->
            cls.methods.filter {
                it.name == "<init>" && it.parameterTypes.map { p -> p.toString() } == ENTITLEMENT_CTOR_PARAMS
            }
        }.singleOrNull() ?: error("RevenueCat EntitlementInfo constructor not found")

        val first = ctor.instructions().firstOrNull()
        assertEquals(Opcode.CONST_4, first?.opcode, "isActive must be forced true before the constructor runs")
    }
}

private val ENTITLEMENT_CTOR_PARAMS = listOf(
    "Ljava/lang/String;", "Z", "Z", "LG5/q0;",
    "Ljava/util/Date;", "Ljava/util/Date;", "Ljava/util/Date;", "LG5/Y1;",
    "Ljava/lang/String;", "Ljava/lang/String;", "Z", "Ljava/util/Date;",
    "Ljava/util/Date;", "LG5/m0;", "Lorg/json/JSONObject;", "LG5/F2;",
)

