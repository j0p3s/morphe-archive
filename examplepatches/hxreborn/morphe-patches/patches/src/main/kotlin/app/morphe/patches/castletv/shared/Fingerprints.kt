/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.castletv.shared

import app.morphe.patcher.Fingerprint

internal object IsAdFreeMemberFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    strings = listOf("vip===isMember==isPermission==="),
)
