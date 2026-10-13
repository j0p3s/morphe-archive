package app.czyeru.patches.irisgallery

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.czyeru.patches.shared.Constants.COMPATIBILITY_IRIS_GALLERY

private const val EXTENSION_CLASS = "Lapp/czyeru/extension/securefolder/SecureFolderPatch;"

@Suppress("unused")
val secureFolderPatch = bytecodePatch(
    name = "Move to Secure Folder",
    description = "Adds Samsung Secure Folder as a destination when moving items to Locked. " +
        "Only active on devices where Secure Folder is available.",
    default = true
) {
    compatibleWith(COMPATIBILITY_IRIS_GALLERY)

    extendWith("extensions/extension.mpe")

    execute {
        MoveToLockedHandlerFingerprint.method.apply {
            // The method has 18 locals, so p0 and p1 are v18 and v19. Most instructions can only
            // address v0-v15, so the parameters are copied to low registers first. v0-v2 are
            // free at this point because the original code assigns them again before reading.
            //
            // Only the vault variant (field i == 2) is intercepted. If the extension returns
            // true it has taken over and the click is reported as handled (kotlin.Unit).
            addInstructionsWithLabels(
                0,
                """
                    move-object/from16 v0, p0
                    iget v1, v0, Ltj1;->i:I
                    const/4 v2, 0x2
                    if-ne v1, v2, :original
                    iget-object v1, v0, Ltj1;->o:Landroid/content/Context;
                    move-object/from16 v2, p1
                    invoke-static {v0, v1, v2}, $EXTENSION_CLASS->interceptMoveToLocked(Ljava/lang/Object;Landroid/content/Context;Ljava/lang/Object;)Z
                    move-result v1
                    if-eqz v1, :original
                    sget-object v1, Lv93;->a:Lv93;
                    return-object v1
                """,
                ExternalLabel("original", getInstruction(0))
            )
        }
    }
}
