// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/chatheaderbuttons/
// HideAiFriendsButtonPatch.kt: turns off the function.ai.character.display_button server flag.

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.line.shared.forceServerFlagFalse
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideAiFriendsButtonPatch = bytecodePatch(
    name = "Hide AI friends button",
    description = "Removes the AI friends button from the Chats tab header. The Album button " +
        "shows in its place.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // The flag, not the button: the header builder and the album green dot read the same
        // flag, and all readers must agree that the feature is off.
        forceServerFlagFalse(AiFriendsFlagFingerprint)
    }
}
