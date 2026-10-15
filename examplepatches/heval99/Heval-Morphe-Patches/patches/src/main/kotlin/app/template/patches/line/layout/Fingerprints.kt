// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidelinetodaytab, hideshoppingtab,
// hidevoomtab, hidewallettab, chatheaderbuttons, hidenewbadges, hideagenti and disablevoom
// Fingerprints.kt. Re-anchored for 26.15.1 without R8-renamed class names or resource ids: the tab
// enum and the header icon enum are derived from their constant names, the header builder from
// its "createEndButtons" reference, the green dot set from its coroutine metadata, and the Home
// Agent i button from its server flag instead of the access_agenti label id.

package app.template.patches.line.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import app.template.patches.line.shared.ServerFlagFingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

internal const val HEADER_BUTTON = "Ljp/naver/line/android/common/view/header/HeaderButton;"

// The main bottom-navigation tab list builder (()List of the main tab enum). It adds each tab as
// `sget-object <tab>` + ArrayList.add. Anchored on HOME, CHAT, SQUARE and CALL, which no patch
// removes, so every tab patch still matches after another one has edited the method.
internal object MainTabListFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    parameters = listOf(),
    filters = listOf(
        fieldAccess(name = "HOME", opcode = Opcode.SGET_OBJECT),
        fieldAccess(name = "CHAT", opcode = Opcode.SGET_OBJECT),
        fieldAccess(name = "SQUARE", opcode = Opcode.SGET_OBJECT),
        fieldAccess(name = "CALL", opcode = Opcode.SGET_OBJECT),
    ),
)

// ChatTabHeaderStateImpl's constructor builds the Chats tab header button list (AI_FRIEND or
// ALBUM, CALENDAR, OPEN_CHAT, PLUS_MENU) and wires it into createEndButtons.
internal object ChatTabHeaderBuilderFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    strings = listOf("createEndButtons"),
)

// ChatTabHeaderStateImpl$greenDotVisibleIconSet$1: the set of header icons that get a green dot.
// Found by its Kotlin coroutine metadata (c = class, m = method). LINE's R8 renames the
// DebugMetadata annotation type itself, so only its element values are compared.
internal object ChatTabGreenDotSetFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    custom = { _, classDef ->
        classDef.annotations.any { annotation ->
            fun value(name: String) =
                (annotation.elements.firstOrNull { it.name == name }?.value as? StringEncodedValue)?.value
            value("c") == "com.linecorp.line.chattab.header.ChatTabHeaderStateImpl\$greenDotVisibleIconSet\$1" &&
                value("m") == "invokeSuspend"
        }
    },
)

// The AI Friends header button. The builder adds AI_FRIEND when this flag is on, ALBUM otherwise.
internal object AiFriendsFlagFingerprint : ServerFlagFingerprint("function.ai.character.display_button")

// Agent i (LINE AI). The Home header shows its button only when this flag is on.
internal object AgentIFlagFingerprint : ServerFlagFingerprint("function.ai.agenti.enabled")

// The Agent i entry at the end of the search bar.
internal object SearchBarAgentIFlagFingerprint : ServerFlagFingerprint("function.search.line_ai_entry.enabled")

// HeaderButton's dot setter (visibility, theme): the only place a header "new" dot is shown.
// It calls the reset method first, then shows the green or the red dot.
internal object HeaderDotFingerprint : Fingerprint(
    definingClass = HEADER_BUTTON,
    returnType = "V",
    parameters = listOf("I", "Z"),
)

// The chat room header badge renderer, at its "N" branch: the only code outside HeaderButton that
// reads one of its ImageView fields (header_n_badge) and shows it.
internal object HeaderNBadgeFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        fieldAccess(definingClass = HEADER_BUTTON, type = "Landroid/widget/ImageView;", opcode = Opcode.IGET_OBJECT),
        methodCall(name = "setVisibility", location = MatchAfterImmediately()),
    ),
    custom = { _, classDef -> classDef.type != HEADER_BUTTON },
)

internal object SettingsTextItemNewBadgeFingerprint : Fingerprint(
    definingClass = "Lcom/linecorp/line/settings/base/itemview/LineUserSettingTextItemView;",
    name = "setNewBadgeVisible",
    returnType = "V",
    parameters = listOf("Z"),
)

internal object SettingsSwitchItemNewMarkFingerprint : Fingerprint(
    definingClass = "Lcom/linecorp/line/settings/base/itemview/LineUserSettingSwitchItemView;",
    name = "setIsNewMarkVisible",
    returnType = "V",
    parameters = listOf("Z"),
)

// The Home service badge enum (NEW, UPDATE, EVENT, EXPIRED, NONE). Its isVisible accessor gates
// the badge on Home service tiles, the service list and the shortcuts row.
internal object ServiceBadgeEnumFingerprint : Fingerprint(
    name = "<clinit>",
    returnType = "V",
    filters = listOf(
        fieldAccess(name = "NEW", opcode = Opcode.SPUT_OBJECT),
        fieldAccess(name = "UPDATE", opcode = Opcode.SPUT_OBJECT),
        fieldAccess(name = "EVENT", opcode = Opcode.SPUT_OBJECT),
        fieldAccess(name = "EXPIRED", opcode = Opcode.SPUT_OBJECT),
    ),
)

// The shared friend/group/birthday row binder: it compares the row's badge kind with
// GREEN_DOT_BADGE and sets the green dot's visibility from that.
internal object FriendRowGreenDotFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        fieldAccess(name = "GREEN_DOT_BADGE", opcode = Opcode.SGET_OBJECT),
        methodCall(name = "setVisibility"),
    ),
)

// The line://home/ scheme handler (VOOM's scheme host), found by two VOOM path segments.
internal object VoomSchemeHandlerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "L"),
    filters = listOf(
        string("hashtaglist"),
        string("deleteCache"),
    ),
)

// The standalone VOOM feed, which the notification center opens without the scheme router.
internal object LineVoomActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/linecorp/line/timeline/notification/impl/activity/LineVoomActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    filters = listOf(methodCall(name = "onCreate", opcode = Opcode.INVOKE_SUPER)),
)
