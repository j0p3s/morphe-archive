// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hideagenti: the search bar flag.
// The Home header part now turns off the function.ai.agenti.enabled flag that feeds the header's
// Agent i boolean, instead of matching the access_agenti label's resource id.

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.line.shared.forceServerFlagFalse
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val disableAgentIPatch = bytecodePatch(
    name = "Disable Agent i",
    description = "Turns off Agent i, LINE's AI assistant, which removes its buttons from the " +
        "Home header and the search bar.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // Both flags read false, as for an account without Agent i: the Home header skips the
        // icon and the search bar hides its entry and moves its guideline back. The agenti flag
        // is Agent i's master switch, so its other readers (AI talk suggestions, the image edit
        // intro, AI subscription screens) also see it off.
        forceServerFlagFalse(AgentIFlagFingerprint)
        forceServerFlagFalse(SearchBarAgentIFlagFingerprint)
    }
}
