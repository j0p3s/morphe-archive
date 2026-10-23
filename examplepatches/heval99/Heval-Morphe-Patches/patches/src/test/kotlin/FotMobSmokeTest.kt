import com.android.tools.smali.dexlib2.AccessFlags
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
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.ByteBuffer
import java.util.zip.ZipFile

private const val PKG = "com.mobilefootie.wc2010"
private const val STAFF_EMAIL = "norapps.as@"
private const val NO_SUBSCRIPTION_TEXT = "No active subscriptions found"
private const val MEMBERSHIP_ACTIVITY = "Lcom/fotmob/android/feature/billing/ui/MembershipActivity;"
private const val MAIN_ACTIVITY_WRAPPER = "Lcom/fotmob/android/ui/MainActivityWrapper;"
private const val SIGNATURE_SHA1 = "5546e6c4b0c1f31b9e5a953a89a8bb6f25a2ad6f"
private const val TYPE_NO_CREDENTIAL = "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL"
private const val NO_ACCOUNT_LOG = "Did not find any existing Google accounts already used for logging"
private const val DISMISSED_LOG = "User seemingly dismissed the automatic Google sign-in prompt"

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
    fun `all FotMob patches apply and mutate the expected bytecode`() {
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
            patchNames = setOf("Enable FotMob+", "GmsCore support", "Spoof signature"),
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

        // Exactly one candidate, like the patch requires (it throws on none or several).
        val getters = manager.methods.filter { it.isSubscriptionGetter() }
        assertEquals(1, getters.size, "subscription getter candidates in ${manager.type}: ${getters.map { it.name }}")
        val getter = getters.single()
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

        // GmsCore support + Spoof signature (MicroG RE for Google sign-in, issue #64).
        assertGmsCoreSupport(classes, workDir, PKG, MAIN_ACTIVITY_WRAPPER, SIGNATURE_SHA1, expectCredentialRouting = true)
        assertSpoofSignature(classes, PKG, SIGNATURE_SHA1)

        // GmsCore support also skips the onboarding auto sign-in: right after the request reads
        // google_server_client_id, it throws NoCredentialException inside the try block whose
        // NoCredentialException handler is the stock "no account found" path.
        assertOnboardingAutoSignInSkipped(classes, originalClasses(apk))
    }

    private fun assertOnboardingAutoSignInSkipped(classes: List<ClassDef>, stock: List<ClassDef>) {
        // Both found like the patch does: the NoCredentialException(CharSequence) constructor by
        // its type string, the onboarding coroutine by its two log prefixes.
        fun noCredentialType(all: List<ClassDef>) = all.single { cls ->
            cls.methods.any { m ->
                m.name == "<init>" && AccessFlags.PUBLIC.isSet(m.accessFlags) &&
                    m.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/CharSequence;") &&
                    m.instructions().any { it.referencesString(TYPE_NO_CREDENTIAL) }
            }
        }.type
        fun Method.isOnboardingAutoSignIn() = returnType == "Ljava/lang/Object;" &&
            parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/Object;") &&
            instructions().any { it.stringStartsWith(NO_ACCOUNT_LOG) } &&
            instructions().any { it.stringStartsWith(DISMISSED_LOG) }
        val noCredential = noCredentialType(classes)
        assertEquals(noCredentialType(stock), noCredential, "NoCredentialException resolves differently in stock and patched dex")
        val method = classes.flatMap { it.methods }.single { it.isOnboardingAutoSignIn() }
        val label = "${method.definingClass}.${method.name} (onboarding auto sign-in)"
        val insns = method.instructions()

        // The inserted new-instance / invoke-direct <init>(CharSequence) / throw on one register.
        val newInstance = insns.indices.singleOrNull { i ->
            insns[i].opcode == Opcode.NEW_INSTANCE && insns[i].typeRef() == noCredential &&
                insns.getOrNull(i + 2)?.opcode == Opcode.THROW
        } ?: error("$label: no new-instance $noCredential followed by throw")
        val exceptionReg = (insns[newInstance] as OneRegisterInstruction).registerA
        val init = insns[newInstance + 1]
        val initRef = init.methodRef()
        val initRegs = init as FiveRegisterInstruction
        assertTrue(
            init.opcode == Opcode.INVOKE_DIRECT && initRef?.definingClass == noCredential && initRef.name == "<init>" &&
                initRef.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/CharSequence;") &&
                initRegs.registerCount == 2 && initRegs.registerC == exceptionReg,
            "$label: the exception is not built with $noCredential.<init>(CharSequence); got ${init.opcode} $initRef"
        )
        assertEquals(exceptionReg, (insns[newInstance + 2] as OneRegisterInstruction).registerA, "$label: throws another register")

        // Directly after Context.getString(I) + move-result-object, whose result is the message.
        val getString = insns[newInstance - 2]
        val moveResult = insns[newInstance - 1]
        val getStringRef = getString.methodRef()
        assertTrue(
            getString.opcode == Opcode.INVOKE_VIRTUAL && getStringRef?.definingClass == "Landroid/content/Context;" &&
                getStringRef.name == "getString" && getStringRef.parameterTypes.map(CharSequence::toString) == listOf("I") &&
                moveResult.opcode == Opcode.MOVE_RESULT_OBJECT,
            "$label: the throw does not follow Context.getString(I) + move-result-object; got ${getString.opcode} $getStringRef"
        )
        assertEquals((moveResult as OneRegisterInstruction).registerA, initRegs.registerD, "$label: the message is not the getString result")
        assertTrue(exceptionReg != initRegs.registerD, "$label: exception and message share v$exceptionReg")

        // The throw lies inside one try block whose first handler able to catch it is the
        // NoCredentialException one (the Exception and catch-all handlers come after it).
        val addresses = insns.runningFold(0) { address, insn -> address + insn.codeUnits }
        val throwAddress = addresses[newInstance + 2]
        val covering = method.implementation!!.tryBlocks.filter {
            throwAddress >= it.startCodeAddress && throwAddress < it.startCodeAddress + it.codeUnitCount
        }
        assertEquals(1, covering.size, "$label: try blocks covering the throw at 0x${throwAddress.toString(16)}")
        val firstCatching = covering.single().exceptionHandlers.first { handler ->
            handler.exceptionType == null || handler.exceptionType == noCredential ||
                handler.exceptionType == "Ljava/lang/Exception;" || handler.exceptionType == "Ljava/lang/Throwable;"
        }
        assertEquals(noCredential, firstCatching.exceptionType, "$label: the throw is not caught by the NoCredentialException handler first")

        // The patch adds exactly this one NoCredentialException, and every other method that reads
        // the same client id resource (the Sign in with Google buttons in SignInActivity, the
        // predictor and the profile screens) is unchanged.
        fun List<ClassDef>.noCredentialNews() = sumOf { cls ->
            cls.methods.sumOf { m -> m.instructions().count { it.opcode == Opcode.NEW_INSTANCE && it.typeRef() == noCredential } }
        }
        assertEquals(stock.noCredentialNews() + 1, classes.noCredentialNews(), "NoCredentialException new-instance count")

        val idRegister = (getString as FiveRegisterInstruction).registerD
        val clientIdRes = insns.subList(0, newInstance - 2).last { insn ->
            insn is NarrowLiteralInstruction && (insn as? OneRegisterInstruction)?.registerA == idRegister
        }.let { (it as NarrowLiteralInstruction).narrowLiteral }
        fun List<ClassDef>.clientIdReaders() = flatMap { cls -> cls.methods.map { m -> "${cls.type}->${m.name}${m.parameterTypes}" to m } }
            .filter { (_, m) ->
                !m.isOnboardingAutoSignIn() && m.instructions().any { insn ->
                    insn.opcode.name.startsWith("const") && (insn as? NarrowLiteralInstruction)?.narrowLiteral == clientIdRes
                }
            }.toMap()
        val stockReaders = stock.clientIdReaders()
        val patchedReaders = classes.clientIdReaders()
        assertEquals(stockReaders.keys, patchedReaders.keys, "methods reading the client id resource")
        assertTrue(stockReaders.size >= 3, "expected the Sign in with Google call sites to read the client id; found ${stockReaders.keys}")
        for ((key, stockMethod) in stockReaders) {
            assertEquals(
                stockMethod.instructions().map { it.opcode },
                patchedReaders.getValue(key).instructions().map { it.opcode },
                "$key (reads the client id) was changed",
            )
        }
    }

    private fun Instruction.stringStartsWith(prefix: String): Boolean =
        ((this as? ReferenceInstruction)?.reference as? StringReference)?.string?.startsWith(prefix) == true

    private fun Instruction.typeRef(): String? = ((this as? ReferenceInstruction)?.reference as? TypeReference)?.type

    /** The unpatched classes, read straight from the APK's dex files. */
    private fun originalClasses(apk: File): List<ClassDef> = ZipFile(apk).use { zip ->
        zip.entries().toList()
            .filter { Regex("classes\\d*\\.dex").matches(it.name) }
            .flatMap { entry ->
                val bytes = zip.getInputStream(entry).use { it.readBytes() }
                DexBackedDexFile(Opcodes.forApi(36), ByteBuffer.wrap(bytes)).classes.toList()
            }
    }

    private fun Instruction.methodRef(): MethodReference? =
        (this as? ReferenceInstruction)?.reference as? MethodReference
}
