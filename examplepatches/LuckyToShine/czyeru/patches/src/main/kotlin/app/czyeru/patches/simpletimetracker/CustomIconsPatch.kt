package app.czyeru.patches.simpletimetracker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.czyeru.patches.shared.Constants.COMPATIBILITY_SIMPLE_TIME_TRACKER
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val EXTENSION_CLASS = "Lapp/czyeru/extension/simpletimetracker/CustomIcons;"
private const val ICON_IMAGE_REPO = "Lcom/example/util/simpletimetracker/core/repo/IconImageRepo;"
private const val ICON_VIEW = "Lcom/example/util/simpletimetracker/feature_views/IconView;"
private const val ICON_VIEW_BINDING =
    "Lcom/example/util/simpletimetracker/feature_views/databinding/IconViewLayoutBinding;"
private const val ICON_SELECTION_VIEW_DATA =
    "Lcom/example/util/simpletimetracker/feature_icon_selection/api/viewData/IconSelectionViewData;"

@Suppress("unused")
val customIconsPatch = bytecodePatch(
    name = "Custom icons",
    description = "Adds your own vector icons (.xml files) to the icon picker. Put the files in " +
        "Android/data/com.razeeman.util.simpletimetracker/files/custom_icons, then open the icon " +
        "picker. They are at the end of the list and show in your activity color.",
    default = true
) {
    compatibleWith(COMPATIBILITY_SIMPLE_TIME_TRACKER)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. Add the files to the icon picker. getImages has 5 locals (v0-v4). v0 holds the map
        // from the first instruction on and is returned, v1 and v2 are free at the return.
        // The custom icons go into the last group (R.array.icon_toggle), after the built-in
        // icons, so the search words (matched by position) of the built-in icons stay correct.
        GetImagesFingerprint.method.apply {
            val returnIndex = implementation!!.instructions
                .indexOfLast { it.opcode == Opcode.RETURN_OBJECT }
            if (returnIndex < 0) throw PatchException("IconImageRepo.getImages has no return.")

            addInstructions(
                returnIndex,
                """
                    iget-object v1, p0, $ICON_IMAGE_REPO->context:Landroid/content/Context;
                    sget v2, Lcom/example/util/simpletimetracker/resources/R${'$'}drawable;->app_unknown:I
                    invoke-static {v1, v0, v2}, $EXTENSION_CLASS->endImages(Landroid/content/Context;Ljava/util/Map;I)V
                """
            )
            // v0 is not assigned before the original code does it.
            addInstructions(
                0,
                """
                    sget v0, Lcom/example/util/simpletimetracker/core/R${'$'}array;->icon_toggle:I
                    invoke-static {p1, v0}, $EXTENSION_CLASS->beginImages(II)V
                """
            )
        }

        // 2. Show a custom icon wherever the app shows an icon with IconView. The saved name
        // "xml:file.xml" is not an "ic_" name, so the app passes it on as text.
        // setTextIcon has 2 locals (v0, v1), both assigned by the original code before use.
        SetTextIconFingerprint.method.addInstructionsWithLabels(
            0,
            """
                iget-object v0, p0, $ICON_VIEW->binding:$ICON_VIEW_BINDING
                iget-object v1, v0, $ICON_VIEW_BINDING->ivIconViewImage:Landroidx/appcompat/widget/AppCompatImageView;
                iget-object v0, v0, $ICON_VIEW_BINDING->tvIconViewEmoji:Landroidx/appcompat/widget/AppCompatTextView;
                invoke-static {v1, v0, p1}, $EXTENSION_CLASS->showIconText(Landroid/widget/ImageView;Landroid/widget/TextView;Ljava/lang/String;)Z
                move-result v0
                if-eqz v0, :original
                return-void
            """,
            ExternalLabel("original", SetTextIconFingerprint.method.getInstruction(0))
        )

        // 3. Show the real icon in the picker cells too. The cell is bound with the placeholder
        // drawable, then setTag. v1 is the cell's image view, v0 is free after setTag.
        BindIconSelectionItemFingerprint.method.apply {
            val setTagIndex = implementation!!.instructions.indexOfFirst {
                it.opcode == Opcode.INVOKE_VIRTUAL &&
                    ((it as ReferenceInstruction).reference as MethodReference).name == "setTag"
            }
            if (setTagIndex < 0) throw PatchException("Icon picker cell has no setTag call.")

            addInstructions(
                setTagIndex + 1,
                """
                    move-object v0, p2
                    check-cast v0, $ICON_SELECTION_VIEW_DATA
                    invoke-virtual {v0}, $ICON_SELECTION_VIEW_DATA->getIconName()Ljava/lang/String;
                    move-result-object v0
                    invoke-static {v1, v0}, $EXTENSION_CLASS->showPickerIcon(Landroid/widget/ImageView;Ljava/lang/String;)V
                """
            )
        }
    }
}
