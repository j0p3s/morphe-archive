/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.radiojavan.misc.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object SetCtaOverlayFingerprint : Fingerprint(
    name = "setCtas",
    returnType = "V",
    parameters = listOf("L"),
    custom = { method, _ -> method.implementation != null },
)

internal object ConsentRequestFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/ump/ConsentInformation;",
            name = "requestConsentInfoUpdate",
        ),
    ),
)

internal object MobileAdsInitializeFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAds;",
    name = "initialize",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/google/android/gms/ads/initialization/OnInitializationCompleteListener;",
    ),
)
