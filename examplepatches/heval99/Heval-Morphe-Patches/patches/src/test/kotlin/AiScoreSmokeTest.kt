import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

private const val PKG = "com.onesports.score"
private const val USER_PREFERENCE = "Lcom/onesports/score/base/preference/UserPreference;"
private const val PACKAGE_MANAGER = "Landroid/content/pm/PackageManager;"
private const val SIGNATURE_EXTENSION = "Lapp/template/extension/signature/SignatureSpoof;"
private const val SIGNATURE_SHA1 = "d34738b4cf2d39aad6e63e032323d78b0af5a84c"

class AiScoreSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Method.sameClassNoArgCalls(returnType: String) = instructions()
        .filter { it.opcode == Opcode.INVOKE_VIRTUAL }
        .mapNotNull { (it as? ReferenceInstruction)?.reference as? MethodReference }
        .filter { it.definingClass == USER_PREFERENCE && it.parameterTypes.isEmpty() && it.returnType == returnType }

    private fun Method.calls() = instructions().mapNotNull { (it as? ReferenceInstruction)?.reference as? MethodReference }

    @Test
    fun `Enable Premium forces the cached VIP gate and level, Spoof signature routes getPackageInfo`() {
        val root = repoRoot()
        val apk = File(root, "apks/aiscore/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        // Locate the targets in the unpatched dex the same way the patches do.
        val originalClasses: List<ClassDef> = ZipFile(apk).use { zip ->
            zip.entries().asSequence()
                .filter { Regex("classes\\d*\\.dex").matches(it.name) }
                .flatMap { entry ->
                    val bytes = zip.getInputStream(entry).use { it.readBytes() }
                    DexBackedDexFile(Opcodes.forApi(36), ByteBuffer.wrap(bytes)).classes.asSequence()
                }
                .toList()
        }
        val original = originalClasses.single { it.type == USER_PREFERENCE }
        val gate = original.methods.single {
            it.returnType == "Z" && it.parameterTypes.isEmpty() &&
                it.sameClassNoArgCalls("Z").isNotEmpty() && it.sameClassNoArgCalls("I").isNotEmpty()
        }
        val levelName = gate.sameClassNoArgCalls("I").single().name

        // Firebase's PackageManagerWrapper (R8-renamed, its (String, int) parameters may be
        // reordered): returns PackageInfo, takes a String and an int, and calls
        // Context.getPackageManager() then PackageManager.getPackageInfo(String, int).
        // AndroidUtilsLight reads the X-Android-Cert hash through it.
        val wrappers = originalClasses.flatMap { cls ->
            cls.methods.filter { m ->
                m.returnType == "Landroid/content/pm/PackageInfo;" &&
                    m.parameterTypes.map(CharSequence::toString).sorted() == listOf("I", "Ljava/lang/String;") &&
                    m.calls().any { it.definingClass == "Landroid/content/Context;" && it.name == "getPackageManager" } &&
                    m.calls().any {
                        it.definingClass == PACKAGE_MANAGER && it.name == "getPackageInfo" &&
                            it.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/String;", "I")
                    }
            }.map { cls.type to it }
        }
        check(wrappers.size == 1) { "expected one PackageManagerWrapper.getPackageInfo, found ${wrappers.map { (t, m) -> "$t.${m.name}" }}" }
        val (wrapperType, wrapper) = wrappers.single()
        val wrapperParameters = wrapper.parameterTypes.map(CharSequence::toString)

        val patched = applyPatches(
            apk = apk, workDir = workDir, pkg = PKG, version = "4.3.2",
            patchNames = setOf("Enable Premium", "Spoof signature"), allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        val byType = patched.associateBy { it.type }

        // Enable Premium.
        val userPreference = byType[USER_PREFERENCE] ?: error("$USER_PREFERENCE not found")
        assertForcedBoolean(userPreference.method(gate.name, emptyList()), expected = true, label = "UserPreference.${gate.name}() (VIP gate)")
        assertReturnsInt(userPreference.method(levelName, emptyList()), expected = 1, label = "UserPreference.$levelName() (VIP level)")

        // Spoof signature: the extension carries com.onesports.score and the onescore certificate,
        // and no direct PackageManager.getPackageInfo(String, ...) call is left.
        assertSpoofSignature(patched, PKG, SIGNATURE_SHA1)

        // The wrapper Firebase Installations uses for X-Android-Cert now calls the extension.
        val patchedWrapper = (byType[wrapperType] ?: error("$wrapperType not found")).method(wrapper.name, wrapperParameters)
        check(
            patchedWrapper.calls().any {
                it.definingClass == SIGNATURE_EXTENSION && it.name == "getPackageInfo" &&
                    it.parameterTypes.map(CharSequence::toString) == listOf(PACKAGE_MANAGER, "Ljava/lang/String;", "I")
            }
        ) { "$wrapperType.${wrapper.name} (PackageManagerWrapper.getPackageInfo) does not call $SIGNATURE_EXTENSION.getPackageInfo" }
        check(patchedWrapper.calls().none { it.definingClass == PACKAGE_MANAGER && it.name == "getPackageInfo" }) {
            "$wrapperType.${wrapper.name} still calls PackageManager.getPackageInfo directly"
        }
    }
}
