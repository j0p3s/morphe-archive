package app.czyeru.patches.stickerly

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.czyeru.patches.shared.Constants.COMPATIBILITY_STICKERLY
import java.io.File
import java.util.logging.Logger
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Document

/**
 * Resources that belong to libraries, such as AppCompat, Material, ExoPlayer and the ad SDKs.
 * They get the dark theme through the app theme, or have their own look. Only the palette
 * references in them are fixed.
 */
private val LIBRARY_PREFIXES = listOf(
    "abc_", "mtrl_", "material_", "design_", "m3_", "$", "common_", "com_", "exo_", "mbridge_",
    "applovin_", "max_", "mediation_", "moloco_", "messenger_", "browser_", "preference_",
    "notification_", "fingerprint_", "tooltip_", "compat_", "support_", "ime_", "select_dialog",
    "expand_button", "tt_", "admob_", "aps_", "avd_", "ia_", "im_", "cb_", "btn_checkbox", "btn_radio",
    "offline_", "dt_store", "content_info", "open_url", "learn_more", "pn_circular", "mainlayout",
    "background_lightpopup",
)

/** Drawables that must keep their colors: app icons and the system splash icon. */
private val KEPT_DRAWABLES = listOf("ic_launcher", "splash_icon")

private fun parseXml(file: File): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)

@Suppress("unused")
val amoledThemePatch = resourcePatch(
    name = "AMOLED theme",
    description = "Replaces the light theme with a dark AMOLED theme. Backgrounds become black and " +
        "dark text becomes light. Pictures (PNG, WebP) and colors set in code are not changed.",
    default = true
) {
    compatibleWith(COMPATIBILITY_STICKERLY)

    val amoledColor by stringOption(
        key = "amoledColor",
        default = "#000000",
        values = mapOf(
            "Pure black" to "#000000",
            "Near black" to "#0A0A0A",
            "Dark charcoal" to "#121212",
        ),
        title = "Background color",
        description = "The color of the screens that were white. Pure black turns the pixels off on AMOLED screens.",
        required = true,
        validator = { it != null && Regex("#[0-9A-Fa-f]{6}").matches(it) }
    )

    execute {
        val logger = Logger.getLogger("AmoledTheme")

        val resDir = get("res", false)
        if (!resDir.isDirectory) throw PatchException("The res directory was not found")

        val color = amoledColor ?: "#000000"
        val base = AmoledColors.parse(color) ?: throw PatchException("Invalid background color $color")
        val converter = AmoledConverter(resDir, base)

        fun isLibrary(file: File) = LIBRARY_PREFIXES.any { file.name.startsWith(it) }

        /** Every XML file of the directories whose name passes [dirFilter]. Night variants are never included. */
        fun xmlFiles(dirFilter: (String) -> Boolean): List<File> =
            (resDir.listFiles() ?: emptyArray())
                .filter { it.isDirectory && !it.name.contains("night") && dirFilter(it.name) }
                .sortedBy { it.name }
                .flatMap { dir -> (dir.listFiles() ?: emptyArray()).filter { it.extension == "xml" }.sortedBy { it.name } }

        /** Converts a file on a scratch copy first, so only the files that change are rewritten. */
        fun convertAll(files: List<File>, convert: (Document) -> Boolean): Int {
            var changed = 0
            for (file in files) {
                val path = "res/" + file.relativeTo(resDir).invariantSeparatorsPath
                if (!convert(parseXml(file))) continue
                document(path).use { convert(it) }
                changed++
            }
            return changed
        }

        // values: themes, the palette and the lists that can hold colors. Strings and the like have none.
        val valueFiles = xmlFiles { it.startsWith("values") }
        val styleFiles = valueFiles.filter { it.name == "styles.xml" }
        styleFiles.forEach { converter.registerStyles(parseXml(it)) }

        val themes = convertAll(styleFiles) { converter.convertStyles(it) or converter.pin(it) }
        val palette = convertAll(valueFiles.filter { it.parentFile.name == "values" && it.name == "colors.xml" }) {
            converter.flipPalette(it)
        }
        convertAll(valueFiles.filter { it.name == "arrays.xml" || it.name == "drawables.xml" || (it.name == "colors.xml" && it.parentFile.name != "values") }) {
            converter.pin(it)
        }

        val layoutFiles = xmlFiles { it.startsWith("layout") }
        val layouts = convertAll(layoutFiles.filterNot(::isLibrary)) { converter.convertLayout(it) or converter.pin(it) }
        convertAll(layoutFiles.filter(::isLibrary)) { converter.pin(it) }

        val drawableFiles = xmlFiles { it.startsWith("drawable") }
        fun keepsColors(file: File) = isLibrary(file) || KEPT_DRAWABLES.any { file.name.startsWith(it) }
        val drawables = convertAll(drawableFiles.filterNot(::keepsColors)) { converter.convertDrawable(it) or converter.pin(it) }
        convertAll(drawableFiles.filter(::keepsColors)) { converter.pin(it) }

        // Color selectors. The ones of the app are named selector_*, and are used for text.
        val colorFiles = xmlFiles { it == "color" || it.startsWith("color-") }
        val colorLists = convertAll(colorFiles.filter { it.name.startsWith("selector_") }) {
            converter.convertColorList(it) or converter.pin(it)
        }
        convertAll(colorFiles.filterNot { it.name.startsWith("selector_") }) { converter.pin(it) }

        // Everything else that can name a color: menus, XML configs, animations, adaptive icons.
        val others = convertAll(
            xmlFiles {
                !it.startsWith("values") && !it.startsWith("layout") && !it.startsWith("drawable") &&
                    it != "color" && !it.startsWith("color-") && !it.startsWith("raw")
            }
        ) { converter.pin(it) }

        // These files exist in the app. If none changed, the app changed and the patch is out of date.
        if (themes == 0 || palette == 0 || layouts == 0 || drawables == 0) {
            throw PatchException("Nothing to change: themes=$themes palette=$palette layouts=$layouts drawables=$drawables")
        }
        logger.info(
            "AMOLED theme applied: $themes style file(s), $palette palette file, $layouts layout(s), " +
                "$drawables drawable(s), $colorLists color list(s), $others other file(s)"
        )
    }
}
