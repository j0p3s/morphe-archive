package app.morphe.patches.shadhin.shared.data.remote.api.model.mainContentModel

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.matchSingle
import app.morphe.util.returnEarly

val isPaidPatch = bytecodePatch {
    execute {
        IsPaidFingerprint.matchSingle().method.returnEarly(false)
    }
}