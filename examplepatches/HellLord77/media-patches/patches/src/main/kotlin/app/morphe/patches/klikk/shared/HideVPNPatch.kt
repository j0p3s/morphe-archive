package app.morphe.patches.klikk.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.network.hideVPNPatch
import app.morphe.patches.klikk.shared.Constants.COMPATIBILITY_KLIKK

@Suppress("unused")
val hideVPNPatch = rawResourcePatch(
    name = hideVPNPatch.name,
    description = hideVPNPatch.description,
    default = true,
) {
    compatibleWith(COMPATIBILITY_KLIKK)

    dependsOn(hideVPNPatch)
}