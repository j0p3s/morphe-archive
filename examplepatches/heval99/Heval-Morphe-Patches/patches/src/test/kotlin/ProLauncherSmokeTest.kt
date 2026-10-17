import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "app.prolauncher"
private const val SUBSCRIBER_INFO = "Lapp/prolauncher/data/SubscriberInfo;"

class ProLauncherSmokeTest {

    @TempDir
    lateinit var workDir: File

    // Mirrors the patch: a no-arg Z method of the kept SubscriberInfo that loads isPro:Z.
    // After patching the iget-boolean stays behind the forced return, so the shape still holds.
    private fun Method.readsIsPro(): Boolean =
        returnType == "Z" && parameterTypes.isEmpty() &&
            instructions().any { instruction ->
                val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                instruction.opcode == Opcode.IGET_BOOLEAN && field != null &&
                    field.definingClass == SUBSCRIBER_INFO && field.name == "isPro" && field.type == "Z"
            }

    // Mirrors the patch: the obfuscated prefs getter is the no-arg Z method that loads the
    // "IS_PRO" key and reads it with SharedPreferences.getBoolean (found by shape, R8-proof).
    private fun Method.readsIsProPref(): Boolean {
        if (returnType != "Z" || parameterTypes.isNotEmpty()) return false
        val refs = instructions().mapNotNull { (it as? ReferenceInstruction)?.reference }
        return refs.any { it is StringReference && it.string == "IS_PRO" } &&
            refs.any {
                it is MethodReference && it.definingClass == "Landroid/content/SharedPreferences;" &&
                    it.name == "getBoolean"
            }
    }

    @Test
    fun `Enable Premium forces the IS_PRO pref getter and SubscriberInfo isPro`() {
        val root = repoRoot()
        val apk = File(root, "apks/prolauncher/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "v2.4.2",
            patchNames = setOf("Enable Premium"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // The prefs getter (IS_PRO || trial) that the Interrupt switch, the Pro menu items and
        // most feature gates read. Exactly one method has that shape.
        val prefGetters = classes.flatMap { cls -> cls.methods.filter { it.readsIsProPref() } }
        assertEquals(1, prefGetters.size, "IS_PRO pref getters: ${prefGetters.map { "${it.definingClass}->${it.name}" }}")
        prefGetters.forEach {
            assertForcedBoolean(it, expected = true, label = "${it.definingClass}->${it.name}() (IS_PRO pref)")
        }

        val subscriberInfo = classes.firstOrNull { it.type == SUBSCRIBER_INFO }
            ?: error("$SUBSCRIBER_INFO not found in emitted dexes")

        // Both readers of the field: the getter the Compose screens call, and component1().
        val readers = subscriberInfo.methods.filter { it.readsIsPro() }
        assertEquals(setOf("isPro", "component1"), readers.map { it.name }.toSet(), "isPro readers")
        readers.forEach { assertForcedBoolean(it, expected = true, label = "$SUBSCRIBER_INFO.${it.name}()") }
    }
}
