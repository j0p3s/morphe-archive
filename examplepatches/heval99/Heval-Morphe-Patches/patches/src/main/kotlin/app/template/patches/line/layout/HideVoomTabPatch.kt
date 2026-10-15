// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidevoomtab: drops the TIMELINE
// (VOOM) tab from the tab list builder. Always on when selected (no settings toggle).

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideVoomTabPatch = bytecodePatch(
    name = "Hide VOOM tab",
    description = "Removes the VOOM tab from the bottom navigation bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        removeMainTabs("TIMELINE")
    }
}
