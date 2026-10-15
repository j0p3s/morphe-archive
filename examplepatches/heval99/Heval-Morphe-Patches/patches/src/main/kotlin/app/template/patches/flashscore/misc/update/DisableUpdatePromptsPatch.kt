package app.template.patches.flashscore.misc.update

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_FLASHSCORE

@Suppress("unused")
val disableUpdatePromptsPatch = bytecodePatch(
    name = "Disable update prompts",
    description = "Hides the \"update available\" and \"update required\" dialogs. An old " +
        "version may still stop working if Flashscore's servers drop support for it.",
) {
    compatibleWith(COMPATIBILITY_FLASHSCORE)

    execute {
        // Both the splash screen and the event list show the dialog only through this method.
        UpdateDialogFingerprint.method.returnEarly()
    }
}
