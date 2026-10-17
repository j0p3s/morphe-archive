package app.template.patches.truecaller.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

private const val CALLS = "$BOTTOM_BAR_BUTTON_TYPE->CALLS:$BOTTOM_BAR_BUTTON_TYPE"
private const val MESSAGES = "$BOTTOM_BAR_BUTTON_TYPE->MESSAGES:$BOTTOM_BAR_BUTTON_TYPE"
private const val SHOW_BAR_HELPER = "morpheShowBottomBarWithTwoTabs"

@Suppress("unused")
val hideMessagesTabPatch = bytecodePatch(
    name = "Hide Messages tab",
    description = "Removes the Messages tab from the bottom bar and always opens Calls. SMS " +
        "notifications still open their conversation. When Calls is the only tab left, the " +
        "bottom bar is hidden.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // 1. Remove the tab.
        hideBottomBarTabs("MESSAGES")

        // 2. The default tab is always Calls, also when the user had made Messages the default
        // (long press on the tab). Otherwise the back button keeps "returning" to the missing
        // Messages tab instead of leaving the app.
        DefaultTabFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, $CALLS
                return-object p0
            """,
        )

        // 3. Every programmatic switch to Messages (launch tab, SMS notifications and deep links
        // that open the inbox, then stack the conversation on top) selects Calls instead. Left
        // alone, selecting the missing tab on a fresh start would leave the screen empty.
        SelectTabFingerprint.method.apply {
            // v0 is a local: the method has registers besides this, the type and the flag.
            if (implementation!!.registerCount <= 3) throw PatchException("$definingClass.$name has no local register")
            addInstructionsWithLabels(
                0,
                """
                    sget-object v0, $MESSAGES
                    if-ne p1, v0, :select_tab
                    sget-object p1, $CALLS
                """,
                ExternalLabel("select_tab", getInstruction(0)),
            )
        }

        // 4. BottomBarView needs two tabs: with fewer it keeps an empty bar. A new helper hides the
        // bar in that case (only Calls left, e.g. with every other tab patch) and shows it
        // otherwise; nothing else in the app changes the bar's visibility. It is called right
        // where the builder casts its bar to BottomBarView, with the final tab list.
        val builder = BottomBarTabsFingerprint.method
        val builderClass = BottomBarTabsFingerprint.classDef
        val instructions = builder.instructions
        val take = instructions.indices.singleOrNull { index ->
            val call = (instructions[index] as? ReferenceInstruction)?.reference as? MethodReference
            instructions[index].opcode == Opcode.INVOKE_STATIC && call?.definingClass == "Lkotlin/collections/CollectionsKt;" &&
                call.returnType == "Ljava/util/List;" &&
                call.parameterTypes.map(CharSequence::toString) == listOf("Ljava/lang/Iterable;", "I")
        } ?: throw PatchException("${builderClass.type}->${builder.name} has no single take(Iterable, I) call")
        val listResult = instructions.getOrNull(take + 1)?.takeIf { it.opcode == Opcode.MOVE_RESULT_OBJECT }
            ?: throw PatchException("${builderClass.type}->${builder.name} does not keep the tab list")
        val listRegister = (listResult as OneRegisterInstruction).registerA
        val cast = (take + 2 until instructions.size).firstOrNull { index ->
            instructions[index].opcode == Opcode.CHECK_CAST &&
                ((instructions[index] as ReferenceInstruction).reference as TypeReference).type == BOTTOM_BAR_VIEW
        } ?: throw PatchException("${builderClass.type}->${builder.name} does not cast to $BOTTOM_BAR_VIEW")
        val barRegister = (instructions[cast] as OneRegisterInstruction).registerA
        val listOverwritten = (take + 2 until cast).any { index ->
            instructions[index].opcode.setsRegister() && (instructions[index] as? OneRegisterInstruction)?.registerA == listRegister
        }
        if (listOverwritten || barRegister == listRegister) {
            throw PatchException("${builderClass.type}->${builder.name} no longer holds the tab list at the BottomBarView cast")
        }
        if (barRegister > 15 || listRegister > 15) throw PatchException("Bottom bar registers are out of invoke range")

        builderClass.methods.add(
            ImmutableMethod(
                builderClass.type,
                SHOW_BAR_HELPER,
                listOf(
                    ImmutableMethodParameter(BOTTOM_BAR_VIEW, null, null),
                    ImmutableMethodParameter("Ljava/util/List;", null, null),
                ),
                "V",
                AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
                null,
                null,
                MutableMethodImplementation(4),
            ).toMutable().apply {
                addInstructionsWithLabels(
                    0,
                    """
                        invoke-interface {p1}, Ljava/util/List;->size()I
                        move-result v0
                        const/4 v1, 0x2
                        if-lt v0, v1, :hide
                        const/4 v0, 0x0
                        goto :set
                        :hide
                        const/16 v0, 0x8
                        :set
                        invoke-virtual {p0, v0}, Landroid/view/View;->setVisibility(I)V
                        return-void
                    """,
                )
            },
        )
        builder.addInstructions(
            cast + 1,
            "invoke-static {v$barRegister, v$listRegister}, ${builderClass.type}->$SHOW_BAR_HELPER(${BOTTOM_BAR_VIEW}Ljava/util/List;)V",
        )
    }
}
