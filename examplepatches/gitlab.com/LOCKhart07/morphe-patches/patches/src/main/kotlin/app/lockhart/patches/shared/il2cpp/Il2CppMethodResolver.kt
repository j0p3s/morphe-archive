/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Finds where an IL2CPP method's native code lives in libil2cpp.so, by name, so native
 * patches don't depend on byte patterns or RVAs that move with every rebuild.
 *
 * - global-metadata.dat maps "Namespace.Type::Method" to a method token, and the type to
 *   the image (assembly) that defines it.
 * - In libil2cpp.so, each image has an Il2CppCodeGenModule whose first field points at the
 *   image name string, followed by methodPointerCount and methodPointers. The token's RID
 *   (1-based) indexes methodPointers. All of these pointers are RELATIVE relocations, so the
 *   module is found by looking for the relocation that targets the image name string.
 *
 * Only metadata version 31 (Unity 2022.x) is supported; other layouts are rejected rather
 * than misread.
 */
package app.lockhart.patches.shared.il2cpp

import app.morphe.patcher.patch.PatchException
import java.nio.ByteBuffer
import java.nio.ByteOrder

// libil2cpp.so ships in the ABI split, so a lone base APK from a split bundle has no native code.
const val MISSING_IL2CPP_MESSAGE = "This APK has no libil2cpp.so. Patch the full split bundle " +
    "(.apkm, .xapk or .apks) that includes the arm64-v8a or armeabi-v7a split."

/** [name] is matched as a prefix when [namePrefix] is set, for compiler-generated names like `<Foo>b__27_0`. */
data class Il2CppMethod(
    val typeName: String,
    val name: String,
    val parameterCount: Int,
    val namePrefix: Boolean = false,
) {
    override fun toString() = "$typeName::$name"
}

private const val METADATA_SANITY = 0xFAB11BAF.toInt()
private const val SUPPORTED_METADATA_VERSION = 31

// Indices of (offset, size) pairs in the metadata header, which starts after sanity and version.
private const val HEADER_STRINGS = 2
private const val HEADER_METHODS = 5
private const val HEADER_TYPE_DEFINITIONS = 19
private const val HEADER_IMAGES = 20

// Struct sizes for metadata version 31.
private const val METHOD_DEFINITION_SIZE = 36
private const val TYPE_DEFINITION_SIZE = 88
private const val IMAGE_DEFINITION_SIZE = 40

private fun ByteArray.littleEndian() = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)

class Il2CppMetadata(bytes: ByteArray) {
    private val buffer = bytes.littleEndian()

    init {
        if (buffer.getInt(0) != METADATA_SANITY) throw PatchException("global-metadata.dat has an unknown format")

        val version = buffer.getInt(4)
        if (version != SUPPORTED_METADATA_VERSION) {
            throw PatchException("Unsupported IL2CPP metadata version $version")
        }
    }

    private fun section(index: Int) = buffer.getInt(8 + index * 8) to buffer.getInt(12 + index * 8)

    private val stringsOffset = section(HEADER_STRINGS).first

    private fun string(index: Int): String {
        var end = stringsOffset + index
        while (buffer.get(end) != 0.toByte()) end++
        return String(buffer.array(), stringsOffset + index, end - stringsOffset - index, Charsets.UTF_8)
    }

    private fun typeName(typeIndex: Int): String {
        val offset = section(HEADER_TYPE_DEFINITIONS).first + typeIndex * TYPE_DEFINITION_SIZE
        val namespace = string(buffer.getInt(offset + 4))
        val name = string(buffer.getInt(offset))
        return if (namespace.isEmpty()) name else "$namespace.$name"
    }

    private fun imageName(typeIndex: Int): String {
        val (offset, size) = section(HEADER_IMAGES)
        for (image in offset until offset + size step IMAGE_DEFINITION_SIZE) {
            val typeStart = buffer.getInt(image + 8)
            val typeCount = buffer.getInt(image + 12)
            if (typeIndex in typeStart until typeStart + typeCount) return string(buffer.getInt(image))
        }
        throw PatchException("No image defines type index $typeIndex")
    }

    /** The image name and the 1-based index into that image's method pointers. */
    fun locate(method: Il2CppMethod): Pair<String, Int> {
        val (offset, size) = section(HEADER_METHODS)
        val matches = (offset until offset + size step METHOD_DEFINITION_SIZE).filter { definition ->
            string(buffer.getInt(definition)).let { if (method.namePrefix) it.startsWith(method.name) else it == method.name } &&
                buffer.getShort(definition + 34).toInt() == method.parameterCount &&
                typeName(buffer.getInt(definition + 4)) == method.typeName
        }

        val definition = matches.singleOrNull()
            ?: throw PatchException("Expected one $method in metadata, found ${matches.size}")

        val token = buffer.getInt(definition + 24)
        return imageName(buffer.getInt(definition + 4)) to (token and 0xFFFFFF)
    }
}

private const val PT_LOAD = 1
private const val PT_DYNAMIC = 2
private const val PF_X = 1
private const val DT_RELA = 7L
private const val DT_RELASZ = 8L
private const val DT_REL = 17L
private const val DT_RELSZ = 18L
private const val R_AARCH64_RELATIVE = 1027L
private const val R_ARM_RELATIVE = 23L

class Il2CppLibrary(bytes: ByteArray) {
    private class Segment(val address: Long, val offset: Long, val size: Long, val executable: Boolean = false)

    private val buffer = bytes.littleEndian()
    private val is64Bit = bytes[4] == 2.toByte()
    private val pointerSize = if (is64Bit) 8 else 4

    private val segments = mutableListOf<Segment>()

    // Relocated value by the address it is written to.
    private val relocations = HashMap<Long, Long>()

    private fun word(offset: Long) =
        if (is64Bit) buffer.getLong(offset.toInt()) else buffer.getInt(offset.toInt()).toLong() and 0xFFFFFFFF

    init {
        val headerOffset = word(if (is64Bit) 0x20 else 0x1C)
        val headerCount = buffer.getShort(if (is64Bit) 0x38 else 0x2C).toInt()
        val headerSize = if (is64Bit) 56 else 32
        var dynamic: Segment? = null

        for (index in 0 until headerCount) {
            val header = headerOffset + index * headerSize
            val type = buffer.getInt(header.toInt())
            val flags = buffer.getInt((header + if (is64Bit) 4 else 24).toInt())
            val segment = if (is64Bit) {
                Segment(word(header + 16), word(header + 8), word(header + 32), flags and PF_X != 0)
            } else {
                Segment(word(header + 8), word(header + 4), word(header + 16), flags and PF_X != 0)
            }

            if (type == PT_LOAD) segments += segment
            if (type == PT_DYNAMIC) dynamic = segment
        }

        val tags = HashMap<Long, Long>()
        dynamic ?: throw PatchException("libil2cpp.so has no dynamic section")
        for (entry in dynamic.offset until dynamic.offset + dynamic.size step 2L * pointerSize) {
            tags.putIfAbsent(word(entry), word(entry + pointerSize))
        }

        if (is64Bit) {
            val table = fileOffset(tags[DT_RELA] ?: throw PatchException("libil2cpp.so has no RELA table"))
            for (entry in table until table + tags.getValue(DT_RELASZ) step 24) {
                if (word(entry + 8) == R_AARCH64_RELATIVE) relocations[word(entry)] = word(entry + 16)
            }
        } else {
            // REL entries keep their addend in place.
            val table = fileOffset(tags[DT_REL] ?: throw PatchException("libil2cpp.so has no REL table"))
            for (entry in table until table + tags.getValue(DT_RELSZ) step 8) {
                if (word(entry + 4) and 0xFF == R_ARM_RELATIVE) {
                    val address = word(entry)
                    relocations[address] = word(fileOffset(address))
                }
            }
        }
    }

    fun fileOffset(address: Long): Long {
        val segment = segments.firstOrNull { address >= it.address && address < it.address + it.size }
            ?: throw PatchException("Address 0x${address.toString(16)} is outside libil2cpp.so")
        return address - segment.address + segment.offset
    }

    // File offsets of every relocated pointer into code: method pointers, delegates and vtables.
    private val codeStarts by lazy {
        val code = segments.filter { it.executable }
        relocations.values
            .map { it and if (is64Bit) -1L else -2L }
            .filter { target -> code.any { target >= it.address && target < it.address + it.size } }
            .map(::fileOffset)
            .toSortedSet()
    }

    /** Conservative end of the function at [fileOffset]: the next known code entry point after it. */
    fun functionEnd(fileOffset: Long): Long = codeStarts.tailSet(fileOffset + 1).firstOrNull()
        ?: throw PatchException("No code follows offset 0x${fileOffset.toString(16)}")

    private fun address(fileOffset: Long): Long {
        val segment = segments.first { fileOffset >= it.offset && fileOffset < it.offset + it.size }
        return fileOffset - segment.offset + segment.address
    }

    private fun stringAddresses(value: String): List<Long> {
        val needle = "\u0000$value\u0000".toByteArray()
        val bytes = buffer.array()
        return (0..bytes.size - needle.size).filter { start ->
            needle.indices.all { bytes[start + it] == needle[it] }
        }.map { address(it + 1L) }
    }

    /** File offset of the native code for the [rid]th method of [image]. */
    fun methodOffset(image: String, rid: Int): Long {
        val names = stringAddresses(image).toSet()
        val modules = relocations.filter { (address, value) ->
            value in names && relocations.containsKey(address + 2 * pointerSize)
        }.keys

        val module = modules.singleOrNull()
            ?: throw PatchException("Expected one code module for $image, found ${modules.size}")

        val count = buffer.getInt(fileOffset(module + pointerSize).toInt())
        if (rid !in 1..count) throw PatchException("Method $rid is outside $image's $count methods")

        val methodPointers = relocations.getValue(module + 2 * pointerSize)
        val pointer = relocations[methodPointers + (rid - 1L) * pointerSize]
            ?: throw PatchException("Method $rid of $image has no native code")

        // The ARM stubs are ARM mode, so refuse Thumb targets.
        if (!is64Bit && pointer and 1 != 0L) throw PatchException("Method $rid of $image is Thumb code")

        return fileOffset(pointer)
    }
}

/** File offsets in [library] of each of [methods], resolved through [metadata]. */
fun Il2CppLibrary.resolve(metadata: Il2CppMetadata, methods: List<Il2CppMethod>) = methods.associateWith { method ->
    val (image, rid) = metadata.locate(method)
    methodOffset(image, rid)
}
