package app.morphe.patches.vantasurf

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Disables the attribution and analytics SDKs.
 *
 * The application initializes Adjust, the Facebook App Events logger, the
 * advertising ID collection and AppsFlyer in a single block at the end of the
 * application onCreate method. Returning before that block keeps the app
 * working while none of the tracking SDKs are started.
 */
@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable telemetry",
    description = "Disables Adjust, AppsFlyer, Facebook App Events and advertising ID collection.",
    default = true,
) {
    compatibleWith(VantaSurfCompatibility)

    execute {
        val match = AnalyticsInitFingerprint.match()

        // The first matched instruction is the Adjust SDK initialization.
        val index = match.instructionMatches.first().index

        match.method.addInstructions(
            index,
            """
                return-void
            """,
        )
    }
}
