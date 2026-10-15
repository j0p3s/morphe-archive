package app.template.patches.truecaller.misc.update

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER

@Suppress("unused")
val disableUpdatePromptsPatch = bytecodePatch(
    name = "Disable update prompts",
    description = "Stops the Google Play in-app update prompts and the app's own \"update " +
        "required\" page, so the app does not block you from using the patched version.",
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // The suspend function returns a boxed Boolean ("an update flow was started"); report
        // that none was.
        StartUpdateFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object p0
            """,
        )

        // The server can also mark this version REQUIRED or DISCONTINUED, which shows a
        // full-screen page that blocks the app. Report that no update is needed.
        ForcedUpdateTypeFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/truecaller/forcedupdate/UpdateType;->NONE:Lcom/truecaller/forcedupdate/UpdateType;
                return-object v0
            """,
        )
    }
}
