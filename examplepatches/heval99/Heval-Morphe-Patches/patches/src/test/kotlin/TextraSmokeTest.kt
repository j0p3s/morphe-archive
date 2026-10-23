import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

private const val PKG = "com.textra"

class TextraSmokeTest {

    @TempDir
    lateinit var workDir: File

    /** The unpatched classes, read straight from the APK's dex files. */
    private fun originalClasses(apk: File): List<ClassDef> = ZipFile(apk).use { zip ->
        zip.entries().toList()
            .filter { Regex("classes\\d*\\.dex").matches(it.name) }
            .flatMap { entry ->
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                DexBackedDexFile(Opcodes.forApi(36), ByteBuffer.wrap(bytes)).classes.toList()
            }
    }

    // Mirrors the patch: the license preference class is the one instantiated right before the
    // "lc" key, so no obfuscated names are needed.
    private fun licenseTypes(classes: List<ClassDef>): Set<String> = classes.flatMap { cls ->
        cls.methods.flatMap { m ->
            val insns = m.instructions()
            insns.indices.filter { insns[it].opcode == Opcode.CONST_STRING && insns[it].stringOrNull() == "lc" }
                .mapNotNull { i ->
                    (i - 1 downTo maxOf(0, i - 3)).firstOrNull { insns[it].opcode == Opcode.NEW_INSTANCE }
                        ?.let { ((insns[it] as ReferenceInstruction).reference as TypeReference).type }
                }
        }
    }.toSet()

    // Mirrors the patch: the ()Z getter testing intValue() against [state] (if-nez for 0,
    // const/4 + if-ne otherwise), read on the stock class before the patch changed it.
    private fun ClassDef.getterTesting(state: Int): Method = methods.single { m ->
        if (m.returnType != "Z" || m.parameterTypes.isNotEmpty()) return@single false
        val insns = m.instructions()
        val i = insns.indexOfFirst {
            ((it as? ReferenceInstruction)?.reference as? MethodReference)?.name == "intValue"
        }
        if (i < 0) return@single false
        val next = insns.getOrNull(i + 2)
        when (next?.opcode) {
            Opcode.IF_NEZ -> state == 0
            Opcode.CONST_4 -> (next as NarrowLiteralInstruction).narrowLiteral == state &&
                insns.getOrNull(i + 3)?.opcode == Opcode.IF_NE
            else -> false
        }
    }

    @Test
    fun `Enable Pro forces the license preference to the purchased state`() {
        val root = repoRoot()
        val apk = File(root, "apks/textra/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "4.87",
            patchNames = setOf("Enable Pro"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        val stock = originalClasses(apk)
        val type = licenseTypes(stock).singleOrNull()
            ?: error("expected one license preference class for \"lc\", found ${licenseTypes(stock)}")
        val stockLicense = stock.first { it.type == type }
        val license = classes.first { it.type == type }

        assertForcedBoolean(
            license.method(stockLicense.getterTesting(1).name, emptyList()),
            expected = true,
            label = "license getter (state 1, purchased)",
        )
        assertForcedBoolean(
            license.method(stockLicense.getterTesting(0).name, emptyList()),
            expected = false,
            label = "license getter (state 0, free)",
        )
    }
}
