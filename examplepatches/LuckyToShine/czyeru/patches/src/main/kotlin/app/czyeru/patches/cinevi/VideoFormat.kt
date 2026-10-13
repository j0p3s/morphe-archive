package app.czyeru.patches.cinevi

import java.io.File
import java.io.RandomAccessFile

/** What [VideoFormat.inspect] found in a video file. */
internal data class VideoInfo(
    /** "WebM", "MP4" or "unknown". */
    val container: String,
    /** Codec name such as "VP9", "AV1", "HEVC", "H.264" or null if it could not be determined. */
    val codec: String?,
) {
    /** WebM with VP9 or AV1, or MP4 with HEVC. */
    val supported: Boolean
        get() = (container == "WebM" && (codec == "VP9" || codec == "AV1")) ||
            (container == "MP4" && codec == "HEVC")

    override fun toString() = "$container / ${codec ?: "unknown codec"}"
}

/**
 * Reads the container and video codec straight from the file, so a wrong file is rejected when
 * patching and not discovered as a blank loading screen on the phone.
 */
internal object VideoFormat {
    private val EBML_MAGIC = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())

    /** EBML "CodecID" element (0x86) followed by a length byte and the codec string. */
    private val WEBM_CODECS = mapOf(
        "V_VP9" to "VP9",
        "V_AV1" to "AV1",
        "V_VP8" to "VP8",
        "V_MPEG4/ISO/AVC" to "H.264",
        "V_MPEGH/ISO/HEVC" to "HEVC",
    )

    private val MP4_CODECS = mapOf(
        "hvc1" to "HEVC",
        "hev1" to "HEVC",
        "avc1" to "H.264",
        "avc3" to "H.264",
        "av01" to "AV1",
        "vp09" to "VP9",
    )

    fun inspect(file: File): VideoInfo = RandomAccessFile(file, "r").use { raf ->
        val head = ByteArray(8)
        val read = raf.read(head)
        when {
            read >= 4 && head.copyOfRange(0, 4).contentEquals(EBML_MAGIC) ->
                VideoInfo("WebM", webmCodec(raf))
            read >= 8 && String(head, 4, 4, Charsets.ISO_8859_1) == "ftyp" ->
                VideoInfo("MP4", mp4Codec(raf))
            else -> VideoInfo("unknown", null)
        }
    }

    /** The track list sits at the start of the file, so the first 2 MiB are enough. */
    private fun webmCodec(raf: RandomAccessFile): String? {
        val size = minOf(raf.length(), 2L * 1024 * 1024).toInt()
        val data = ByteArray(size)
        raf.seek(0)
        raf.readFully(data)
        for ((id, name) in WEBM_CODECS) {
            val needle = byteArrayOf(0x86.toByte(), (0x80 or id.length).toByte()) +
                id.toByteArray(Charsets.ISO_8859_1)
            if (indexOf(data, needle) >= 0) return name
        }
        return null
    }

    /** Walks moov/trak/mdia/(hdlr, minf/stbl/stsd) and returns the first video sample entry type. */
    private fun mp4Codec(raf: RandomAccessFile): String? {
        val containers = setOf("moov", "trak", "mdia", "minf", "stbl")

        fun walk(start: Long, end: Long, isVideoTrack: Boolean?): String? {
            var pos = start
            var video = isVideoTrack
            while (pos + 8 <= end) {
                raf.seek(pos)
                val header = ByteArray(8)
                raf.readFully(header)
                var size = readUInt(header, 0)
                val type = String(header, 4, 4, Charsets.ISO_8859_1)
                var payload = pos + 8
                if (size == 1L) {
                    val large = ByteArray(8)
                    raf.readFully(large)
                    size = readLong(large)
                    payload += 8
                } else if (size == 0L) {
                    size = end - pos
                }
                if (size < 8 || pos + size > end) return null
                val boxEnd = pos + size

                when (type) {
                    "trak" -> walk(payload, boxEnd, null)?.let { return it }
                    in containers -> walk(payload, boxEnd, video)?.let { return it }
                    "hdlr" -> {
                        // version/flags (4) + pre_defined (4) + handler_type (4)
                        raf.seek(payload + 8)
                        val handler = ByteArray(4)
                        raf.readFully(handler)
                        video = String(handler, Charsets.ISO_8859_1) == "vide"
                    }
                    "stsd" -> if (video == true) {
                        // version/flags (4) + entry_count (4) + first entry: size (4) + type (4)
                        raf.seek(payload + 12)
                        val entry = ByteArray(4)
                        raf.readFully(entry)
                        val fourcc = String(entry, Charsets.ISO_8859_1)
                        return MP4_CODECS[fourcc] ?: fourcc
                    }
                }
                pos = boxEnd
            }
            return null
        }

        return walk(0, raf.length(), null)
    }

    private fun readUInt(b: ByteArray, offset: Int): Long =
        ((b[offset].toLong() and 0xFF) shl 24) or ((b[offset + 1].toLong() and 0xFF) shl 16) or
            ((b[offset + 2].toLong() and 0xFF) shl 8) or (b[offset + 3].toLong() and 0xFF)

    private fun readLong(b: ByteArray): Long {
        var value = 0L
        for (i in 0 until 8) value = (value shl 8) or (b[i].toLong() and 0xFF)
        return value
    }

    private fun indexOf(data: ByteArray, needle: ByteArray): Int {
        outer@ for (i in 0..data.size - needle.size) {
            for (j in needle.indices) if (data[i + j] != needle[j]) continue@outer
            return i
        }
        return -1
    }
}
