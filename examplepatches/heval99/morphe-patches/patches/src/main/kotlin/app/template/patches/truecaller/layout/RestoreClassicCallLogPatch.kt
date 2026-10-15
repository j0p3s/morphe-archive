package app.template.patches.truecaller.layout

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import app.template.patches.truecaller.shared.overrideFeatureToggle

@Suppress("unused")
val restoreClassicCallLogPatch = bytecodePatch(
    name = "Restore classic call log",
    description = "Experimental: turns off the redesigned call log, which brings back the classic " +
        "call log and its \"Frequently called\" setting.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // "featureCallLogRevamp" is read in an R8-merged toggle lambda shared with other call
        // log toggles, so it is overridden in the toggle resolver itself.
        overrideFeatureToggle("featureCallLogRevamp", false)
    }
}
