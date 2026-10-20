package mightymich.morphe.patches.com.piriform.ccleaner

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string

/**
 * Disables CCleaner telemetry / analytics tracking.
 *
 * Nops out TrackingManager.trackEvent so no events are sent to
 * Avast Burger / Firebase / DomainTracker backends.
 *
 * Fingerprint uses the stable class name + unique log string
 * that appears inside the method (survives obfuscation of the method name).
 */
@Suppress("unused")
val disableTelemetryPatch = bytecodePatch(
    name = "Disable Telemetry",
    description = "Disables analytics and telemetry event tracking.",
    default = true
) {
    compatibleWith(CCleanerCompatibility.CCLEANER)

    val trackEventFingerprint = Fingerprint(
        definingClass = "Lcom/avast/android/cleaner/tracking/TrackingManager;",
        returnType = "V",
        filters = listOf(
            string("TrackingManager.trackEvent")
        )
    )

    execute {
        val method = trackEventFingerprint.method
            ?: throw PatchException(
                "Could not find TrackingManager.trackEvent (looked for log string)."
            )

        // Immediately return – no tracking is performed
        method.addInstructions(
            0,
            """
                return-void
            """
        )
    }
}
