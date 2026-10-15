package app.template.patches.line.layout

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.util.getReference
import app.template.patches.line.shared.removeConstantAdd
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/** Constants of the Chats tab header icon enum that its <clinit> must define. */
private val HEADER_ICONS = setOf("AI_FRIEND", "ALBUM", "CALENDAR", "OPEN_CHAT", "PLUS_MENU")

/**
 * The Chats tab header icon enum, taken from the header builder's own PLUS_MENU read (the one
 * button no patch removes) and checked to be an enum that defines every header icon constant.
 */
internal fun BytecodePatchContext.chatTabHeaderIconEnum(): String {
    val owners = ChatTabHeaderBuilderFingerprint.method.instructions.mapNotNull { instruction ->
        instruction.takeIf { it.opcode == Opcode.SGET_OBJECT }
            ?.getReference<FieldReference>()
            ?.takeIf { it.name == "PLUS_MENU" }
            ?.definingClass
    }.toSet()
    val iconEnum = owners.singleOrNull()
        ?: throw PatchException("Chats tab header builder reads PLUS_MENU from $owners")
    val classDef = classDefBy(iconEnum)
    val constants = classDef.staticFields.map { it.name }.toSet()
    if (classDef.superclass != "Ljava/lang/Enum;" || !constants.containsAll(HEADER_ICONS)) {
        throw PatchException("$iconEnum is not the Chats tab header icon enum")
    }
    return iconEnum
}

/** Removes [icon]'s button from the Chats tab header button list. */
internal fun BytecodePatchContext.removeChatTabHeaderButton(icon: String) {
    val iconEnum = chatTabHeaderIconEnum()
    ChatTabHeaderBuilderFingerprint.method.removeConstantAdd(icon, iconEnum)
}
