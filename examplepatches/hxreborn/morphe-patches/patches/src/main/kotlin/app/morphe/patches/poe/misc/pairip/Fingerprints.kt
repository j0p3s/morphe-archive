/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.poe.misc.pairip

import app.morphe.patcher.Fingerprint

internal object DownloadCompleteReceiverFingerprint : Fingerprint(
    name = "onReceive",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
    custom = { _, classDef ->
        classDef.superclass == "Landroid/content/BroadcastReceiver;" &&
            classDef.methods.any { it.name.startsWith("getLongExtra$") } &&
            classDef.methods.any { it.name.startsWith("longValue$") }
    },
)
