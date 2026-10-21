/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.raindrop.theme

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal const val JS_BUNDLE_LOADER_COMPANION = "Lcom/facebook/react/bridge/JSBundleLoader\$Companion;"

internal object CreateBundleLoaderFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        string("assets://"),
        string("index.android.bundle"),
        methodCall(
            definingClass = JS_BUNDLE_LOADER_COMPANION,
            name = "createAssetLoader",
        ),
    ),
)
