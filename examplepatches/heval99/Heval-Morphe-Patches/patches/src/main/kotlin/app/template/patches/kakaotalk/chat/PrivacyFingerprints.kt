// Anchors for the Ghost typing, Ghost read and Strip image metadata patches. The ghost anchors
// were found by reverse engineering KakaoTalk 26.8.3; ImageResizeFingerprint is ported from
// AmpleReVanced/revanced-patches (GPL-3.0), interaction/media/ImageUploadFileFingerprint, unchanged
// apart from the name. The app classes are R8-renamed but keep their Kotlin source file names.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// ChatRoomApiHelper.Companion.requestAction(chatId, type, logId): sends the typing/action status.
// Shape fallback for the @DebugMetadata anchor; the only (J, I, J)V method in the 60 classes with
// this source file.
internal object RequestActionFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("J", "I", "J"),
    returnType = "V",
    custom = { _, classDef -> classDef.sourceFile == "ChatRoomApiHelper.kt" },
)

// ChatRoomApiHelper.Companion.requestChatOnRoom(chatRoom): shape fallback for the @DebugMetadata
// anchor. A suspend (chatRoom, continuation) method that delegates to two siblings of the same
// shape (requestChatOnRoomOpenChat and requestChatOnRoomSecretChat).
internal object RequestChatOnRoomFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L", "Lkotlin/coroutines/Continuation;"),
    returnType = "Ljava/lang/Object;",
    custom = { method, classDef ->
        classDef.sourceFile == "ChatRoomApiHelper.kt" && method.implementation?.instructions?.count { instruction ->
            val call = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            call != null && call.name != method.name && call.definingClass == classDef.type &&
                call.returnType == method.returnType &&
                call.parameterTypes.map(CharSequence::toString) == method.parameterTypes.map(CharSequence::toString)
        } == 2
    },
)

// MsgJob's request builder, which adds the "notiRead" flag to the LOCO packet.
internal object MsgJobNotiReadFingerprint : Fingerprint(
    strings = listOf("notiRead"),
    custom = { _, classDef -> classDef.sourceFile == "MsgJob.kt" },
)

// ImageItemSendHelper's static resize step (file, quality, max size) -> file, used for every
// photo sent from the gallery.
internal object ImageResizeFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/io/File;", "I", "J"),
    returnType = "Ljava/io/File;",
    custom = { _, classDef -> classDef.sourceFile == "ImageItemSendHelper.kt" },
)
