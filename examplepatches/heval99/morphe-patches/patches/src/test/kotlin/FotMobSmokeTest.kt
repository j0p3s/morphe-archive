import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "com.mobilefootie.wc2010"
private const val STAFF_EMAIL = "norapps.as@"
private const val NO_SUBSCRIPTION_TEXT = "No active subscriptions found"
private const val MEMBERSHIP_ACTIVITY = "Lcom/fotmob/android/feature/billing/ui/MembershipActivity;"

class FotMobSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Instruction.referencesString(value: String): Boolean {
        val ref = (this as? ReferenceInstruction)?.reference
        return ref is StringReference && ref.string == value
    }

    // Mirrors the patch: the subscription getter is the manager's only no-arg boolean method
    // that is neither the staff check (holds the staff email string) nor a constant-false stub.
    private fun Method.isSubscriptionGetter(): Boolean {
        if (parameterTypes.isNotEmpty() || returnType != "Z") return false
        val insns = implementation?.instructions?.toList() ?: return false
        val holdsStaffEmail = insns.any { it.referencesString(STAFF_EMAIL) }
        val constantFalse = insns.size <= 2 &&
            (insns.firstOrNull() as? NarrowLiteralInstruction)?.narrowLiteral?.toInt() == 0
        return !holdsStaffEmail && !constantFalse
    }

    @Test
    fun `Enable FotMob+ forces the subscription state, disables the staff bypass and shows the icon picker`() {
        val root = repoRoot()
        val apk = File(root, "apks/fotmob/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "238.17794.20261012",
            patchNames = setOf("Enable FotMob+"),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // The manager is R8-renamed every release, so find it the same way the patch does:
        // the class whose staff check contains the unique email string.
        val manager = classes.firstOrNull { cls ->
            cls.methods.any { method ->
                method.implementation?.instructions?.any { it.referencesString(STAFF_EMAIL) } == true
            }
        } ?: error("subscription manager (staff email string) not found in emitted dexes")

        val staff = manager.methods.first { method ->
            method.implementation?.instructions?.any { it.referencesString(STAFF_EMAIL) } == true
        }
        assertForcedBoolean(staff, expected = false, label = "${manager.type}.${staff.name} (staff check)")

        val getter = manager.methods.firstOrNull { it.isSubscriptionGetter() }
            ?: error("subscription getter not found in ${manager.type}")
        assertForcedBoolean(getter, expected = true, label = "${manager.type}.${getter.name} (subscription)")

        // App icons: the membership screen lambda (the only method with the NoEntitlement text)
        // now calls the icon picker in the NoEntitlement branch too, right after the Text call.
        val screen = classes.asSequence().flatMap { it.methods.asSequence() }.firstOrNull { method ->
            method.instructions().any { it.referencesString(NO_SUBSCRIPTION_TEXT) }
        } ?: error("membership screen lambda (\"$NO_SUBSCRIPTION_TEXT\") not found in emitted dexes")
        val insns = screen.instructions()
        val label = "${screen.definingClass}.${screen.name}"

        val textIndex = insns.indexOfFirst { it.referencesString(NO_SUBSCRIPTION_TEXT) }
        val textCall = (textIndex + 1 until insns.size).first { index ->
            val ref = insns[index].methodRef()
            insns[index].opcode == Opcode.INVOKE_STATIC_RANGE && ref?.returnType == "V" &&
                ref.parameterTypes.firstOrNull()?.toString() == "Ljava/lang/String;"
        }
        val zero = insns[textCall + 1]
        val added = insns[textCall + 2]
        val endGroup = insns[textCall + 3]
        val composerType = endGroup.methodRef()?.definingClass
        assertTrue(
            zero.opcode == Opcode.CONST_4 && (zero as NarrowLiteralInstruction).narrowLiteral == 0,
            "$label: Text call is not followed by const/4 0; got ${zero.opcode}"
        )
        assertTrue(
            endGroup.opcode == Opcode.INVOKE_VIRTUAL &&
                endGroup.methodRef()?.parameterTypes?.map(CharSequence::toString) == listOf("Z"),
            "$label: the NoEntitlement group no longer closes with composer.(Z)V after the picker"
        )

        // The picker is found like the patch does: the most-called MembershipActivity
        // (I, composer)V method. Three entitlement branches call it in stock 238; now four.
        val pickerCalls = insns.filter { insn ->
            val ref = insn.methodRef()
            insn.opcode == Opcode.INVOKE_VIRTUAL && ref?.definingClass == MEMBERSHIP_ACTIVITY &&
                ref.returnType == "V" &&
                ref.parameterTypes.map(CharSequence::toString) == listOf("I", composerType)
        }.groupBy { it.methodRef()!!.name }.maxBy { it.value.size }
        assertEquals(4, pickerCalls.value.size, "$label: expected 4 calls to ${pickerCalls.key}")

        val addedRef = added.methodRef()
        assertTrue(
            added.opcode == Opcode.INVOKE_VIRTUAL && addedRef?.definingClass == MEMBERSHIP_ACTIVITY &&
                addedRef.name == pickerCalls.key,
            "$label: no ${pickerCalls.key} call directly after the NoEntitlement Text call; got ${added.opcode} $addedRef"
        )
        val registers = added as FiveRegisterInstruction
        val activityReg = (pickerCalls.value.first() as FiveRegisterInstruction).registerC
        assertEquals(activityReg, registers.registerC, "$label: picker call is not on the activity register")
        assertEquals((zero as OneRegisterInstruction).registerA, registers.registerD, "$label: picker flags register")
        assertEquals((endGroup as FiveRegisterInstruction).registerC, registers.registerE, "$label: picker composer register")
    }

    private fun Instruction.methodRef(): MethodReference? =
        (this as? ReferenceInstruction)?.reference as? MethodReference
}
