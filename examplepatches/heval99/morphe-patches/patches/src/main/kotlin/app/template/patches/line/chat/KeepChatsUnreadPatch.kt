// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/keepunread: no-ops the read manager's
// open-on-view wrapper. Re-anchored on the "sendChatChecked" Thrift call (its TalkServiceClient
// method name drifted), the wrapper is selected with single instead of first, and it is always on
// (no settings toggle).

package app.template.patches.line.chat

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val keepChatsUnreadPatch = bytecodePatch(
    name = "Keep chats unread",
    description = "Opening a 1:1 or group chat no longer marks it read or sends read receipts. " +
        "Mark as read still works.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // 1. The sendChatChecked lambda names, through its EnclosingMethod, the client method
        //    that sends a receipt.
        val sendMethod = sendChatCheckedMethod()

        // 2. The read worker is the caller of that client method; the call must be the same one.
        val workerCall = ReadWorkerFingerprint.instructionMatches.first().instruction.getReference<MethodReference>()
        if (workerCall?.name != sendMethod.name ||
            workerCall.parameterTypes.map(CharSequence::toString) != sendMethod.parameterTypes.map(CharSequence::toString)
        ) {
            throw PatchException("Read worker calls $workerCall, but sendChatChecked is sent by $sendMethod")
        }

        // 3. Opening a chat reaches the worker only through the read manager's (String, String)
        //    wrapper (chat id, last message id). Manual "mark as read" uses other entry points.
        //    Returning at once skips both the local read and the receipt.
        val readManager = ReadWorkerFingerprint.classDef
        readManager.methods.single { method ->
            method.returnType == "V" && method.implementation != null &&
                method.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }.addInstructions(0, "return-void")
    }
}
