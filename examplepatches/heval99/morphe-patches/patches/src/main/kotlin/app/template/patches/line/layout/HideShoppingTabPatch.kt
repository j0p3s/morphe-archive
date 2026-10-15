// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hideshoppingtab: drops the COMMERCE
// and COMMERCE_TW tabs from the tab list builder. Always on when selected (no settings toggle).

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideShoppingTabPatch = bytecodePatch(
    name = "Hide Shopping tab",
    description = "Removes the Shopping tab (Discover in Taiwan) from the bottom navigation bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // COMMERCE (Japan) and COMMERCE_TW (Taiwan) share one if/else-if slot. With the pair gone
        // the branch still jumps past the OpenChat and VOOM alternatives, like stock LINE when
        // the commerce flag is on, so no other tab moves into the slot.
        removeMainTabs("COMMERCE", "COMMERCE_TW")
    }
}
