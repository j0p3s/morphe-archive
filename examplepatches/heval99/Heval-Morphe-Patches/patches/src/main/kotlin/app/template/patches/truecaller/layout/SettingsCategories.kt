package app.template.patches.truecaller.layout

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.util.addInstructionsAtControlFlowLabel
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

/**
 * Hides a row of the main Settings screen (General, Calls, Premium, ..., Truecaller for Wear OS).
 * The rows are fixed views of CategoriesFragment's layout, each with its own click listener, and
 * the fragment sets a row and its divider VISIBLE or GONE from one boolean of the screen's state
 * (CategoriesSettingsState). Every constructor of that state stores false for [flag] (for example
 * "isWatchItemVisible"), so the row and its divider stay GONE and no gap is left. Each call adds
 * its own store, so row patches work together in any order.
 */
internal fun BytecodePatchContext.hideSettingsCategory(flag: String) {
    val stateClass = CategoriesSettingsStateToStringFingerprint.classDef
    val field = settingsStateField(CategoriesSettingsStateToStringFingerprint.method, stateClass.type, flag)

    var stores = 0
    for (constructor in stateClass.methods.filter { it.name == "<init>" && it.implementation != null }) {
        val instructions = constructor.instructions.toList()
        val indices = instructions.indices.filter { index ->
            val stored = instructions[index].field()
            instructions[index].opcode == Opcode.IPUT_BOOLEAN && stored?.definingClass == field.definingClass &&
                stored.name == field.name && stored.type == field.type
        }
        // From the last store backwards, so the earlier indices stay valid.
        for (index in indices.reversed()) {
            val register = (instructions[index] as TwoRegisterInstruction).registerA
            constructor.addInstructionsAtControlFlowLabel(index, "const/16 v$register, 0x0")
            stores++
        }
    }
    if (stores == 0) throw PatchException("${stateClass.type} never stores $flag")
}

private fun Instruction.field() = (this as? ReferenceInstruction)?.reference as? FieldReference

private fun Instruction.argumentRegisters(): List<Int>? = when (this) {
    is FiveRegisterInstruction -> listOf(registerC, registerD, registerE, registerF, registerG).take(registerCount)
    is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
    else -> null
}

/**
 * The state field toString() prints right after "<flag>=". R8 outlines the data class toString into
 * static StringBuilder helpers (for example s(label1, label2, label3, value1, value2)), so each
 * helper's append order is read from its code instead of assuming it from the parameter list.
 */
private fun BytecodePatchContext.settingsStateField(toString: Method, stateType: String, flag: String): FieldReference {
    val instructions = toString.instructions.toList()
    // The instruction that last wrote [register] before [index].
    fun writer(index: Int, register: Int) = instructions.subList(0, index).lastOrNull {
        it.opcode.setsRegister() && (it as? OneRegisterInstruction)?.registerA == register
    }

    val fields = mutableSetOf<FieldReference>()
    instructions.forEachIndexed { index, instruction ->
        if (instruction.opcode != Opcode.INVOKE_STATIC && instruction.opcode != Opcode.INVOKE_STATIC_RANGE) return@forEachIndexed
        val call = (instruction as ReferenceInstruction).reference as MethodReference
        val parameters = call.parameterTypes.map(CharSequence::toString)
        if (parameters.any { it == "J" || it == "D" }) return@forEachIndexed
        val arguments = instruction.argumentRegisters()?.map { writer(index, it) } ?: return@forEachIndexed
        val helper = classDefByOrNull(call.definingClass)?.methods?.firstOrNull {
            it.name == call.name && it.returnType == call.returnType &&
                it.parameterTypes.map(CharSequence::toString) == parameters && AccessFlags.STATIC.isSet(it.accessFlags)
        } ?: return@forEachIndexed

        // The arguments in the order the helper appends them, then each value after its label.
        val appended = appendOrder(helper).map { arguments[it] }
        appended.zipWithNext { label, value ->
            val text = ((label as? ReferenceInstruction)?.reference as? StringReference)?.string
            val read = value?.field()
            if (label?.opcode == Opcode.CONST_STRING && text != null && (text.endsWith("($flag=") || text.endsWith(" $flag=")) &&
                value?.opcode == Opcode.IGET_BOOLEAN && read?.definingClass == stateType
            ) {
                fields += read
            }
        }
    }
    return fields.singleOrNull() ?: throw PatchException("CategoriesSettingsState.toString prints $flag from fields $fields")
}

/** Indices of the parameters a static StringBuilder helper passes to StringBuilder(String) / append, in order. */
private fun appendOrder(helper: Method): List<Int> {
    val implementation = helper.implementation ?: return emptyList()
    val firstParameter = implementation.registerCount - helper.parameterTypes.size
    return implementation.instructions.mapNotNull { instruction ->
        val call = (instruction as? ReferenceInstruction)?.reference as? MethodReference
        if (call?.definingClass != "Ljava/lang/StringBuilder;" || (call.name != "append" && call.name != "<init>") ||
            call.parameterTypes.size != 1
        ) return@mapNotNull null
        val register = instruction.argumentRegisters()?.getOrNull(1) ?: return@mapNotNull null
        (register - firstParameter).takeIf { it in helper.parameterTypes.indices }
    }
}
