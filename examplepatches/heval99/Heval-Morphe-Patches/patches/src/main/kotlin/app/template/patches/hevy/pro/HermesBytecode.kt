package app.template.patches.hevy.pro

import app.morphe.patcher.patch.PatchException

/**
 * Minimal read-only view of a Hermes bytecode (HBC) file, version 96 only: the function headers
 * and the string table, which is all the Hevy patch needs. Field order, bitfields and alignment
 * follow BytecodeFileFormat.h of the matching Hermes release; every value is little-endian.
 */
internal class HermesBytecode(val bytes: ByteArray) {

    /** A function header, after following the large (overflowed) header when there is one. */
    class Function(
        val id: Int,
        /** File offset of the first bytecode instruction. */
        val offset: Int,
        val paramCount: Int,
        val bytecodeSize: Int,
        /** String id of the function name. */
        val nameId: Int,
        val frameSize: Int,
    )

    val version: Int
    val fileLength: Long
    val functions: List<Function>

    private val stringCount: Int
    private val smallStringTableStart: Int
    private val overflowStringTableStart: Int
    private val overflowStringCount: Int
    private val stringStorageStart: Int
    private val stringStorageSize: Int

    init {
        if (bytes.size < HEADER_FIELDS_END || u64(0) != MAGIC) {
            throw PatchException("Not a Hermes bytecode file (bad magic)")
        }
        version = u32(8).toInt()
        if (version != SUPPORTED_VERSION) {
            throw PatchException("Unsupported Hermes bytecode version $version (only $SUPPORTED_VERSION is supported)")
        }

        // BytecodeFileHeader: magic u64, version u32, sourceHash[20], then u32 fields in this
        // order. Only the ones used below are kept.
        var cursor = 8 + 4 + 20
        fun next(): Long = u32(cursor).also { cursor += 4 }
        fileLength = next()
        next() // globalCodeIndex
        val functionCount = next().toCount("functionCount")
        val stringKindCount = next().toCount("stringKindCount")
        val identifierCount = next().toCount("identifierCount")
        stringCount = next().toCount("stringCount")
        overflowStringCount = next().toCount("overflowStringCount")
        stringStorageSize = next().toCount("stringStorageSize")
        // bigIntCount, bigIntStorageSize, regExpCount, regExpStorageSize, arrayBufferSize,
        // objKeyBufferSize, objValueBufferSize, segmentID, cjsModuleCount, functionSourceCount,
        // debugInfoOffset.
        repeat(11) { next() }
        cursor += 1 // BytecodeOptions
        check(cursor == HEADER_FIELDS_END)

        if (fileLength != bytes.size.toLong()) {
            throw PatchException("Hermes header says $fileLength bytes, but the file has ${bytes.size}")
        }

        // The header is padded to a 32-byte boundary (0x80); the function headers follow.
        cursor = alignUp(cursor, 32)
        if (cursor.toLong() + functionCount.toLong() * SMALL_FUNCTION_HEADER_SIZE > bytes.size) {
            throw PatchException("Hermes function headers run past the end of the file")
        }
        functions =(0 until functionCount).map { id ->
            readFunction(id, cursor + id * SMALL_FUNCTION_HEADER_SIZE)
        }
        cursor += functionCount * SMALL_FUNCTION_HEADER_SIZE

        // Every following segment is aligned to 4 bytes (BYTECODE_ALIGNMENT = alignof(uint32_t)).
        cursor = alignUp(cursor, 4) + stringKindCount * 4 // StringKind::Entry, RLE count:31 kind:1
        cursor = alignUp(cursor, 4) + identifierCount * 4 // identifier hashes
        smallStringTableStart = alignUp(cursor, 4)
        cursor = smallStringTableStart + stringCount * 4 // SmallStringTableEntry
        overflowStringTableStart = alignUp(cursor, 4)
        cursor = overflowStringTableStart + overflowStringCount * 8 // OverflowStringTableEntry
        stringStorageStart = alignUp(cursor, 4)
        if (stringStorageStart.toLong() + stringStorageSize > bytes.size) {
            throw PatchException("Hermes string storage runs past the end of the file")
        }
    }

    // SmallFuncHeader, four u32 words with LSB-first bitfields:
    //   offset:25 paramCount:7 | bytecodeSizeInBytes:15 functionName:17 |
    //   infoOffset:25 frameSize:7 | environmentSize, highestReadCacheIndex,
    //   highestWriteCacheIndex, flags (u8 each).
    // flags: prohibitInvoke:2 strictMode:1 hasExceptionHandler:1 hasDebugInfo:1 overflowed:1 kind:2.
    private fun readFunction(id: Int, at: Int): Function {
        val w0 = u32(at)
        val w1 = u32(at + 4)
        val w2 = u32(at + 8)
        val flags = bytes[at + 15].toInt() and 0xFF
        val offset = w0 and 0x1FFFFFFL
        val infoOffset = w2 and 0x1FFFFFFL

        if (((flags shr 5) and 1) == 0) {
            return function(
                id,
                offset = offset,
                paramCount = w0 ushr 25,
                bytecodeSize = w1 and 0x7FFFL,
                nameId = w1 ushr 15,
                frameSize = w2 ushr 25,
            )
        }

        // Overflowed: the large FunctionHeader (all u32: offset, paramCount, bytecodeSizeInBytes,
        // functionName, infoOffset, frameSize, environmentSize, then three u8) lives at
        // (infoOffset << 16) | offset.
        val large = (infoOffset shl 16) or offset
        if (large < 0 || large + LARGE_FUNCTION_HEADER_SIZE > bytes.size) {
            throw PatchException("Hermes function #$id has its large header outside the file")
        }
        val at2 = large.toInt()
        return function(
            id,
            offset = u32(at2),
            paramCount = u32(at2 + 4),
            bytecodeSize = u32(at2 + 8),
            nameId = u32(at2 + 12),
            frameSize = u32(at2 + 20),
        )
    }

    private fun function(id: Int, offset: Long, paramCount: Long, bytecodeSize: Long, nameId: Long, frameSize: Long): Function {
        if (offset + bytecodeSize > bytes.size) {
            throw PatchException("Hermes function #$id bytecode runs past the end of the file")
        }
        return Function(id, offset.toInt(), paramCount.toInt(), bytecodeSize.toInt(), nameId.toInt(), frameSize.toInt())
    }

    /** Resolves string [id] through the small string table (and the overflow table if needed). */
    fun string(id: Int): String {
        if (id !in 0 until stringCount) throw PatchException("Hermes string id $id out of range")
        // SmallStringTableEntry: isUTF16:1 offset:23 length:8. Length 0xFF means the offset
        // indexes OverflowStringTableEntry (u32 offset, u32 length).
        val entry = u32(smallStringTableStart + id * 4)
        val utf16 = (entry and 1L) == 1L
        var offset = (entry ushr 1) and 0x7FFFFFL
        var length = entry ushr 24
        if (length == 0xFFL) {
            if (offset >= overflowStringCount) throw PatchException("Hermes string $id has a bad overflow index")
            val overflow = overflowStringTableStart + offset.toInt() * 8
            offset = u32(overflow)
            length = u32(overflow + 4)
        }
        val byteLength = if (utf16) length * 2 else length
        if (offset + byteLength > stringStorageSize) throw PatchException("Hermes string $id runs past the string storage")
        val start = stringStorageStart + offset.toInt()
        return if (utf16) {
            String(bytes, start, byteLength.toInt(), Charsets.UTF_16LE)
        } else {
            String(bytes, start, byteLength.toInt(), Charsets.ISO_8859_1)
        }
    }

    /** Ids of every string-table entry equal to [value]. */
    fun stringIds(value: String): List<Int> = (0 until stringCount).filter { string(it) == value }

    private fun Long.toCount(field: String): Int {
        if (this > bytes.size) throw PatchException("Hermes header field $field is out of range: $this")
        return toInt()
    }

    private fun u32(at: Int): Long =
        (bytes[at].toLong() and 0xFF) or
            ((bytes[at + 1].toLong() and 0xFF) shl 8) or
            ((bytes[at + 2].toLong() and 0xFF) shl 16) or
            ((bytes[at + 3].toLong() and 0xFF) shl 24)

    private fun u64(at: Int): Long = u32(at) or (u32(at + 4) shl 32)

    private fun alignUp(value: Int, alignment: Int): Int = (value + alignment - 1) / alignment * alignment

    companion object {
        const val SUPPORTED_VERSION = 96
        private const val MAGIC = 0x1F1903C103BC1FC6L
        private const val HEADER_FIELDS_END = 8 + 4 + 20 + 19 * 4 + 1
        private const val SMALL_FUNCTION_HEADER_SIZE = 16
        private const val LARGE_FUNCTION_HEADER_SIZE = 7 * 4 + 3
    }
}
