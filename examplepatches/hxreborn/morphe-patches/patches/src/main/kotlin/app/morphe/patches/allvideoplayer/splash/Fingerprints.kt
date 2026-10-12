/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.allvideoplayer.splash

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patches.allvideoplayer.offline.StartNextActivityFingerprint

internal object SplashDelayFingerprint : Fingerprint(
    classFingerprint = StartNextActivityFingerprint,
    name = "<init>",
    filters = listOf(literal(10000)),
)
