package app.czyeru.patches.stickerly

import app.morphe.patcher.patch.PatchException
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document
import org.w3c.dom.Element

/**
 * Turns the light resources of Sticker.ly into AMOLED ones.
 *
 * The app has no dark theme. Its screens are XML layouts and drawables that name light colors
 * directly, so the change is made at every place a color is used, by what the color is used for
 * ([Role]). Every function edits the document it is given and returns true when something changed.
 *
 * Some palette colors (white, s_default...) are also read by the app's code, which cannot be
 * edited here. Those colors are redefined in values/colors.xml ([flipPalette]). Every XML place
 * that names one of them is first replaced by a plain color value ([pin]), so that the XML keeps
 * the color chosen for it by role and does not follow the new palette.
 *
 * [base] is the color that replaces white surfaces.
 */
internal class AmoledConverter(private val resDir: File, private val base: Int) {
    private val colors = ColorResolver(resDir)
    private val styleNames = HashSet<String>()
    private val dominantCache = HashMap<String, Int?>()

    /** New value for each palette color that the app's code reads. See [flipPalette]. */
    private val flipped: Map<String, Int> = buildMap {
        for ((name, how) in FLIPPED_COLORS) {
            val original = colors.resolve("@color/$name")
                ?: throw PatchException("Palette color $name was not found. This app version is not supported.")
            put(
                name,
                when (how) {
                    Flip.BASE -> base
                    Flip.SURFACE -> AmoledColors.map(original, Role.BACKGROUND, base)
                    Flip.TEXT -> AmoledColors.map(original, Role.FOREGROUND, base)
                }
            )
        }
    }

    /** Names of every style in the app. Parent themes are only replaced by themes that exist. */
    fun registerStyles(doc: Document) {
        val styles = doc.getElementsByTagName("style")
        for (i in 0 until styles.length) styleNames.add((styles.item(i) as Element).getAttribute("name"))
    }

    // region layouts and drawables

    fun convertLayout(doc: Document): Boolean {
        var changed = false
        val all = doc.getElementsByTagName("*")
        for (i in 0 until all.length) {
            val e = all.item(i) as Element
            val keepForeground = backgroundStaysBright(e)
            changed = rewriteAttributes(e, keepForeground) or changed
        }
        return changed
    }

    fun convertDrawable(doc: Document): Boolean {
        var changed = false
        val all = doc.getElementsByTagName("*")

        // A vector is only inverted when it is a one-color icon: only greys, with at least one dark one.
        // Illustrations with real colors in them keep every color.
        val paths = (0 until all.length).map { all.item(it) as Element }.filter { it.tagName == "path" }
        val solid = paths.flatMap { p -> VECTOR_COLOR_ATTRS.mapNotNull { resolveAttr(p, it) } }
            .filter { AmoledColors.isSolid(it) }
        val monochrome = solid.isNotEmpty() &&
            solid.all { AmoledColors.isNeutral(it) } &&
            solid.any { AmoledColors.isDarkNeutral(it) }

        for (i in 0 until all.length) {
            val e = all.item(i) as Element
            when (e.tagName) {
                "solid" -> changed = rewrite(e, "android:color", Role.BACKGROUND) or changed
                "stroke" -> changed = rewrite(e, "android:color", Role.BOTH) or changed
                "gradient" -> for (a in GRADIENT_ATTRS) changed = rewrite(e, a, Role.BACKGROUND) or changed
                "item" -> changed = rewrite(e, "android:drawable", Role.BACKGROUND) or changed
                "path" -> if (monochrome) {
                    for (a in VECTOR_COLOR_ATTRS) changed = rewrite(e, a, Role.BOTH) or changed
                }
            }
            // tint is the only attribute that all drawable kinds share with the layouts
            changed = rewrite(e, "android:tint", Role.FOREGROUND) or changed
        }
        return changed
    }

    /** Color selectors of the app (res/color/selector_*.xml). They are used for text. */
    fun convertColorList(doc: Document): Boolean {
        var changed = false
        val items = doc.getElementsByTagName("item")
        for (i in 0 until items.length) {
            changed = rewrite(items.item(i) as Element, "android:color", Role.FOREGROUND) or changed
        }
        return changed
    }

    private fun rewriteAttributes(e: Element, keepForeground: Boolean): Boolean {
        var changed = false
        val attrs = e.attributes
        val names = (0 until attrs.length).map { attrs.item(it).nodeName }
        for (name in names) {
            if (name.startsWith("tools:") || name.startsWith("xmlns")) continue
            val role = ATTR_ROLES[name.substringAfter(':')] ?: continue
            if (role == Role.FOREGROUND && keepForeground) continue
            changed = rewrite(e, name, role) or changed
        }
        return changed
    }

    /**
     * Writes the color for [role] into [attr]. A reference to a [flipped] palette color is always
     * replaced, even when the color stays as it is, because the palette itself changes.
     */
    private fun rewrite(e: Element, attr: String, role: Role): Boolean {
        if (!e.hasAttribute(attr)) return false
        val value = e.getAttribute(attr)
        val original = colors.resolve(value) ?: return false
        val mapped = AmoledColors.map(original, role, base)
        if (mapped == original && !isFlippedRef(value)) return false
        e.setAttribute(attr, AmoledColors.format(mapped))
        return true
    }

    private fun resolveAttr(e: Element, attr: String): Int? =
        if (e.hasAttribute(attr)) colors.resolve(e.getAttribute(attr)) else null

    /**
     * Dark text stays dark on a bright, colorful background such as a yellow button, because
     * that background is not turned dark. The background is taken from the same element.
     */
    private fun backgroundStaysBright(e: Element): Boolean {
        if (!e.hasAttribute("android:background")) return false
        val value = e.getAttribute("android:background")
        val color = if (value.startsWith("@drawable/")) dominantColor(value.removePrefix("@drawable/"))
        else colors.resolve(value)
        return color != null && AmoledColors.staysBright(color)
    }

    /** The first fill color of a shape drawable, or null when it has none or is not a shape. */
    private fun dominantColor(drawable: String): Int? = dominantCache.getOrPut(drawable) {
        val file = File(resDir, "drawable/$drawable.xml")
        if (!file.isFile) return@getOrPut null
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        for ((tag, attr) in listOf("solid" to "android:color", "gradient" to "android:startColor")) {
            val nodes = doc.getElementsByTagName(tag)
            if (nodes.length > 0) return@getOrPut resolveAttr(nodes.item(0) as Element, attr)
        }
        null
    }

    // endregion

    // region palette

    private fun isFlippedRef(value: String): Boolean {
        val v = value.trim()
        return v.startsWith("@color/") && v.removePrefix("@color/") in flipped
    }

    /**
     * Replaces every remaining reference to a [flipped] palette color by the color it has today.
     * Run it on every XML file, after the role conversion.
     */
    fun pin(doc: Document): Boolean {
        var changed = false
        val all = doc.getElementsByTagName("*")
        for (i in 0 until all.length) {
            val e = all.item(i) as Element
            val attrs = e.attributes
            for (k in 0 until attrs.length) {
                val attr = attrs.item(k)
                if (isFlippedRef(attr.nodeValue)) {
                    attr.nodeValue = AmoledColors.format(colors.resolve(attr.nodeValue)!!)
                    changed = true
                }
            }
            if (e.tagName == "item" || e.tagName == "color") {
                // only elements with just text, such as <item name="x">@color/white</item>
                val onlyText = (0 until e.childNodes.length).none { e.childNodes.item(it) is Element }
                if (onlyText && isFlippedRef(e.textContent)) {
                    e.textContent = AmoledColors.format(colors.resolve(e.textContent)!!)
                    changed = true
                }
            }
        }
        return changed
    }

    /**
     * values/colors.xml: gives the palette colors read by the app's code their AMOLED value.
     * Colors that merely point at one of them are replaced by the old value first, so they stay
     * as they were.
     */
    fun flipPalette(doc: Document): Boolean {
        var changed = false
        val nodes = doc.getElementsByTagName("color")
        val seen = HashSet<String>()
        for (i in 0 until nodes.length) {
            val e = nodes.item(i) as Element
            val name = e.getAttribute("name")
            val new = flipped[name]
            if (new != null) {
                e.textContent = AmoledColors.format(new)
                seen.add(name)
                changed = true
            } else if (isFlippedRef(e.textContent)) {
                e.textContent = AmoledColors.format(colors.resolve(e.textContent)!!)
                changed = true
            }
        }
        if (seen != flipped.keys) throw PatchException("Palette colors missing: ${flipped.keys - seen}")
        return changed
    }

    // endregion

    // region themes

    /**
     * Makes the light app themes dark and gives them AMOLED backgrounds, and applies the color
     * roles to the style items that use the app's palette. Styles of libraries are not changed,
     * except for the light-bar flags.
     */
    fun convertStyles(doc: Document): Boolean {
        var changed = false
        val styles = doc.getElementsByTagName("style")
        for (i in 0 until styles.length) {
            val style = styles.item(i) as Element
            val name = style.getAttribute("name")

            if (name in DARKENED_THEMES) {
                changed = darken(doc, style) or changed
            }

            val children = style.childNodes
            for (k in 0 until children.length) {
                val item = children.item(k) as? Element ?: continue
                if (item.tagName != "item") continue
                val local = item.getAttribute("name").substringAfter(':')
                val text = item.textContent.trim()

                if (local in LIGHT_BAR_FLAGS) {
                    if (text == "true") {
                        item.textContent = "false"
                        changed = true
                    }
                    continue
                }
                if (!isPaletteRef(text)) continue
                val role = ATTR_ROLES[local] ?: continue
                val original = colors.resolve(text) ?: continue
                val mapped = AmoledColors.map(original, role, base)
                if (mapped != original || isFlippedRef(text)) {
                    item.textContent = AmoledColors.format(mapped)
                    changed = true
                }
            }
        }
        return changed
    }

    private fun darken(doc: Document, style: Element): Boolean {
        val parent = style.getAttribute("parent")
        val name = style.getAttribute("name")
        val dark = DARK_PARENTS[parent]
        val keepsParent = dark == null && parent in KEPT_PARENTS
        if (dark == null && !keepsParent && parent !in DARK_PARENTS.values) {
            throw PatchException("Unexpected parent '$parent' for style $name")
        }
        if (dark != null) {
            if (dark.removePrefix("@style/") !in styleNames) {
                throw PatchException("Dark theme $dark for style $name does not exist")
            }
            style.setAttribute("parent", dark)
        }

        val items = LinkedHashMap<String, String>()
        val dialog = (dark ?: parent).contains("Dialog")
        items["android:colorBackground"] = AmoledColors.format(base)
        if (dialog) items["colorBackgroundFloating"] = AmoledColors.format(base)
        else {
            items["android:windowBackground"] = AmoledColors.format(base)
            items["android:navigationBarColor"] = AmoledColors.format(base)
        }
        // A theme that keeps its light parent has light default text. The dark text colors fix that.
        if (keepsParent) items.putAll(DARK_DEFAULT_TEXT)

        val existing = HashSet<String>()
        val children = style.childNodes
        for (k in 0 until children.length) (children.item(k) as? Element)?.let { existing.add(it.getAttribute("name")) }

        var changed = dark != null
        for ((item, value) in items) {
            if (item in existing) continue
            val e = doc.createElement("item")
            e.setAttribute("name", item)
            e.textContent = value
            style.appendChild(e)
            changed = true
        }
        return changed
    }

    /** Only colors of the app's own palette are changed in styles. Library styles name their own colors. */
    private fun isPaletteRef(text: String) =
        text == "@color/white" || text.startsWith("@color/s_") || text == "@color/dimmedText"

    // endregion

    private enum class Flip { BASE, SURFACE, TEXT }

    private companion object {
        /** Color attributes by what they paint. Names are without the namespace prefix. */
        val ATTR_ROLES: Map<String, Role> = buildMap {
            for (a in listOf(
                "background", "backgroundTint", "cardBackgroundColor", "chipBackgroundColor",
                "boxBackgroundColor", "trackColor", "dividerColor", "divider",
                "windowBackground", "colorBackground", "colorBackgroundFloating",
                "statusBarColor", "navigationBarColor",
            )) put(a, Role.BACKGROUND)
            for (a in listOf(
                "textColor", "textColorHint", "textColorLink", "textColorPrimary", "textColorSecondary",
                "textColorTertiary", "tint", "iconTint", "drawableTint", "indeterminateTint", "progressTint",
                "appbar_title_color", "tabIndicatorColor", "tabTextColor", "tabSelectedTextColor",
                "indicatorColor", "hintTextColor", "cursorColor", "colorEdgeEffect",
            )) put(a, Role.FOREGROUND)
            put("strokeColor", Role.BOTH)
        }

        val VECTOR_COLOR_ATTRS = listOf("android:fillColor", "android:strokeColor")
        val GRADIENT_ATTRS = listOf("android:startColor", "android:centerColor", "android:endColor")
        val LIGHT_BAR_FLAGS = setOf("windowLightStatusBar", "windowLightNavigationBar")

        /**
         * Palette colors that the app's code reads (title text, navigation bar, data binding).
         * The white ones become the AMOLED color, the light greys become dark, and the dark
         * text colors become light. s_black is a dark surface in the code (navigation bar), so it
         * becomes the AMOLED color too. Greys in the middle are readable on black and stay.
         */
        val FLIPPED_COLORS = linkedMapOf(
            "white" to Flip.BASE,
            "s_white" to Flip.BASE,
            "s_black" to Flip.BASE,
            "s_default" to Flip.TEXT,
            "s_gray_80" to Flip.TEXT,
            "s_gray_1" to Flip.SURFACE,
            "s_gray_2" to Flip.SURFACE,
            "s_gray_3" to Flip.SURFACE,
            "s_gray_5" to Flip.SURFACE,
            "s_gray_10" to Flip.SURFACE,
            "s_lightgray" to Flip.SURFACE,
            "s_lightblue" to Flip.SURFACE,
            "s_bg_blue" to Flip.SURFACE,
            "s_lightgreen" to Flip.SURFACE,
            "s_lightpink" to Flip.SURFACE,
            "s_lightpurple" to Flip.SURFACE,
        )

        /** The app themes that are light, as named in res/values*. */
        val DARKENED_THEMES = setOf(
            "AlertDialog", "AppTheme", "AppTheme.WhiteColorBackground", "EditText", "EditTextTheme",
            "SheetDialogParent",
        )

        val DARK_PARENTS = mapOf(
            "@style/Theme.AppCompat.Light.NoActionBar" to "@style/Theme.AppCompat.NoActionBar",
            "@style/Theme.AppCompat.Light.Dialog" to "@style/Theme.AppCompat.Dialog",
        )

        /** Light parents that have no dark twin in the app. They stay, and the theme is darkened by its items. */
        val KEPT_PARENTS = setOf("@style/Theme.Design.Light.BottomSheetDialog")

        val DARK_DEFAULT_TEXT = mapOf(
            "android:textColorPrimary" to "@color/abc_primary_text_material_dark",
            "android:textColorSecondary" to "@color/abc_secondary_text_material_dark",
            "android:textColorHint" to "@color/abc_hint_foreground_material_dark",
        )
    }
}
