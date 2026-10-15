// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/hidenewbadges: its core sites (header
// dots, the chat room "N" badge, settings rows, the Home service badge enum, the friend row green
// dot) and the Chats tab green dot set. Every site now fails the patch when it does not match
// (the reference skipped most of them silently). Not ported: the chat menu row and album row
// holders and the bottom navigation "new" badge, which only resource ids tell apart (in 26.15.1
// the reference's bottom-bar id 0x7f0b03b5 is bnb_button_badge_live, not the "new" badge), and the
// attach button dot, whose accessor was not located.

package app.template.patches.line.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.returnEarly
import app.template.patches.line.shared.forceGoneAt
import app.template.patches.line.shared.removeConstantAdd
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

@Suppress("unused")
val hideNewBadgesPatch = bytecodePatch(
    name = "Hide new badges",
    description = "Hides the green dots and N badges that mark new items on header buttons, " +
        "the Chats tab header, settings rows, Home services and friend rows. Unread counts stay.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // 1. Header "new" dots: HeaderButton's (visibility, theme) setter resets every badge view,
        //    then shows the green or the red dot. Forcing the visibility to GONE keeps the reset.
        HeaderDotFingerprint.method.apply {
            if (instructions.none { it.getReference<MethodReference>()?.name == "setVisibility" }) {
                throw PatchException("HeaderButton (I, Z) setter does not set a visibility")
            }
            addInstructions(0, "const/16 p1, 0x8")
        }

        // 2. The circled "N" on the chat room header: only the visibility of the N view's
        //    setVisibility changes; the count branch in the same method keeps working.
        HeaderNBadgeFingerprint.method.forceGoneAt(HeaderNBadgeFingerprint.instructionMatches[1].index)

        // 3-4. Settings rows: the "new" argument reads false, so each setter takes its hide branch.
        SettingsTextItemNewBadgeFingerprint.method.addInstructions(0, "const/4 p1, 0x0")
        SettingsSwitchItemNewMarkFingerprint.method.addInstructions(0, "const/4 p1, 0x0")

        // 5. Home services: the badge enum's isVisible accessor returns false.
        ServiceBadgeEnumFingerprint.classDef.methods.single { method ->
            method.returnType == "Z" && method.parameterTypes.isEmpty() &&
                method.implementation?.instructions?.any {
                    it.opcode == Opcode.IGET_BOOLEAN && it.getReference<FieldReference>()?.name == "isVisible"
                } == true
        }.returnEarly(false)

        // 6. Friend rows: the green dot's setVisibility right after the `badge == GREEN_DOT_BADGE`
        //    comparison always passes GONE. The binder's other GREEN_DOT_BADGE read (assigning the
        //    badge kind) is not followed by a comparison.
        FriendRowGreenDotFingerprint.method.apply {
            val comparisons = instructions.indices.filter { index ->
                instructions[index].opcode == Opcode.SGET_OBJECT &&
                    instructions[index].getReference<FieldReference>()?.name == "GREEN_DOT_BADGE" &&
                    instructions.getOrNull(index + 1)?.opcode in setOf(Opcode.IF_NE, Opcode.IF_EQ)
            }
            val comparison = comparisons.singleOrNull()
                ?: throw PatchException("Expected one GREEN_DOT_BADGE comparison, found ${comparisons.size}")
            val setVisibility = (comparison until instructions.size).firstOrNull {
                instructions[it].getReference<MethodReference>()?.name == "setVisibility"
            } ?: throw PatchException("No setVisibility after the GREEN_DOT_BADGE comparison")
            forceGoneAt(setVisibility)
        }

        // 7. Chats tab header green dots: the set of icons that get a dot stays empty.
        val iconEnum = chatTabHeaderIconEnum()
        ChatTabGreenDotSetFingerprint.method.apply {
            listOf("ALBUM", "OPEN_CHAT", "AI_FRIEND").forEach { removeConstantAdd(it, iconEnum) }
        }
    }
}
