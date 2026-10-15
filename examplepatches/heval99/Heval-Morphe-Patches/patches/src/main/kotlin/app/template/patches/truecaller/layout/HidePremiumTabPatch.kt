package app.template.patches.truecaller.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER

@Suppress("unused")
val hidePremiumTabPatch = bytecodePatch(
    name = "Hide Premium tab",
    description = "Removes the Premium tab from the bottom bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // Links that open the Premium tab (upsell buttons) then leave the current tab selected:
        // selecting a tab that is not in the bar does nothing.
        hideBottomBarTabs("PREMIUM")
    }
}
