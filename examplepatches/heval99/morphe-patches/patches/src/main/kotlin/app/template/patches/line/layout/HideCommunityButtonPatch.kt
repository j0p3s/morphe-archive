// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/chatheaderbuttons/
// HideCommunityButtonPatch.kt: removes the OPEN_CHAT + add pair from the Chats tab header builder.
// The enum and the builder are now derived (constant names, createEndButtons) instead of pinned.

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideCommunityButtonPatch = bytecodePatch(
    name = "Hide community button",
    description = "Removes the OpenChat (community) button from the Chats tab header.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        removeChatTabHeaderButton("OPEN_CHAT")
    }
}
