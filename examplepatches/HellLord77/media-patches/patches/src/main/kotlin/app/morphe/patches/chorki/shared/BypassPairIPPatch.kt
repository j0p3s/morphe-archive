package app.morphe.patches.chorki.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.pairip.bypass.bypassLicenseCheckPatch
import app.morphe.patches.chorki.shared.Constants.COMPATIBILITY_CHORKI

@Suppress("unused")
val bypassPairIPPatch = rawResourcePatch(
    name = "Bypass PairIP",
    description = "Bypasses PairIP checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CHORKI)

    dependsOn(bypassLicenseCheckPatch)
}