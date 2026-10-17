package app.linkedin.patches.theme

import app.linkedin.patches.shared.Constants.COMPATIBILITY_LINKEDIN
import app.morphe.patcher.patch.resourcePatch

/**
 * LinkedIn's blue palette (blue_* and the Mercado design system's mercado_mvp_blue_*) mapped to
 * the system accent palette. Tone 70 is the action color on light backgrounds, tone 50 on dark ones.
 */
private val ACCENT_SHADES = mapOf(
    "10" to "10",
    "20" to "50",
    "30" to "100",
    "40" to "200",
    "50" to "200",
    "60" to "400",
    "70" to "600",
    "80" to "700",
    "90" to "800",
    "100" to "900",
)

@Suppress("unused")
val materialYouPatch = resourcePatch(
    name = "Material You",
    description = "Uses your wallpaper colors (Android 12+) instead of LinkedIn blue. " +
        "Some screens drawn by LinkedIn's servers may stay blue.",
    default = false
) {
    compatibleWith(COMPATIBILITY_LINKEDIN)

    execute {
        // values-v31: the system accent colors only exist on Android 12+, older versions keep LinkedIn blue.
        val directory = get("res").resolve("values-v31").apply { mkdirs() }
        val colors = buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine("<resources>")
            for ((tone, accent) in ACCENT_SHADES) {
                for (prefix in listOf("blue_", "mercado_mvp_blue_")) {
                    // blue_90 does not exist in LinkedIn's palette.
                    if (prefix == "blue_" && tone == "90") continue
                    if (prefix == "mercado_mvp_blue_" && tone == "100") continue
                    appendLine("""    <color name="$prefix$tone">@android:color/system_accent1_$accent</color>""")
                }
            }
            appendLine("</resources>")
        }
        directory.resolve("colors.xml").writeText(colors)
    }
}
