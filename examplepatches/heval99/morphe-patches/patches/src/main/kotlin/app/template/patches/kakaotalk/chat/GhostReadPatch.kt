package app.template.patches.kakaotalk.chat

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.template.patches.kakaotalk.shared.resolveByCoroutine
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

internal const val CHAT_ROOM_API_HELPER_COMPANION = "com.kakao.talk.chatroom.ChatRoomApiHelper\$Companion"

@Suppress("unused")
val ghostReadPatch = bytecodePatch(
    name = "Ghost read",
    description = "Experimental: others won't see that you read their messages while you only view " +
        "chats. Sending a message or reacting should still mark the chat read, read counts on your own " +
        "messages may update late, and secret chats or open chats may behave differently. Not " +
        "verified on a real account.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. ChatRoomApiHelper.Companion.requestChatOnRoom (the "I opened this room" request)
        // returns Boolean.FALSE without sending, the result it already gives for a background room.
        resolveByCoroutine("requestChatOnRoom", CHAT_ROOM_API_HELPER_COMPANION, "requestChatOnRoom") {
            RequestChatOnRoomFingerprint.methodOrNull
        }.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """,
        )

        // 2. MsgJob: builder.put("notiRead", Boolean.valueOf(...)) always puts FALSE. The value is
        // the third register of the put call right after the const-string.
        MsgJobNotiReadFingerprint.method.apply {
            val keyIndex = instructions.indexOfFirst {
                it.opcode == Opcode.CONST_STRING && it.getReference<StringReference>()?.string == "notiRead"
            }
            val put = instructions.getOrNull(keyIndex + 1)
            val putCall = put?.getReference<MethodReference>()
            if (keyIndex < 0 || put !is FiveRegisterInstruction || put.registerCount != 3 ||
                put.opcode != Opcode.INVOKE_VIRTUAL ||
                putCall?.parameterTypes?.map(CharSequence::toString) != listOf("Ljava/lang/String;", "Ljava/lang/Object;") ||
                put.registerD != (instructions[keyIndex] as OneRegisterInstruction).registerA
            ) {
                throw PatchException("No put(\"notiRead\", value) call in $definingClass->$name")
            }
            addInstructions(keyIndex, "sget-object v${put.registerE}, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;")
        }
    }
}
