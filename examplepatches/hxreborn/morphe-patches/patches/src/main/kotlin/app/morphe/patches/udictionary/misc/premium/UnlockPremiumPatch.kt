/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.udictionary.misc.premium

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.udictionary.misc.fix.signature.spoofSignaturePatch
import app.morphe.util.matchSingle
import app.morphe.util.returnEarly

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks Pro features and removes ads.",
) {
    compatibleWith(AppCompatibilities.U_DICTIONARY)

    dependsOn(spoofSignaturePatch)

    execute {
        DebugVipFlagFingerprint.matchSingle().method.returnEarly(true)
    }
}
