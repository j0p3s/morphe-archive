import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "net.dinglisch.android.taskerm"
private const val LICENSE_STATUS = "Lcom/joaomgcd/taskerm/licensing/LicenseStatus;"
private const val CONTEXT = "Landroid/content/Context;"

private fun Method.strings(): List<String> = instructions().mapNotNull {
    ((it as? ReferenceInstruction)?.reference as? StringReference)?.string
}

private fun Method.params(): List<String> = parameterTypes.map(CharSequence::toString)

private fun Method.hasFlags(vararg flags: AccessFlags): Boolean =
    accessFlags == flags.fold(0) { acc, flag -> acc or flag.value }

class TaskerSmokeTest {

    @TempDir
    lateinit var workDir: File

    @Test
    fun `Enable Full Version patch forces the license status to Licensed`() {
        val root = repoRoot()
        val apk = File(root, "apks/tasker/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "6.6.20",
            patchNames = setOf("Enable Full Version"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // 1. Response mapper: found by its constructor's status labels, like the patch.
        val statusClass = classes.firstOrNull { cls ->
            cls.methods.any {
                it.name == "<init>" && it.params() == listOf("I") &&
                    it.strings().containsAll(listOf("Licensed", "Retrying", "Unlicensed"))
            }
        } ?: error("LicenseStatus response mapper not found in emitted dexes")
        val statusGetters = statusClass.methods.filter {
            it.returnType == LICENSE_STATUS && it.parameterTypes.isEmpty() && it.implementation != null
        }
        assertTrue(statusGetters.isNotEmpty(), "${statusClass.type} has no LicenseStatus getter")
        statusGetters.forEach {
            assertReturnsLicensedEnum(it, label = "${statusClass.type}.${it.name}()")
        }

        // 2. Licence manager: found by the "Checking cached only" log line.
        var licenseManager: ClassDef? = null
        val cached = classes.firstNotNullOfOrNull { cls ->
            cls.methods.firstOrNull {
                it.returnType == "Z" && it.params() == listOf(CONTEXT, "J") &&
                    it.strings().any { s -> s.startsWith("Checking cached only") }
            }?.also { licenseManager = cls }
        } ?: error("cached licence check not found in emitted dexes")
        val manager = licenseManager!!
        assertForcedBoolean(cached, expected = true, label = "${manager.type}.${cached.name}(Context, long)")

        val fresh = manager.methods.single {
            it.hasFlags(AccessFlags.PUBLIC, AccessFlags.FINAL) &&
                it.returnType == "Z" && it.params() == listOf(CONTEXT)
        }
        assertForcedBoolean(fresh, expected = true, label = "${manager.type}.${fresh.name}(Context)")

        // 3. Static facade: the public static (Context)Z that calls the (Context) overload.
        val facade = classes.asSequence().flatMap { it.methods.asSequence() }.single { method ->
            method.hasFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL) &&
                method.returnType == "Z" && method.params() == listOf(CONTEXT) &&
                method.instructions().any {
                    val ref = (it as? ReferenceInstruction)?.reference as? MethodReference
                    ref?.definingClass == manager.type && ref.name == fresh.name &&
                        ref.parameterTypes.map(CharSequence::toString) == listOf(CONTEXT)
                }
        }
        assertForcedBoolean(facade, expected = true, label = "${facade.definingClass}.${facade.name}(Context)")

        // 4. Startup check result helper: reads LicenseStatus.Retry and Licensed.
        val result = manager.methods.single { method ->
            method.hasFlags(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL) &&
                method.returnType == "Z" && method.params().size == 3 &&
                method.params().drop(1) == listOf("Z", CONTEXT) &&
                method.instructions().any {
                    val ref = (it as? ReferenceInstruction)?.reference as? FieldReference
                    ref?.definingClass == LICENSE_STATUS && ref.name == "Retry"
                }
        }
        assertForcedBoolean(result, expected = true, label = "${manager.type}.${result.name}(status, boolean, Context)")
    }
}
