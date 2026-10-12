package app.morphe.patches.hoichoi.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.pairip.strip.stripPairIPPatch
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOI
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOITV

@Suppress("unused")
val stripPairIPPatch = rawResourcePatch(
    name = "Strip PairIP",
    description = "Strips PairIP checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOICHOI, COMPATIBILITY_HOICHOITV)

    dependsOn(stripPairIPPatch)
}