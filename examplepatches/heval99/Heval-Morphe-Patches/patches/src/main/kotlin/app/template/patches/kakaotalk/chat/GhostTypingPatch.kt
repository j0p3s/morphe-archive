package app.template.patches.kakaotalk.chat

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.kakaotalk.shared.resolveByCoroutine
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK

internal const val REQUEST_ACTION_COROUTINE = "com.kakao.talk.chatroom.ChatRoomApiHelper\$Companion\$requestAction\$4"

@Suppress("unused")
val ghostTypingPatch = bytecodePatch(
    name = "Ghost typing",
    description = "Hides your typing status from other people.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // ChatRoomApiHelper.Companion.requestAction(chatId, type, logId) launches the coroutine
        // that sends the typing/action status packet; it now returns before launching it.
        resolveByCoroutine("requestAction", REQUEST_ACTION_COROUTINE) {
            RequestActionFingerprint.methodOrNull
        }.returnEarly()
    }
}
