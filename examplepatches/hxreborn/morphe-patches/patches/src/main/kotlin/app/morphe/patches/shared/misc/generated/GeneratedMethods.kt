/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.shared.misc.generated

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.formatter.DexFormatter
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

internal fun descriptor(method: MethodReference): String = DexFormatter.INSTANCE.getMethodDescriptor(method)

internal fun descriptor(field: FieldReference): String = DexFormatter.INSTANCE.getFieldDescriptor(field)

internal fun MutableClass.addStaticMethod(
    name: String,
    parameters: List<String>,
    returnType: String,
    registers: Int,
    smali: String,
): String {
    val method = ImmutableMethod(
        type,
        name,
        parameters.map { ImmutableMethodParameter(it, null, null) },
        returnType,
        AccessFlags.PUBLIC.value or AccessFlags.STATIC.value,
        null,
        null,
        MutableMethodImplementation(registers),
    ).toMutable().apply { addInstructionsWithLabels(0, smali) }
    methods.add(method)
    return descriptor(method)
}

internal fun MutableClass.replaceStub(name: String, registers: Int, smali: String) {
    val stub = methods.single { it.name == name }
    methods.remove(stub)
    addStaticMethod(name, stub.parameterTypes.map(CharSequence::toString), stub.returnType, registers, smali)
}

internal fun MutableClass.replaceMethod(name: String, registers: Int, smali: String) {
    val original = methods.single { it.name == name }
    methods.remove(original)
    methods.add(
        ImmutableMethod(
            type,
            name,
            original.parameters,
            original.returnType,
            original.accessFlags,
            null,
            null,
            MutableMethodImplementation(registers),
        ).toMutable().apply { addInstructionsWithLabels(0, smali) },
    )
}

internal fun MutableClass.addDefaultConstructor() {
    if (methods.any { it.name == "<init>" && it.parameterTypes.isEmpty() }) return
    methods.add(
        ImmutableMethod(
            type,
            "<init>",
            emptyList(),
            "V",
            AccessFlags.PUBLIC.value or AccessFlags.CONSTRUCTOR.value,
            null,
            null,
            MutableMethodImplementation(1),
        ).toMutable().apply {
            addInstructions(0, "invoke-direct {p0}, Ljava/lang/Object;-><init>()V\nreturn-void")
        },
    )
}
