// Ported from AmpleReVanced/revanced-patches (GPL-3.0): layout/subdevice/Fingerprints.kt,
// layout/chatroom/fingerprints/Remove300PlusLimitFingerprint.kt and
// interaction/chatlog/fingerprints/Remove99ClampFingerprint.kt, written for KakaoTalk 26.7.2.
// Changes for 26.8.3: the Universal mobile-only check is located through the MobileOnlyCover
// state (its toString) instead of the first matching call, which in 26.8.3 is the lock check;
// the watermark fingerprint also requires the 99 clamp.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val ALIMTALK_TYPE_COMPANION = "Lcom/kakao/talk/bubble/alimtalk/AlimtalkType\$Companion;"
internal const val UNIVERSAL_PUBLIC_DATA = "Lcom/kakao/talk/bubble/universal/validator/UniversalPublicData;"

// Cover state shown over AlimTalk bubbles on a sub device (tablet/PC login): returns the
// obfuscated cover enum, NONE meaning "show the message".
internal object AlimtalkCoverStateFingerprint : Fingerprint(
    definingClass = ALIMTALK_TYPE_COMPANION,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(fieldAccess(name = "NONE", opcode = Opcode.SGET_OBJECT)),
)

// The same for Leverage bubbles (LeverageType companion), which defers to the AlimTalk check.
internal object LeverageCoverStateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z", "Z"),
    filters = listOf(
        fieldAccess(name = "NONE", opcode = Opcode.SGET_OBJECT),
        methodCall(definingClass = ALIMTALK_TYPE_COMPANION, parameters = listOf("Z"), opcode = Opcode.INVOKE_VIRTUAL),
    ),
)

// Universal bubbles: the validator's state method, which maps the message to a cover state with
// "if (check()) return new XxxCover(data)" branches.
internal object UniversalCoverStateFingerprint : Fingerprint(
    filters = listOf(
        methodCall(definingClass = UNIVERSAL_PUBLIC_DATA, parameters = listOf(), returnType = "Z", opcode = Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately()),
        opcode(Opcode.IF_EQZ, location = MatchAfterImmediately()),
        opcode(Opcode.NEW_INSTANCE, location = MatchAfterImmediately()),
    ),
    custom = { _, classDef -> classDef.sourceFile == "UniversalDataValidator.kt" },
)

// The mobile-only cover state class, found by its data class toString.
internal object MobileOnlyCoverToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf("MobileOnlyCover(data="),
)

// Chat list unread badge: the regular and OpenChat room items show "300+" from 300 on, and the
// room's unread count getter clamps with Math.min(count, 300).
internal object Limit300PlusBaseChatRoomFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    strings = listOf("300+"),
    custom = { _, classDef -> classDef.sourceFile == "BaseChatRoomItemViewHolder.kt" },
)

internal object Limit300PlusOpenChatRoomFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf(),
    strings = listOf("300+"),
    custom = { _, classDef -> classDef.sourceFile == "OpenLinkChatsItem.kt" },
)

internal object GetUnreadCountFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.DECLARED_SYNCHRONIZED),
    returnType = "I",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.MONITOR_ENTER,
        Opcode.IGET,
        Opcode.IGET_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.MOVE_RESULT,
        Opcode.ADD_INT_2ADDR,
        Opcode.CONST_16,
    ),
    custom = { _, classDef -> classDef.sourceFile == "UnreadCountInfo.kt" },
)

// In-chat read counter (members who have not read a message yet): the chat log item and the
// watermark cache both cap it at 99.
internal object ProcessWatermarkCountFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "I",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.SGET_OBJECT,
        Opcode.IF_NE,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    ) + literal(99, opcodes = listOf(Opcode.CONST_16)),
    custom = { _, classDef -> classDef.sourceFile == "ChatLogRecyclerItem.kt" },
)

internal object GetWatermarkCountFromCacheFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L", "J", "J"),
    returnType = "I",
    filters = listOf(
        literal(99, opcodes = listOf(Opcode.CONST_16)),
        opcode(Opcode.IF_LE, location = MatchAfterImmediately()),
        opcode(Opcode.MOVE, location = MatchAfterImmediately()),
    ),
    custom = { _, classDef -> classDef.sourceFile == "WatermarksManager.kt" },
)
