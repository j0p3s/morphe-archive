/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.protonmail.misc.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral

internal object SubscriptionEntryFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(resourceLiteral(ResourceType.STRING, "presentation_menu_item_title_subscription")),
)

internal object ReportAProblemEntryFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(resourceLiteral(ResourceType.STRING, "drawer_title_bug_report")),
)
