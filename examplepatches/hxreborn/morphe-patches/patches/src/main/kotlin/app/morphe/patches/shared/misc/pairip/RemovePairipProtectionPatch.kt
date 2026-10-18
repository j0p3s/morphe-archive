/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.shared.misc.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.util.matchSingle
import app.morphe.util.returnEarly

val removePairipProtectionPatch = bytecodePatch {
    compatibleWith(
        AppCompatibilities.ALOA,
        AppCompatibilities.ANYTRACKER,
        AppCompatibilities.AUDIOLAB,
        AppCompatibilities.BANGLA_CALENDAR,
        AppCompatibilities.BIOBIRD,
        AppCompatibilities.BLURWALL,
        AppCompatibilities.CATZY,
        AppCompatibilities.DEPTHPIX,
        AppCompatibilities.ECHO_EQUALIZER,
        AppCompatibilities.ECHOGRAM,
        AppCompatibilities.HINDU_CALENDAR,
        AppCompatibilities.JUJO,
        AppCompatibilities.KEEPA,
        AppCompatibilities.LAGGUY_WALLPAPERS,
        AppCompatibilities.LED_BLINKER,
        AppCompatibilities.LUMO,
        AppCompatibilities.MEDICNEET,
        AppCompatibilities.MYMOVESET,
        AppCompatibilities.PRAYER_TIMES,
        AppCompatibilities.QURANIFY,
        AppCompatibilities.RATEGLANCE,
        AppCompatibilities.RUBBER_BANDS,
        AppCompatibilities.VLLO,
        AppCompatibilities.WALP,
    )

    execute {
        if (classDefByOrNull(LICENSE_CLIENT_CLASS) == null) return@execute

        InitializeLicenseCheckFingerprint.matchSingle().method.returnEarly()
    }
}
