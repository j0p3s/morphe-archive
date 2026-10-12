package app.morphe.patches.kabbik.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.pairip.bypass.bypassLicenseCheckPatch
import app.morphe.patches.kabbik.shared.Constants.COMPATIBILITY_KABBIK

@Suppress("unused")
val bypassPairIPPatch = rawResourcePatch(
    name = "Bypass PairIP",
    description = "Bypasses PairIP checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_KABBIK)

    dependsOn(bypassLicenseCheckPatch)
}