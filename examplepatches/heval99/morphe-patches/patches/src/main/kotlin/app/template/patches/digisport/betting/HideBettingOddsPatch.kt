package app.template.patches.digisport.betting

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_DIGISPORT

@Suppress("unused")
val hideBettingOddsPatch = bytecodePatch(
    name = "Hide betting odds",
    description = "Hides the bookmaker odds block in soccer match rows.",
) {
    compatibleWith(COMPATIBILITY_DIGISPORT)

    execute {
        // Every match row reads as having no odds, so its odds block stays GONE.
        UIResultsMatchGetBetFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """,
        )
    }
}
