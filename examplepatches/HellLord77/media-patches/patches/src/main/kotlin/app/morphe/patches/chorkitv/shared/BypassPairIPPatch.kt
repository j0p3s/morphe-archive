package app.morphe.patches.chorkitv.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.pairip.bypass.bypassLicenseCheckPatch
import app.morphe.patches.chorkitv.shared.Constants.COMPATIBILITY_CHORKITV

@Suppress("unused")
val bypassPairIPPatch = rawResourcePatch(
    name = "Bypass PairIP",
    description = "Bypasses PairIP checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CHORKITV)

    dependsOn(bypassLicenseCheckPatch)
}