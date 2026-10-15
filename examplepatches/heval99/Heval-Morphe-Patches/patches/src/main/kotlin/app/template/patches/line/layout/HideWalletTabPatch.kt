// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidewallettab: drops the MINI and
// WALLET tabs from the tab list builder. Always on when selected (no settings toggle).

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideWalletTabPatch = bytecodePatch(
    name = "Hide Wallet tab",
    description = "Removes the Wallet (LINE Pay) tab from the bottom navigation bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // MINI is the mini-tab layout's Wallet slot, WALLET the normal one.
        removeMainTabs("MINI", "WALLET")
    }
}
