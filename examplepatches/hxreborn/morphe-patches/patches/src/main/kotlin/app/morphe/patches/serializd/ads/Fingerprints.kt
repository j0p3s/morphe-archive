/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.serializd.ads

import app.morphe.patcher.Fingerprint

internal object TextResponseReceivedFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/react/modules/network/ResponseUtil;",
    name = "onDataReceived",
    returnType = "V",
    parameters = listOf("Lcom/facebook/react/bridge/ReactApplicationContext;", "I", "Ljava/lang/String;"),
)
