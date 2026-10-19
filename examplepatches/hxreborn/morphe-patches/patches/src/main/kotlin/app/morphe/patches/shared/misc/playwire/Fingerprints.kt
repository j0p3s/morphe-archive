/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.shared.misc.playwire

import app.morphe.patcher.Fingerprint

private object PlaywireModuleFingerprint : Fingerprint(
    name = "getName",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf("PlaywireSDKProvider"),
)

internal object StartPlaywireSdkFingerprint : Fingerprint(
    classFingerprint = PlaywireModuleFingerprint,
    name = "startSDK",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Lcom/facebook/react/bridge/Callback;"),
)
