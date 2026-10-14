package mightymich.morphe.patches.com.teslacoilsw.launcher

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference


private val isPrimeStaticFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Loy/h2;",
            name = "h"
        ),
        methodCall(
            definingClass = "Llv/f0;",
            name = "a"
        )
    )
)


private val primeStateInitFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/SharedPreferences;"),
    filters = listOf(
        string("ro.razer.internal.api")
    )
)


private val subscriptionActiveFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("nova_billing"),
        string("subscription_active"),
        opcode(Opcode.XOR_INT_LIT8)
    )
)

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Prime",
    description = "Unlocks Nova Launcher Prime by bypassing the static (isPrime) gate. WARNING: May cause crashes.",
    default = true
) {
    compatibleWith(NovaLauncherCompatibility.NOVA_LAUNCHER)

    execute {

        isPrimeStaticFingerprint.let { fingerprint ->
            val method = fingerprint.method
                ?: throw PatchException("Could not find isPrime static gate (wu/v0.b).")
            method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }

        // ── Patch B: oy/h2.a(SharedPreferences) — runtime prime state init ──
        primeStateInitFingerprint.let { fingerprint ->
            val primeInitMethod = fingerprint.method
                ?: throw PatchException("Could not find prime state init (oy/h2.a).")

            val hFieldDesc = "Loy/h2;->h:Z"
            val hPutIndices = primeInitMethod.implementation?.instructions
                ?.mapIndexedNotNull { index, insn ->
                    if (insn.opcode != Opcode.IPUT_BOOLEAN) return@mapIndexedNotNull null
                    val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
                        ?: return@mapIndexedNotNull null
                    if ("${ref.definingClass}->${ref.name}:${ref.type}" != hFieldDesc)
                        return@mapIndexedNotNull null
                    index
                }
                ?.reversed()
                ?: emptyList()

            if (hPutIndices.isEmpty()) {
                throw PatchException(
                    "Nova Launcher: no iput-boolean on $hFieldDesc found in PrimeStateInitFingerprint method."
                )
            }

            hPutIndices.forEach { idx ->
                val valueReg = (primeInitMethod.implementation!!
                    .instructions.elementAt(idx) as TwoRegisterInstruction).registerA
                primeInitMethod.addInstructions(idx, "const/4 v$valueReg, 0x1")
            }
        }


        subscriptionActiveFingerprint.let { fingerprint ->
            val method = fingerprint.method
                ?: throw PatchException("Could not find subscription active gate (lv/c0.w).")
            method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }
    }
}
