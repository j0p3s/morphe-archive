/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object AddAccountCallFingerprint : Fingerprint(
    filters = listOf(
        methodCall(definingClass = "Landroid/accounts/AccountManager;", name = "addAccount"),
    ),
)
