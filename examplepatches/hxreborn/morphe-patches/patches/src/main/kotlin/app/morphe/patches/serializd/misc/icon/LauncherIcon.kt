/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.serializd.misc.icon

import app.morphe.patcher.patch.PatchException
import app.morphe.patches.shared.misc.png.PngImage
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val MARK_MIN_GREEN = 150
private const val MARK_MIN_GREEN_LEAD = 60
private const val MARK_SHARE_OF_LAYER = 0.573
private const val CHANNEL_COUNT = 4

internal class LogoMark(val centerX: Double, val centerY: Double, val diameter: Int) {
    val layerSide: Double get() = diameter / MARK_SHARE_OF_LAYER
}

internal fun PngImage.findLogoMark(): LogoMark {
    var minX = width
    var maxX = -1
    var minY = height
    var maxY = -1

    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = argb[y * width + x]
            val red = (pixel shr 16) and 0xff
            val green = (pixel shr 8) and 0xff
            if (green < MARK_MIN_GREEN || green - red < MARK_MIN_GREEN_LEAD) continue
            minX = min(minX, x)
            maxX = max(maxX, x)
            minY = min(minY, y)
            maxY = max(maxY, y)
        }
    }

    if (maxX < 0) throw PatchException("Could not find the logo mark in the splash image")
    return LogoMark((minX + maxX) / 2.0 + 0.5, (minY + maxY) / 2.0 + 0.5, maxX - minX + 1)
}

internal fun PngImage.resampleLayer(mark: LogoMark, size: Int): PngImage {
    val side = mark.layerSide
    val left = mark.centerX - side / 2
    val top = mark.centerY - side / 2
    if (left < 0 || top < 0 || left + side > width || top + side > height) {
        throw PatchException("The splash image is too small to crop the launcher icon from")
    }

    val scale = side / size
    val samplesPerAxis = max(1, ceil(scale).toInt())
    val sampleCount = samplesPerAxis * samplesPerAxis
    val channelSums = DoubleArray(CHANNEL_COUNT)
    val pixels = IntArray(size * size)

    for (y in 0 until size) {
        for (x in 0 until size) {
            channelSums.fill(0.0)
            for (sampleY in 0 until samplesPerAxis) {
                for (sampleX in 0 until samplesPerAxis) {
                    val sourceX = left + (x + (sampleX + 0.5) / samplesPerAxis) * scale - 0.5
                    val sourceY = top + (y + (sampleY + 0.5) / samplesPerAxis) * scale - 0.5
                    accumulateBilinearSample(sourceX, sourceY, channelSums)
                }
            }
            pixels[y * size + x] = averageArgb(channelSums, sampleCount)
        }
    }

    return PngImage(size, size, pixels)
}

private fun PngImage.accumulateBilinearSample(x: Double, y: Double, channelSums: DoubleArray) {
    val x0 = x.toInt().coerceIn(0, width - 1)
    val y0 = y.toInt().coerceIn(0, height - 1)
    val x1 = min(x0 + 1, width - 1)
    val y1 = min(y0 + 1, height - 1)
    val fx = (x - x0).coerceIn(0.0, 1.0)
    val fy = (y - y0).coerceIn(0.0, 1.0)

    for (channel in 0 until CHANNEL_COUNT) {
        val shift = channel * 8
        val top = channelAt(x0, y0, shift) * (1 - fx) + channelAt(x1, y0, shift) * fx
        val bottom = channelAt(x0, y1, shift) * (1 - fx) + channelAt(x1, y1, shift) * fx
        channelSums[channel] += top * (1 - fy) + bottom * fy
    }
}

private fun PngImage.channelAt(x: Int, y: Int, shift: Int): Int = (argb[y * width + x] ushr shift) and 0xff

private fun averageArgb(channelSums: DoubleArray, sampleCount: Int): Int {
    var pixel = 0
    for (channel in 0 until CHANNEL_COUNT) {
        pixel = pixel or ((channelSums[channel] / sampleCount).roundToInt().coerceIn(0, 255) shl (channel * 8))
    }
    return pixel
}
