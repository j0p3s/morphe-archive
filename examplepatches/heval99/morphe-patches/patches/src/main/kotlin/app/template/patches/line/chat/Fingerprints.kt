// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/keepunsent/Fingerprints.kt and
// patches/line/keepunread/Fingerprints.kt. The read worker is now cross-checked against the
// "sendChatChecked" Thrift call, since the TalkServiceClient method name drifts (c1 in 26.14.0).

package app.template.patches.line.chat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.value.MethodEncodedValue

internal const val TALK_SERVICE_CLIENT = "Ljp/naver/line/android/thrift/client/TalkServiceClient;"

// The lambda that applies an incoming unsend to the local database, run inside a chat_history
// transaction. Every destructive write (type -> UNSENT, NULLed content, search index, reactions
// and multiple-image rows) sits behind one "is this row already an unsend tombstone?" guard.
// The four SQL literals are unique to it in this order.
internal object UnsendMessageDbWriteFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("reactions"),
        string("server_message_id=?"),
        string("multiple_image_message_mapping"),
        string("local_message_id = ?"),
    ),
)

// The Thrift call that sends a read receipt: sendChatChecked(seq, chatId, lastMessageId). Its
// lambda's EnclosingMethod is the TalkServiceClient implementation that the read worker calls.
internal object SendChatCheckedFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("L"),
    strings = listOf("sendChatChecked"),
)

// The 1:1/group read worker (messageId, chatId, ...): it marks the chat read locally and sends the
// receipt through TalkServiceClient's (int, String, String) call.
internal object ReadWorkerFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("J", "Ljava/lang/String;", "Z"),
    filters = listOf(
        methodCall(
            definingClass = TALK_SERVICE_CLIENT,
            parameters = listOf("I", "Ljava/lang/String;", "Ljava/lang/String;"),
            returnType = "V",
        ),
    ),
)

// ChatHistoryActivity.onCreate: super.onCreate first, then it wires up its component providers.
internal object ChatHistoryOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf("chatDataComponentProvider"),
)

// The chat launch dispatcher (intent, flag) that starts ChatHistoryActivity, used by message
// notifications among others; whether a launch came from a notification is read from the request
// in the intent's "chatHistoryRequest" extra (a string of the dispatcher's class).
internal object ChatLaunchDispatchFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Intent;", "Z"),
    strings = listOf("shouldLaunchMainActivity", "shouldLaunchChatHistoryActivityOnly", "CHAT_LIST_MOVE_ALL_TAB"),
)

// ChatHistoryRequest's back-navigation args: its isFromNotification flag is the class's only
// public instance boolean.
internal object BackNavigationArgsToStringFingerprint : Fingerprint(
    name = "toString",
    strings = listOf("BackNavigationArgs(isFromNotification="),
)

// One row of the chat-list long-press menu: (label string resource, action). The action type is
// the menu's sealed action class (Hide, ToggleMute, TogglePin, MarkAsRead, Delete, ...).
internal object ChatRowMenuItemToStringFingerprint : Fingerprint(
    name = "toString",
    strings = listOf("ChatRowMenuItem(labelRes=", ", action="),
)

// The long-pressed chat as the menu sees it: chat id, chat type enum and state flags.
internal object ChatPreviewItemDataToStringFingerprint : Fingerprint(
    name = "toString",
    strings = listOf("ChatPreviewItemData(chatId=", ", chatType="),
)

/**
 * The TalkServiceClient implementation of sendChatChecked (int, chatId, lastMessageId), named by
 * the EnclosingMethod of the lambda that holds the "sendChatChecked" Thrift name.
 */
internal fun BytecodePatchContext.sendChatCheckedMethod(): MethodReference {
    val sendLambda = SendChatCheckedFingerprint.originalClassDef
    return (sendLambda.annotations.singleOrNull { it.type == "Ldalvik/annotation/EnclosingMethod;" }
        ?.elements?.singleOrNull()?.value as? MethodEncodedValue)?.value
        ?: throw PatchException("${sendLambda.type} has no EnclosingMethod")
}
