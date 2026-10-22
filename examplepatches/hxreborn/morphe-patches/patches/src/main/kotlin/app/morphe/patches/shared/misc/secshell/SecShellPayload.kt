/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.shared.misc.secshell

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import com.android.tools.smali.dexlib2.Opcodes
import com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile

private const val PAYLOAD_JAR = "assets/classes0.jar"
private const val SHELL_DEX = "classes.dex"
private const val EXTENSION_DEX = "extensions/extension.mpe"

internal interface SecShellContainer {
    fun dexEntries(): List<String>

    fun read(name: String): ByteArray

    fun withEntries(replacements: Map<String, ByteArray>): ByteArray
}

internal class SecShellPayload(private val container: SecShellContainer) {
    private val editors = LinkedHashMap<String, SecShellDex>()

    fun dexContaining(classType: String) = singleDex(classType) { it.hasClass(classType) }

    fun replaceBody(classType: String, name: String, registers: Int, smali: String) =
        editor(dexContaining(classType)).replaceBody(classType, name, registers, smali)

    fun method(classType: String, name: String) = editor(dexContaining(classType)).method(classType, name)

    fun prependToMethodUsingString(string: String, firstParameter: String, smali: String): String {
        val entry = singleDex("\"$string\"") { it.hasString(string) }
        val method = editor(entry).methodUsingString(string, firstParameter)
        method.addInstructions(0, smali)
        return method.definingClass
    }

    fun addExtensionPackage(packageType: String, besideClass: String) {
        val extension = SecShellPayload::class.java.classLoader.getResourceAsStream(EXTENSION_DEX)
            ?.use { DexBackedDexFile.fromInputStream(Opcodes.getDefault(), it.buffered()) }
            ?: throw PatchException("$EXTENSION_DEX is missing from the patch bundle")
        val classes = extension.classes.filter { it.type.startsWith(packageType) }
        if (classes.isEmpty()) throw PatchException("$EXTENSION_DEX has no class in $packageType")

        val editor = editor(dexContaining(besideClass))
        classes.forEach(editor::addClass)
    }

    fun sealed(): ByteArray =
        container.withEntries(editors.filterValues { it.isEdited }.mapValues { it.value.toBytes() })

    private fun singleDex(subject: String, holds: (SecShellDex) -> Boolean): String {
        val owners = container.dexEntries().filter { holds(editor(it)) }
        return owners.singleOrNull()
            ?: throw PatchException("Expected $subject in one payload dex, found ${owners.size}")
    }

    private fun editor(entry: String) = editors.getOrPut(entry) { SecShellDex(container.read(entry)) }
}

internal fun ResourcePatchContext.editSecShellPayload(block: SecShellPayload.() -> Unit) {
    val jar = get(PAYLOAD_JAR)
    val file = if (jar.exists()) jar else get(SHELL_DEX)
    val bytes = file.readBytes()
    val container = when {
        jar.exists() -> SecShellJar.of(bytes)
        SecShellEmbeddedDex.isEmbedded(bytes) -> SecShellEmbeddedDex(bytes)
        else -> throw PatchException("Neither $PAYLOAD_JAR nor a payload in $SHELL_DEX, this is not a SecShell build")
    }

    val payload = SecShellPayload(container)
    payload.block()
    file.writeBytes(payload.sealed())
}
