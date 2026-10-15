// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidecalendar: only its Chats tab
// header part (the CALENDAR + add pair). The attach menu, chat room toolbar, context menu and Home
// parts are not ported.

package app.template.patches.line.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE

@Suppress("unused")
val hideCalendarButtonPatch = bytecodePatch(
    name = "Hide calendar button",
    description = "Removes the calendar button from the Chats tab header.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        removeChatTabHeaderButton("CALENDAR")
    }
}
