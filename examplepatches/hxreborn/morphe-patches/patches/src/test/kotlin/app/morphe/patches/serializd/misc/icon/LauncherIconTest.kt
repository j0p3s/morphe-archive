/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.serializd.misc.icon

import app.morphe.patcher.patch.PatchException
import app.morphe.patches.shared.misc.png.PngImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class LauncherIconTest {
    @Test
    fun `finds the logo mark circle`() {
        // given
        val splash = splashWithMark(side = 600, centerX = 300, centerY = 340, diameter = 120)

        // when
        val mark = splash.findLogoMark()

        // then
        assertEquals(120, mark.diameter)
        assertEquals(300.5, mark.centerX, 0.6)
        assertEquals(340.5, mark.centerY, 0.6)
    }

    @Test
    fun `rejects a splash image without a mark`() {
        // given
        val blank = PngImage(10, 10, IntArray(100) { OPAQUE_DARK })

        // when
        val error = assertFailsWith<PatchException> { blank.findLogoMark() }

        // then
        assertTrue(error.message!!.contains("logo mark"))
    }

    @Test
    fun `keeps the mark at its stock share of the layer`() {
        // given
        val splash = splashWithMark(side = 600, centerX = 300, centerY = 300, diameter = 186)

        // when
        val layer = splash.resampleLayer(splash.findLogoMark(), 324)

        // then
        assertEquals(324, layer.width)
        assertEquals(0.573 * 324, layer.findLogoMark().diameter.toDouble(), 3.0)
    }

    @Test
    fun `rejects a splash image too small to crop`() {
        // given
        val splash = splashWithMark(side = 200, centerX = 100, centerY = 100, diameter = 186)

        // when
        val error = assertFailsWith<PatchException> { splash.resampleLayer(splash.findLogoMark(), 108) }

        // then
        assertTrue(error.message!!.contains("too small"))
    }

    private fun splashWithMark(side: Int, centerX: Int, centerY: Int, diameter: Int): PngImage {
        val pixels = IntArray(side * side) { OPAQUE_DARK }
        val radius = diameter / 2.0
        for (y in 0 until side) {
            for (x in 0 until side) {
                val dx = x + 0.5 - centerX
                val dy = y + 0.5 - centerY
                if (dx * dx + dy * dy <= radius * radius) pixels[y * side + x] = OPAQUE_TEAL
            }
        }
        return PngImage(side, side, pixels)
    }

    private companion object {
        const val OPAQUE_DARK = 0xff10282b.toInt()
        const val OPAQUE_TEAL = 0xff46bba4.toInt()
    }
}
