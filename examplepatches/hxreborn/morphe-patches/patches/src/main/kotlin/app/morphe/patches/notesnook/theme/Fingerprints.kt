/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.notesnook.theme

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object MmkvInstallFingerprint : Fingerprint(
    name = "install",
    returnType = "Z",
    filters = listOf(
        methodCall(
            name = "nativeInstall",
            parameters = listOf("J", "Ljava/lang/String;"),
            returnType = "V",
        ),
    ),
)
