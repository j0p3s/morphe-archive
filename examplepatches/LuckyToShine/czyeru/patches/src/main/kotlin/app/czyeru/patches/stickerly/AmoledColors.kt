package app.czyeru.patches.stickerly

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** What a color is used for. It decides which colors are changed and how. */
internal enum class Role {
    /** Fills, backgrounds and background tints. Light surfaces become dark. */
    BACKGROUND,

    /** Text, icons and icon tints. Dark neutral colors become light. */
    FOREGROUND,

    /** Strokes and single-color icons. Both of the above. */
    BOTH,
}

/** Colors are plain ARGB integers, as in Android. */
internal object AmoledColors {
    /** Alpha from which a color counts as solid. Translucent overlays are never changed. */
    private const val MIN_SOLID_ALPHA = 0xE0

    private const val SURFACE_MIN_LUMA = 0.70
    private const val SURFACE_MAX_SATURATION = 0.30
    private const val TEXT_MAX_LUMA = 0.40
    private const val TEXT_MAX_SATURATION = 0.35
    private const val NEAR_BLACK_MAX_LUMA = 0.10

    /** Framework colors that are one fixed value. Selector colors such as primary_text_light are left out. */
    private val FRAMEWORK = mapOf(
        "white" to 0xFFFFFFFF.toInt(),
        "black" to 0xFF000000.toInt(),
        "transparent" to 0x00000000,
        "darker_gray" to 0xFFAAAAAA.toInt(),
    )

    fun frameworkColor(name: String): Int? = FRAMEWORK[name]

    /** Android color syntax: #RGB, #ARGB, #RRGGBB or #AARRGGBB. */
    fun parse(text: String): Int? {
        val t = text.trim()
        if (!t.startsWith("#")) return null
        val hex = t.substring(1)
        if (!hex.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) return null
        return when (hex.length) {
            3 -> hex.map { "$it$it" }.joinToString("").let { (0xFF000000 or it.toLong(16)).toInt() }
            4 -> hex.map { "$it$it" }.joinToString("").toLong(16).toInt()
            6 -> (0xFF000000 or hex.toLong(16)).toInt()
            8 -> hex.toLong(16).toInt()
            else -> null
        }
    }

    fun format(argb: Int): String = if (alpha(argb) == 0xFF) {
        "#%06x".format(argb and 0xFFFFFF)
    } else {
        "#%08x".format(argb)
    }

    private fun alpha(c: Int) = c ushr 24
    private fun red(c: Int) = (c shr 16) and 0xFF
    private fun green(c: Int) = (c shr 8) and 0xFF
    private fun blue(c: Int) = c and 0xFF

    fun luma(c: Int): Double = (0.299 * red(c) + 0.587 * green(c) + 0.114 * blue(c)) / 255.0

    private fun saturation(c: Int): Double {
        val hi = max(red(c), max(green(c), blue(c)))
        val lo = min(red(c), min(green(c), blue(c)))
        return if (hi == 0) 0.0 else (hi - lo).toDouble() / hi
    }

    fun isSolid(c: Int) = alpha(c) >= MIN_SOLID_ALPHA

    fun isLightSurface(c: Int) =
        isSolid(c) && luma(c) >= SURFACE_MIN_LUMA && saturation(c) <= SURFACE_MAX_SATURATION

    fun isDarkNeutral(c: Int) =
        isSolid(c) && luma(c) <= TEXT_MAX_LUMA && saturation(c) <= TEXT_MAX_SATURATION

    private fun isNearBlack(c: Int) = isSolid(c) && luma(c) <= NEAR_BLACK_MAX_LUMA && saturation(c) <= TEXT_MAX_SATURATION

    /** Opaque and not colorful: white, black and greys. */
    fun isNeutral(c: Int) = isSolid(c) && saturation(c) <= TEXT_MAX_SATURATION

    /** Keeps hue and saturation and flips lightness: white becomes black, #222222 becomes #dddddd. */
    private fun invertLightness(c: Int): Int {
        val r = red(c) / 255.0
        val g = green(c) / 255.0
        val b = blue(c) / 255.0
        val hi = max(r, max(g, b))
        val lo = min(r, min(g, b))
        val l = (hi + lo) / 2
        val d = hi - lo
        val s = if (d == 0.0) 0.0 else d / (1 - abs(2 * l - 1))
        val h = when {
            d == 0.0 -> 0.0
            hi == r -> (((g - b) / d) % 6 + 6) % 6
            hi == g -> (b - r) / d + 2
            else -> (r - g) / d + 4
        }
        val l2 = 1 - l
        val c2 = (1 - abs(2 * l2 - 1)) * s
        val x = c2 * (1 - abs(h % 2 - 1))
        val m = l2 - c2 / 2
        val (r1, g1, b1) = when {
            h < 1 -> Triple(c2, x, 0.0)
            h < 2 -> Triple(x, c2, 0.0)
            h < 3 -> Triple(0.0, c2, x)
            h < 4 -> Triple(0.0, x, c2)
            h < 5 -> Triple(x, 0.0, c2)
            else -> Triple(c2, 0.0, x)
        }
        fun ch(v: Double) = ((v + m) * 255).roundToInt().coerceIn(0, 255)
        return (alpha(c) shl 24) or (ch(r1) shl 16) or (ch(g1) shl 8) or ch(b1)
    }

    /** Moves the darkest color (pure black) up to [base]. Brighter colors scale to fit. */
    private fun liftBlack(c: Int, base: Int): Int {
        fun ch(v: Int, floor: Int) = (floor + v * (255 - floor) / 255.0).roundToInt().coerceIn(0, 255)
        return (alpha(c) shl 24) or
            (ch(red(c), red(base)) shl 16) or
            (ch(green(c), green(base)) shl 8) or
            ch(blue(c), blue(base))
    }

    /**
     * Returns the AMOLED version of [c] for [role], or [c] itself when it should stay.
     * [base] is the color that replaces white surfaces. Pure black by default.
     *
     * Backgrounds: light surfaces become dark, and the near-black surfaces of the light theme
     * (#111111) become [base], so that nothing is brighter than needed.
     * Foregrounds: dark neutral colors become light.
     */
    fun map(c: Int, role: Role, base: Int): Int {
        if (role != Role.FOREGROUND && isLightSurface(c)) return liftBlack(invertLightness(c), base)
        if (role == Role.BACKGROUND && isNearBlack(c)) return (alpha(c) shl 24) or (base and 0xFFFFFF)
        if (role != Role.BACKGROUND && isDarkNeutral(c)) return invertLightness(c)
        return c
    }

    /** True when [c] stays a bright color after [map] as a background. Dark text on it must stay dark. */
    fun staysBright(c: Int): Boolean = isSolid(c) && luma(c) >= 0.5 && !isLightSurface(c)
}

/**
 * Looks up color values from values/colors.xml, following @color/ references.
 * Colors that are selectors (res/color) or theme attributes have no single value and give null.
 */
internal class ColorResolver(resDir: File) {
    private val named = HashMap<String, String>()

    init {
        val file = File(resDir, "values/colors.xml")
        if (file.isFile) {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
            val nodes = doc.getElementsByTagName("color")
            for (i in 0 until nodes.length) {
                val e = nodes.item(i) as org.w3c.dom.Element
                named[e.getAttribute("name")] = e.textContent.trim()
            }
        }
    }

    fun resolve(value: String, depth: Int = 0): Int? {
        if (depth > 8) return null
        val v = value.trim()
        return when {
            v.startsWith("#") -> AmoledColors.parse(v)
            v.startsWith("@android:color/") -> AmoledColors.frameworkColor(v.removePrefix("@android:color/"))
            v.startsWith("@color/") -> named[v.removePrefix("@color/")]?.let { resolve(it, depth + 1) }
            else -> null
        }
    }
}
