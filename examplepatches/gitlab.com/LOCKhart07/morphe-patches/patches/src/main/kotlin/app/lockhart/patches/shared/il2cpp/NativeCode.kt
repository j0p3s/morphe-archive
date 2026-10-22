/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Helpers for writing small native routines into libil2cpp.so. Instructions are written as
 * 32-bit little-endian words for both arm64 and armv7 (ARM mode, not Thumb).
 */
package app.lockhart.patches.shared.il2cpp

/** Parses whitespace-separated hex bytes, e.g. "20008052 c0035fd6". */
fun hex(value: String): ByteArray {
    val digits = value.filterNot(Char::isWhitespace)
    require(digits.length % 2 == 0) { "Hex string needs an even number of digits" }

    return ByteArray(digits.length / 2) { digits.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

/** Encodes instruction words as little-endian bytes. */
fun words(vararg words: Long) = ByteArray(words.size * 4) { (words[it / 4] ushr (8 * (it % 4))).toByte() }

fun ByteArray.wordAt(offset: Int) =
    (0..3).fold(0) { acc, i -> acc or ((this[offset + i].toInt() and 0xFF) shl (8 * i)) }

/** An instruction to match: bits set in [mask] must equal [word]. */
class MaskedWord(val word: Int, val mask: Int = -1)

/** Word-aligned offsets in [start, end) where [pattern] matches. */
fun ByteArray.findWords(start: Int, end: Int, pattern: List<MaskedWord>) =
    (start..end - pattern.size * 4 step 4).filter { at ->
        pattern.withIndex().all { (i, p) -> wordAt(at + i * 4) and p.mask == p.word and p.mask }
    }

/** `bl` from [from] to [to] (file offsets in the same segment). */
fun arm64Call(from: Long, to: Long) = 0x94000000 or ((to - from) / 4 and 0x03FFFFFF)

/** ARM-mode `bl` from [from] to [to]; the PC reads 8 bytes ahead. */
fun armCall(from: Long, to: Long) = 0xEB000000 or ((to - from - 8) / 4 and 0x00FFFFFF)

/** Target of the arm64 `b` at [at], or null if it is not one. */
fun ByteArray.arm64BranchTarget(at: Int): Long? {
    val word = wordAt(at)
    if (word and 0xFC000000.toInt() != 0x14000000) return null
    return at + ((word shl 6) shr 6) * 4L
}

/** Target of the unconditional ARM-mode `b` at [at], or null if it is not one. */
fun ByteArray.armBranchTarget(at: Int): Long? {
    val word = wordAt(at)
    if (word and 0xFF000000.toInt() != 0xEA000000.toInt()) return null
    return at + 8 + ((word shl 8) shr 8) * 4L
}
