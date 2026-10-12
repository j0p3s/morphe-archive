package app.morphe.patches.all.pairip.strip

import app.morphe.patcher.patch.rawResourcePatch

@Suppress("unused")
fun stripPairIP() = rawResourcePatch(
    name = "Strip PairIP checks",
    description = "Strips Play Integrity API (pairip) client-side checks.",
    default = false,
) {
    execute {
        throw NotImplementedError()
    }
}