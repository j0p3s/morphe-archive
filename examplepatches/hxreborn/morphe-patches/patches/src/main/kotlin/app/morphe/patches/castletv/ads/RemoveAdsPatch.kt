/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.castletv.ads

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.castletv.shared.IsAdFreeMemberFingerprint
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.util.matchSingle
import app.morphe.util.returnEarly

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes the pre-roll and mid-roll ads from playback.",
) {
    compatibleWith(AppCompatibilities.CASTLE_FOR_TV)

    execute {
        IsAdFreeMemberFingerprint.matchSingle().method.returnEarly(true)
    }
}
