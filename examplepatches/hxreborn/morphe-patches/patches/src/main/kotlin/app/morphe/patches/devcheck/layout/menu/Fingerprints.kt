/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.devcheck.layout.menu

import app.morphe.patcher.Fingerprint

internal object PrepareMainOptionsMenuFingerprint : Fingerprint(
    definingClass = "Lflar2/devcheck/MainActivity;",
    name = "onPrepareOptionsMenu",
    returnType = "Z",
    parameters = listOf("Landroid/view/Menu;"),
)
