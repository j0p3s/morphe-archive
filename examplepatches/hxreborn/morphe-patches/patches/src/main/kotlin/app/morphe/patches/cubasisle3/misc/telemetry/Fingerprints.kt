/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.cubasisle3.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object BugsnagStartFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/bugsnag/android/Bugsnag;",
            name = "start",
            parameters = listOf("Landroid/content/Context;"),
        ),
    ),
)
