/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.aloa.misc.premium

import app.morphe.patcher.Fingerprint

internal object SetSubscriptionStatusFingerprint : Fingerprint(
    definingClass = "Lcom/superwall/sdk/store/Entitlements;",
    name = "setSubscriptionStatus",
    returnType = "V",
    parameters = listOf("Lcom/superwall/sdk/models/entitlements/SubscriptionStatus;"),
)
