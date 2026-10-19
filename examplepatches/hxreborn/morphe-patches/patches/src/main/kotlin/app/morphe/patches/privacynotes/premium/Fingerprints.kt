/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.privacynotes.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object PageStartedFingerprint : Fingerprint(
    name = "onPageStarted",
    returnType = "V",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;", "Landroid/graphics/Bitmap;"),
    custom = { _, classDef -> classDef.superclass == "Landroid/webkit/WebViewClient;" },
    filters = listOf(methodCall(name = "onPageLoading")),
)
