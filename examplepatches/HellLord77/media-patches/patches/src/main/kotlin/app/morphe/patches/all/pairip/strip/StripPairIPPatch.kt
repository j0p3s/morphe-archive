package app.morphe.patches.all.pairip.strip

import app.morphe.patcher.patch.bytecodePatch

val stripPairIPPatch = bytecodePatch {
    dependsOn(restoreDexPatch, restoreFieldPatch, stripLibrariesPatch)
}