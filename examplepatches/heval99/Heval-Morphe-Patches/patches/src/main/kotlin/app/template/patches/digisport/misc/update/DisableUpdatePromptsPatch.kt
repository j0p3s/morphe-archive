package app.template.patches.digisport.misc.update

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_DIGISPORT

@Suppress("unused")
val disableUpdatePromptsPatch = bytecodePatch(
    name = "Disable update prompts",
    description = "Stops the Play Store in-app update prompt. An old version may still stop " +
        "working if Digi Sport's servers drop support for it.",
) {
    compatibleWith(COMPATIBILITY_DIGISPORT)

    execute {
        // The app never asks Play for an update, so no in-app update flow starts.
        CheckUpdateFingerprint.method.returnEarly()
    }
}
