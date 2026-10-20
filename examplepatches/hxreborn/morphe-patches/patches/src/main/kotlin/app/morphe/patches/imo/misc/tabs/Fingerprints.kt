/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.misc.tabs

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

internal object LegacyTabsConfigFingerprint : Fingerprint(
    filters = listOf(string("legacyTabsConfig")),
)

internal object HomeTabIdsFingerprint : Fingerprint(
    classFingerprint = LegacyTabsConfigFingerprint,
    returnType = "Ljava/util/List;",
)
