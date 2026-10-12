/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.myapk.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object HasActivePurchaseFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("has_active_purchase"),
)

internal object PreCacheAdFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("J"),
    strings = listOf(" preCacheAd: delay="),
)
