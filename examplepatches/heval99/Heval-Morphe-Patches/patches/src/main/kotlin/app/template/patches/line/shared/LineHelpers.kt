// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/shared/ServerFlag.kt: the server
// flag anchor (key string, then the default-value accessor call). The list-pair removal and the
// visibility helper are new; the references' settings toggles are not ported.

package app.template.patches.line.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.string
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.addInstructionsAtControlFlowLabel
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * One boolean server flag. LINE parses each flag as `const-string "<key>"`, then a call to the
 * flag's ()Z accessor on the default config (for the default value), then the map lookup. R8
 * renames the accessor but keeps the key, so the key finds the accessor.
 */
internal open class ServerFlagFingerprint(key: String) : Fingerprint(
    filters = listOf(
        string(key),
        methodCall(returnType = "Z", parameters = listOf(), location = MatchAfterImmediately()),
    ),
)

/**
 * Makes the accessor found by [fingerprint] return false, so every reader of the flag sees what
 * LINE sees when the server turns the flag off.
 */
internal fun BytecodePatchContext.forceServerFlagFalse(fingerprint: ServerFlagFingerprint) {
    val call = fingerprint.instructionMatches[1].instruction.getReference<MethodReference>()
        ?: throw PatchException("Server flag accessor call has no method reference")
    val accessor = mutableClassDefBy(call.definingClass).methods.single {
        it.name == call.name && it.returnType == "Z" && it.parameterTypes.isEmpty()
    }
    if (accessor.implementation == null) {
        throw PatchException("${call.definingClass}->${call.name}()Z has no body")
    }
    accessor.returnEarly(false)
}

/**
 * Removes the `sget-object <owner>-><constant>` + `add(...)` pair that puts one enum constant into
 * a list or set. The pair is found in the method's current code, so patches that edit the same
 * method work in any order. The field's own class must be [owner] when given; otherwise any
 * `sget-object` of a field named [constant] counts. Fails when there is not exactly one pair.
 */
internal fun MutableMethod.removeConstantAdd(constant: String, owner: String? = null) {
    val pairs = instructions.indices.filter { index ->
        val get = instructions[index]
        val field = get.getReference<FieldReference>()
        val add = instructions.getOrNull(index + 1)
        get.opcode == Opcode.SGET_OBJECT && field?.name == constant &&
            (owner == null || field.definingClass == owner) &&
            add?.getReference<MethodReference>()?.name == "add" &&
            (add as? FiveRegisterInstruction)?.registerD == (get as OneRegisterInstruction).registerA
    }
    if (pairs.size != 1) {
        throw PatchException("Expected one $constant + add pair in $definingClass->$name, found ${pairs.size}")
    }
    removeInstructions(pairs.single(), 2)
}

/**
 * Makes the `setVisibility(I)V` call at [index] always pass View.GONE. Branches that jump to the
 * call land on the inserted constant, so every path is covered.
 */
internal fun MutableMethod.forceGoneAt(index: Int) {
    val call = instructions.getOrNull(index)
    if (call?.getReference<MethodReference>()?.name != "setVisibility") {
        throw PatchException("Instruction $index in $definingClass->$name is not setVisibility")
    }
    val visibility = (call as? FiveRegisterInstruction)?.registerD
        ?: throw PatchException("setVisibility at $index in $definingClass->$name is not a 35c call")
    addInstructionsAtControlFlowLabel(index, "const/16 v$visibility, 0x8")
}
