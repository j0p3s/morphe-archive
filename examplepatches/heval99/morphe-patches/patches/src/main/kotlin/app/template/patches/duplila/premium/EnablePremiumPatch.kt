package app.template.patches.duplila.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.duplila.misc.license.disableLicenseCheckPatch
import app.template.patches.shared.Constants.COMPATIBILITY_DUPLILA

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Unlocks Duplila Premium: no watermark, the higher mirroring resolution and " +
        "the other premium-only actions. Premium is checked on the device only; the license " +
        "check is disabled as well so the patched app starts."
) {
    compatibleWith(COMPATIBILITY_DUPLILA)

    dependsOn(disableLicenseCheckPatch)

    execute {
        // userPaid() answers Boolean.TRUE straight away. A suspend function may return its
        // result directly, so callers resume at once: the Play purchase query and its 10 s
        // timeout never run, and startup does not wait on billing. With paid = true the app
        // itself skips the banner and never closes the main screen over refused ad consent.
        UserPaidFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent()
        )
    }
}
