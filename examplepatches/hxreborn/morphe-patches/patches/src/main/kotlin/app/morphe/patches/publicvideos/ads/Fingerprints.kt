/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.publicvideos.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object InitializeMobileAdsFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    strings = listOf("ca-app-pub-4482405035205548~9657278803"),
)

internal object RegisterAmazonAdsFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        methodCall(definingClass = "Lcom/amazon/device/ads/AdRegistration;", name = "getInstance"),
        methodCall(definingClass = "Lcom/amazon/device/ads/AdRegistration;", name = "setAdNetworkInfo"),
    ),
)

internal object InitializeAudienceNetworkFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(definingClass = "Lcom/facebook/ads/AudienceNetworkAds;", name = "initialize"),
    ),
)
