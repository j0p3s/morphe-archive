// Ported from AmpleReVanced/revanced-patches (GPL-3.0), layout/tab/DisableFriendFeedTabPatch.kt,
// written for KakaoTalk 26.7.2. Changes for 26.8.3: the FriendTab gate's identical twin getter is
// patched too, and Ample's third edit (zeroing the second argument of the friend settings
// state constructor) is dropped: in 26.8.3 that argument is isCheckedDisplayUpdatedFriends of the
// friend list settings, not a feed flag, so it would only uncheck an unrelated setting.

package app.template.patches.kakaotalk.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private fun Method.calls() = implementation?.instructions
    ?.mapNotNull { it.getReference<MethodReference>()?.toString() }.orEmpty()

@Suppress("unused")
val disableFriendFeedTabPatch = bytecodePatch(
    name = "Disable friend feed tab",
    description = "Shows the classic friend list instead of the friend feed in the first tab.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. TabConfig(isFriendFeedTabEnabled, ...): the first boolean field is cleared at the end
        // of the constructor, so slot 0 of the bottom bar resolves to FRIEND_TAB. p1 is not read
        // after its own iput.
        val constructor = TabConfigConstructorFingerprint.method
        val feedField = TabConfigConstructorFingerprint.instructionMatches[1]
            .getInstruction<ReferenceInstruction>().reference as FieldReference
        val returnIndex = constructor.instructions.lastIndex
        if (constructor.instructions[returnIndex].opcode != Opcode.RETURN_VOID) {
            throw PatchException("TabConfig constructor does not end with return-void")
        }
        constructor.addInstructions(
            returnIndex,
            """
                const/4 p1, 0x0
                iput-boolean p1, p0, ${feedField.definingClass}->${feedField.name}:${feedField.type}
            """,
        )

        // 2. The FriendTab "friend feed enabled" gate (the friend settings view model's only ()Z
        // call) returns false. Its class has a second getter with the same body, used by the
        // friend settings screens; it is patched too so both agree.
        val gate = DetermineFeedOrListFingerprint.originalMethod.implementation!!.instructions
            .mapNotNull { it.getReference<MethodReference>() }
            .singleOrNull { it.parameterTypes.isEmpty() && it.returnType == "Z" }
            ?: throw PatchException("Could not resolve the friend feed gate call")
        val gateClass = mutableClassDefBy(gate.definingClass)
        val gateMethod = gateClass.methods.singleOrNull {
            it.name == gate.name && it.parameterTypes.isEmpty() && it.returnType == "Z" && it.implementation != null
        } ?: throw PatchException("Could not resolve the friend feed gate $gate")
        val gateCalls = gateMethod.calls()
        if (gateCalls.isEmpty()) throw PatchException("Friend feed gate $gate no longer reads its flags")
        val gates = gateClass.methods.filter {
            it.parameterTypes.isEmpty() && it.returnType == "Z" && it.implementation != null && it.calls() == gateCalls
        }
        // The gate and its twin read by the friend settings screens; anything else means a new shape.
        if (gates.size > 2) throw PatchException("Expected at most 2 friend feed gates, found ${gates.size}")
        gates.forEach { it.returnEarly(false) }
    }
}
