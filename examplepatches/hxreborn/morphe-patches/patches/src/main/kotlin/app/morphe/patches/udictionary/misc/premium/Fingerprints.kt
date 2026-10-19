/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.udictionary.misc.premium

import app.morphe.patcher.Fingerprint

internal object DebugVipFlagFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("vip_for_debug"),
)
