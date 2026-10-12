package app.morphe.patches.toffee.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.network.hideVPNPatch
import app.morphe.patches.toffee.shared.Constants.COMPATIBILITY_TOFFEE

@Suppress("unused")
val hideVPNPatch = rawResourcePatch(
    name = hideVPNPatch.name,
    description = hideVPNPatch.description,
    default = true,
) {
    compatibleWith(COMPATIBILITY_TOFFEE)

    dependsOn(hideVPNPatch)
}