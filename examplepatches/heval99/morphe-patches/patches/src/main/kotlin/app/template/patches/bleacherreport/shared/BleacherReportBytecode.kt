package app.template.patches.bleacherreport.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.Match
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.string
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.addInstructionsChecked
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// The app (com.bleacherreport.phoenix) and its Kotlin runtime are R8-renamed, but R8 kept the
// .source file names and the data class toString() strings. Classes are found through those and
// method shape; the obfuscated names in comments are the 11.12.0.33 ones, never anchors.

internal fun Instruction.stringRef() = ((this as? ReferenceInstruction)?.reference as? StringReference)?.string
internal fun Instruction.fieldRef() = (this as? ReferenceInstruction)?.reference as? FieldReference
internal fun Instruction.methodRef() = (this as? ReferenceInstruction)?.reference as? MethodReference

internal fun Method.insns(): List<Instruction> = implementation?.instructions?.toList().orEmpty()
internal fun Method.params(): List<String> = parameterTypes.map(CharSequence::toString)
internal fun Method.isStatic() = AccessFlags.STATIC.isSet(accessFlags)

internal fun FieldReference.smali() = "$definingClass->$name:$type"
internal fun MethodReference.smali() = "$definingClass->$name(${parameterTypes.joinToString("")})$returnType"

/** The fingerprint's single match, or a loud failure naming every match. */
internal fun BytecodePatchContext.single(fingerprint: Fingerprint, label: String): Match {
    val matches = fingerprint.matchAllOrNull().orEmpty()
    return matches.singleOrNull() ?: throw PatchException(
        "Expected one $label, found " + matches.map { "${it.classDef.type}->${it.method.name}" },
    )
}

/**
 * kotlin.Unit as [this] method already returns it: the `sget-object vX, U->f:U` read right before
 * `return-object vX`. R8 renamed Unit (11.12.0.33: Lkotlin/j0;->a), so it is copied, not hard-coded.
 */
internal fun Method.unitField(): FieldReference {
    val insns = insns()
    val units = insns.indices.mapNotNull { i ->
        val field = insns[i].fieldRef()?.takeIf {
            insns[i].opcode == Opcode.SGET_OBJECT && it.type == it.definingClass && it.definingClass.startsWith("Lkotlin/")
        } ?: return@mapNotNull null
        val next = insns.getOrNull(i + 1)
        if (next?.opcode == Opcode.RETURN_OBJECT &&
            (next as OneRegisterInstruction).registerA == (insns[i] as OneRegisterInstruction).registerA
        ) field else null
    }.distinctBy { it.smali() }
    return units.singleOrNull()
        ?: throw PatchException("$definingClass->$name: expected one returned kotlin.Unit, found ${units.map { it.smali() }}")
}

/** `return Unit` at entry, with Unit copied from the method's own return. */
internal fun MutableMethod.returnUnit() {
    val unit = unitField()
    addInstructionsChecked(
        0,
        """
            sget-object v0, ${unit.smali()}
            return-object v0
        """,
    )
}

/** `return null` at entry. v0 always exists here: every hooked method takes a parameter. */
internal fun MutableMethod.returnNull() = addInstructionsChecked(
    0,
    """
        const/4 v0, 0x0
        return-object v0
    """,
)

/**
 * The Result.failure(Throwable) call the method already makes after instruction [from]: the
 * first static call taking only a Throwable. R8 renamed it (11.12.0.33: Lkotlin/t;->a), so it is
 * copied from the method.
 */
internal fun Method.resultFailureCall(from: Int = 0): MethodReference =
    insns().drop(from).firstNotNullOfOrNull { insn ->
        insn.methodRef()?.takeIf {
            insn.opcode == Opcode.INVOKE_STATIC && it.definingClass.startsWith("Lkotlin/") &&
                it.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/Throwable;") &&
                it.returnType.startsWith("L")
        }
    } ?: throw PatchException("$definingClass->$name: no Result.failure(Throwable) call after instruction $from")

// ---------------------------------------------------------------------------------------------
// App launch tasks. LaunchTaskManagerImpl runs LaunchTask subclasses (11.12.0.33: launchtaskmanager.b)
// in a dependency graph; each task's execute (a(Continuation)Object) answers an ExecutionResult.
// Each task has a config object (a LaunchTaskConfig subclass, toString "<Name>Config") whose
// <clinit> passes the task id "<Name>"; the task's <init> hands that config to the LaunchTask
// constructor. Answering ExecutionResult.Success(taskId) at once marks the task done, so the
// tasks that depend on it still run.
// ---------------------------------------------------------------------------------------------

// LaunchTask.equals(Object) casts with the Kotlin message naming the LaunchTask class, and compares
// config.taskId, which gives both fields.
private object LaunchTaskEqualsFingerprint : Fingerprint(
    name = "equals",
    returnType = "Z",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(string(".launchtaskmanager.LaunchTask", StringComparisonType.ENDS_WITH)),
)

// ExecutionResult.Success(taskId) (11.12.0.33: launchtaskmanager.a$e).
private object LaunchTaskSuccessToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(string("Success(taskId=", StringComparisonType.STARTS_WITH)),
)

private class LaunchTaskTypes(
    val base: String,
    val config: FieldReference,
    val taskId: FieldReference,
    val execute: Method,
    val success: String,
)

private fun BytecodePatchContext.launchTaskTypes(): LaunchTaskTypes {
    val equals = single(LaunchTaskEqualsFingerprint, "LaunchTask.equals")
    val base = equals.classDef
    if (!AccessFlags.ABSTRACT.isSet(base.accessFlags)) throw PatchException("LaunchTask ${base.type} is not abstract")
    val reads = equals.method.insns().filter { it.opcode == Opcode.IGET_OBJECT }.mapNotNull { it.fieldRef() }
    val config = reads.firstOrNull { it.definingClass == base.type }
        ?: throw PatchException("LaunchTask.equals reads no config field")
    val taskId = reads.firstOrNull { it.definingClass == config.type && it.type == "Ljava/lang/String;" }
        ?: throw PatchException("LaunchTask.equals reads no config.taskId")
    // The short-circuit reads both fields from the task subclass, so neither may be private.
    listOf(config, taskId).forEach { ref ->
        val field = mutableClassDefBy(ref.definingClass).fields.singleOrNull { it.name == ref.name && it.type == ref.type }
            ?: throw PatchException("LaunchTask field ${ref.definingClass}->${ref.name} not found")
        if (AccessFlags.PRIVATE.isSet(field.accessFlags)) {
            throw PatchException("LaunchTask field ${ref.definingClass}->${ref.name} is private")
        }
    }
    val execute = base.methods.singleOrNull {
        AccessFlags.ABSTRACT.isSet(it.accessFlags) && it.returnType == "Ljava/lang/Object;" && it.parameterTypes.size == 1
    } ?: throw PatchException("LaunchTask ${base.type} has no single abstract execute(Continuation)Object")
    val success = single(LaunchTaskSuccessToStringFingerprint, "ExecutionResult.Success toString").classDef
    if (success.methods.none { it.name == "<init>" && it.params() == listOf("Ljava/lang/String;") }) {
        throw PatchException("ExecutionResult.Success ${success.type} has no <init>(String)")
    }
    return LaunchTaskTypes(base.type, config, taskId, execute, success.type)
}

/** The concrete LaunchTask subclass whose config <clinit> names [taskName]. */
private fun BytecodePatchContext.launchTaskClass(types: LaunchTaskTypes, taskName: String): String {
    val configClinit = Fingerprint(
        name = "<clinit>",
        filters = listOf(string(taskName, StringComparisonType.EQUALS)),
    )
    val configs = configClinit.matchAllOrNull().orEmpty().map { it.classDef }
        .filter { it.superclass == types.config.type }
    val config = configs.singleOrNull()?.type
        ?: throw PatchException("Expected one $taskName config (a ${types.config.type}), found ${configs.map { it.type }}")

    val tasks = mutableListOf<String>()
    classDefForEach { classDef ->
        if (classDef.superclass != types.base) return@classDefForEach
        val usesConfig = classDef.methods.any { method ->
            method.name == "<init>" && method.insns().let { insns ->
                insns.any { it.opcode == Opcode.SGET_OBJECT && it.fieldRef()?.definingClass == config } &&
                    insns.any { it.opcode == Opcode.INVOKE_DIRECT && it.methodRef()?.let { ref -> ref.definingClass == types.base && ref.name == "<init>" } == true }
            }
        }
        if (usesConfig) tasks += classDef.type
    }
    return tasks.singleOrNull() ?: throw PatchException("Expected one $taskName (config $config), found $tasks")
}

/**
 * Makes the launch task [taskName] answer ExecutionResult.Success(taskId) without doing its work.
 * Returns the task class.
 */
internal fun BytecodePatchContext.shortCircuitLaunchTask(taskName: String): String {
    val types = launchTaskTypes()
    val task = launchTaskClass(types, taskName)
    val execute = mutableClassDefBy(task).methods.singleOrNull {
        it.name == types.execute.name && it.returnType == types.execute.returnType &&
            it.params() == types.execute.params() && it.implementation != null
    } ?: throw PatchException("$taskName ($task) has no execute override")

    // this.config.taskId into v0, Success(taskId) in v1. v0 and v1 always exist (this plus the
    // continuation), and overwriting either is harmless since the method returns at once.
    execute.addInstructionsChecked(
        0,
        """
            move-object/from16 v0, p0
            iget-object v0, v0, ${types.config.smali()}
            iget-object v0, v0, ${types.taskId.smali()}
            new-instance v1, ${types.success}
            invoke-direct {v1, v0}, ${types.success}-><init>(Ljava/lang/String;)V
            return-object v1
        """,
    )
    return task
}
