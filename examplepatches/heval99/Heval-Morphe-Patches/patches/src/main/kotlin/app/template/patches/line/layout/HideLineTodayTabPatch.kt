// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidelinetodaytab: drops the NEWS and
// NEWS_ROW tabs from the tab list builder. Always on when selected (no settings toggle), and the
// pairs are removed instead of skipped behind a switch.

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideLineTodayTabPatch = bytecodePatch(
    name = "Hide LINE TODAY tab",
    description = "Removes the LINE TODAY (News) tab from the bottom navigation bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // NEWS is the news tab, NEWS_ROW its news-row variant; the builder adds one or the other.
        removeMainTabs("NEWS", "NEWS_ROW")
    }
}
