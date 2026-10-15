// Ported from AmpleReVanced/revanced-patches (GPL-3.0),
// layout/subdevice/ShowSubDeviceRestrictedMessagesPatch.kt, written for KakaoTalk 26.7.2.
// Changes for 26.8.3: the cover state is returned through p0 (written first by the method itself)
// instead of a free register; the Universal check is resolved from the branch that creates the
// MobileOnlyCover state, since the first UniversalPublicData check in 26.8.3 is the lock cover.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val showMobileRestrictedMessagesPatch = bytecodePatch(
    name = "Show messages restricted to mobile",
    description = "Shows AlimTalk, Leverage and Universal messages that a sub device (tablet or PC " +
        "login) otherwise covers with \"check this message on your phone\".",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. AlimTalk and Leverage cover state getters always return NONE (no cover).
        val none = AlimtalkCoverStateFingerprint.instructionMatches.first()
            .getInstruction<ReferenceInstruction>().reference as FieldReference
        AlimtalkCoverStateFingerprint.method.returnCoverState(none)
        LeverageCoverStateFingerprint.method.returnCoverState(none)

        // 2. Universal messages: the check right before "new MobileOnlyCover(data)" reports false.
        val mobileOnlyCover = MobileOnlyCoverToStringFingerprint.originalClassDef.type
        val validator = UniversalCoverStateFingerprint.originalMethod
        val instructions = validator.implementation!!.instructions.toList()
        val coverIndex = instructions.indexOfFirst {
            it.opcode == Opcode.NEW_INSTANCE && it.getReference<TypeReference>()?.type == mobileOnlyCover
        }
        if (coverIndex < 3 ||
            instructions[coverIndex - 1].opcode != Opcode.IF_EQZ ||
            instructions[coverIndex - 2].opcode != Opcode.MOVE_RESULT
        ) {
            throw PatchException("No 'if (check()) MobileOnlyCover' branch in ${validator.definingClass}")
        }
        val check = instructions[coverIndex - 3].getReference<MethodReference>()
            ?.takeIf { it.returnType == "Z" }
            ?: throw PatchException("MobileOnlyCover branch in ${validator.definingClass} is not a boolean check")
        val checkMethod = mutableClassDefBy(check.definingClass).methods.singleOrNull {
            it.name == check.name && it.returnType == "Z" &&
                it.parameterTypes.map(CharSequence::toString) == check.parameterTypes.map(CharSequence::toString)
        } ?: throw PatchException("Could not resolve the mobile-only check $check")
        if (checkMethod.implementation == null) throw PatchException("Mobile-only check $check has no body")
        checkMethod.returnEarly(false)
    }
}

private fun MutableMethod.returnCoverState(coverState: FieldReference) {
    if (returnType != coverState.type) {
        throw PatchException("Unexpected cover state type in $definingClass->$name")
    }
    addInstructions(
        0,
        """
            sget-object p0, ${coverState.definingClass}->${coverState.name}:${coverState.type}
            return-object p0
        """,
    )
}
