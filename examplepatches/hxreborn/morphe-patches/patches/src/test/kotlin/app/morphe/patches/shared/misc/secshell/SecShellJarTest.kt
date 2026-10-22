/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.shared.misc.secshell

import app.morphe.patcher.patch.PatchException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val CIPHER_HEAD_SIZE = 0x20000

private fun zipOf(entries: List<Pair<String, ByteArray>>, comment: String? = null): ByteArray {
    val output = ByteArrayOutputStream()
    ZipOutputStream(output).use { zip ->
        zip.setComment(comment)
        entries.forEach { (name, data) ->
            zip.putNextEntry(ZipEntry(name))
            zip.write(data)
            zip.closeEntry()
        }
    }
    return output.toByteArray()
}

private fun encoded(zip: ByteArray) = zip.copyOf().also {
    for (index in CIPHER_HEAD_SIZE until it.size) it[index] = (it[index].toInt() xor 0xAC).toByte()
}

private fun givenJar(comment: String? = null): ByteArray {
    val random = Random(7)
    val head = ByteArray(CIPHER_HEAD_SIZE + 4096).also(random::nextBytes)
    val first = ByteArray(2048) { (it % 7).toByte() }
    val second = ByteArray(6000) { (it % 13).toByte() }
    return encoded(zipOf(listOf("classes.dex" to head, "classes2.dex" to first, "classes3.dex" to second), comment))
}

private fun crcOf(bytes: ByteArray) = CRC32().apply { update(bytes) }.value

private const val CENTRAL_HEADER_SIZE = 46

private fun indexOf(bytes: ByteArray, pattern: ByteArray, from: Int = 0) =
    (from..bytes.size - pattern.size).first { start -> pattern.indices.all { bytes[start + it] == pattern[it] } }

private fun givenJarWith(entry: String, mutate: (plain: ByteArray, dataOffset: Int, centralOffset: Int) -> Unit): ByteArray {
    val plain = encoded(givenJar())
    val name = entry.toByteArray()
    val localName = indexOf(plain, name)
    val extra = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN).getShort(localName - 2).toInt()
    val centralName = indexOf(plain, name, localName + name.size)
    mutate(plain, localName + name.size + extra, centralName - CENTRAL_HEADER_SIZE)
    return encoded(plain)
}

private fun withPreamble(jar: ByteArray, preamble: ByteArray): ByteArray {
    val shifted = preamble + encoded(jar)
    val buffer = ByteBuffer.wrap(shifted).order(ByteOrder.LITTLE_ENDIAN)
    val endOffset = shifted.size - 22
    buffer.putInt(endOffset + 16, buffer.getInt(endOffset + 16) + preamble.size)
    var cursor = buffer.getInt(endOffset + 16)
    repeat(buffer.getShort(endOffset + 10).toInt()) {
        buffer.putInt(cursor + 42, buffer.getInt(cursor + 42) + preamble.size)
        cursor += CENTRAL_HEADER_SIZE + buffer.getShort(cursor + 28) + buffer.getShort(cursor + 30) + buffer.getShort(cursor + 32)
    }
    return encoded(shifted)
}

private fun commentLengths(jar: ByteArray): Pair<Int, Int> {
    val plain = encoded(jar)
    val endOffset = plain.size - 22 - 4
    val buffer = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN)
    return buffer.getInt(endOffset) to buffer.getShort(endOffset + 20).toInt()
}

class SecShellJarTest {
    @Test
    fun `reads an entry stored past the ciphered head`() {
        // given
        val jar = SecShellJar.of(givenJar())

        // when
        val entry = jar.read("classes2.dex")

        // then
        assertContentEquals(ByteArray(2048) { (it % 7).toByte() }, entry)
    }

    @Test
    fun `lists only the dex entries past the ciphered head`() {
        // given
        val jar = SecShellJar.of(givenJar())

        // when
        val entries = jar.dexEntries()

        // then
        assertEquals(listOf("classes2.dex", "classes3.dex"), entries)
    }

    @Test
    fun `keeps the CRC32 of the whole jar when an entry changes`() {
        // given
        val original = givenJar()
        val replacement = ByteArray(5000) { (it % 31).toByte() }

        // when
        val patched = SecShellJar.of(original).withEntries(mapOf("classes3.dex" to replacement))

        // then
        assertEquals(crcOf(original), crcOf(patched))
    }

    @Test
    fun `reads back a replaced entry and leaves the others untouched`() {
        // given
        val original = givenJar()
        val replacement = ByteArray(5000) { (it % 31).toByte() }

        // when
        val patched = SecShellJar.of(SecShellJar.of(original).withEntries(mapOf("classes3.dex" to replacement)))

        // then
        assertContentEquals(replacement, patched.read("classes3.dex"))
        assertContentEquals(ByteArray(2048) { (it % 7).toByte() }, patched.read("classes2.dex"))
    }

    @Test
    fun `keeps the ciphered head byte for byte`() {
        // given
        val original = givenJar()

        // when
        val patched = SecShellJar.of(original).withEntries(mapOf("classes2.dex" to ByteArray(10)))

        // then
        assertContentEquals(original.copyOf(CIPHER_HEAD_SIZE), patched.copyOf(CIPHER_HEAD_SIZE))
    }

    @Test
    fun `ends with one four byte comment when the jar already had a comment`() {
        // given
        val original = givenJar(comment = "a stock jar comment")

        // when
        val patched = SecShellJar.of(original).withEntries(mapOf("classes3.dex" to ByteArray(10)))
        val repatched = SecShellJar.of(patched).withEntries(mapOf("classes3.dex" to ByteArray(12)))

        // then
        assertEquals(0x06054b50 to 4, commentLengths(patched))
        assertEquals(0x06054b50 to 4, commentLengths(repatched))
    }

    @Test
    fun `reads a replaced entry in a sequential scan`() {
        // given
        val replacement = ByteArray(5000) { (it % 31).toByte() }

        // when
        val patched = SecShellJar.of(givenJar()).withEntries(mapOf("classes2.dex" to replacement))
        val scanned = ZipInputStream(ByteArrayInputStream(encoded(patched))).use { zip ->
            generateSequence { zip.nextEntry }.associate { it.name to zip.readBytes() }
        }

        // then
        assertContentEquals(replacement, scanned["classes2.dex"])
        assertContentEquals(ByteArray(6000) { (it % 13).toByte() }, scanned["classes3.dex"])
    }

    @Test
    fun `rejects replacing an entry inside the ciphered head`() {
        // given
        val jar = SecShellJar.of(givenJar())

        // when
        val failure = assertFailsWith<PatchException> { jar.withEntries(mapOf("classes.dex" to ByteArray(10))) }

        // then
        assertEquals("SecShell jar entry classes.dex starts inside the ciphered head", failure.message)
    }

    @Test
    fun `rejects an entry whose deflate stream is cut short`() {
        // given
        val jar = SecShellJar.of(
            givenJarWith("classes2.dex") { plain, _, central ->
                val buffer = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN)
                buffer.putInt(central + 20, buffer.getInt(central + 20) / 2)
            },
        )

        // when
        val failure = assertFailsWith<PatchException> { jar.read("classes2.dex") }

        // then
        assertEquals("SecShell jar entry ends before its deflate stream does", failure.message)
    }

    @Test
    fun `rejects an entry with a corrupt deflate stream`() {
        // given
        val jar = SecShellJar.of(
            givenJarWith("classes2.dex") { plain, data, central ->
                val size = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN).getInt(central + 20)
                plain.fill(0xFF.toByte(), data, data + size)
            },
        )

        // when
        val failure = assertFailsWith<PatchException> { jar.read("classes2.dex") }

        // then
        assertEquals("SecShell jar entry has a corrupt deflate stream", failure.message)
    }

    @Test
    fun `keeps the bytes before the first local record`() {
        // given
        val preamble = ByteArray(64) { (it + 1).toByte() }
        val replacement = ByteArray(5000) { (it % 31).toByte() }

        // when
        val patched = SecShellJar.of(withPreamble(givenJar(), preamble)).withEntries(mapOf("classes3.dex" to replacement))

        // then
        assertContentEquals(preamble, patched.copyOf(preamble.size))
        assertContentEquals(replacement, SecShellJar.of(patched).read("classes3.dex"))
        assertContentEquals(ByteArray(2048) { (it % 7).toByte() }, SecShellJar.of(patched).read("classes2.dex"))
    }

    @Test
    fun `rejects a central directory out of local record order`() {
        // given
        val jar = SecShellJar.of(
            givenJarWith("classes2.dex") { plain, _, central ->
                val buffer = ByteBuffer.wrap(plain).order(ByteOrder.LITTLE_ENDIAN)
                val next = central + CENTRAL_HEADER_SIZE + buffer.getShort(central + 28) +
                    buffer.getShort(central + 30) + buffer.getShort(central + 32)
                val offset = buffer.getInt(central + 42)
                buffer.putInt(central + 42, buffer.getInt(next + 42))
                buffer.putInt(next + 42, offset)
            },
        )

        // when
        val failure = assertFailsWith<PatchException> { jar.withEntries(mapOf("classes2.dex" to ByteArray(4))) }

        // then
        assertEquals("SecShell jar central directory is not in local record order", failure.message)
    }
}
