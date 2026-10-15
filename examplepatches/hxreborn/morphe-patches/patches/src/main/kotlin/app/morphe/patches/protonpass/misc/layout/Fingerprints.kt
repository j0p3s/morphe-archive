/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.protonpass.misc.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral

internal object HelpCenterSectionFingerprint : Fingerprint(
    name = "HelpCenterProfileSection",
    returnType = "V",
    filters = listOf(resourceLiteral(ResourceType.STRING, "profile_help_center")),
)

internal object SpreadTheWordSectionFingerprint : Fingerprint(
    name = "SpreadTheWordSection",
    returnType = "V",
    parameters = listOf(
        "Landroidx/compose/ui/Modifier;",
        "Lkotlin/jvm/functions/Function0;",
        "Lkotlin/jvm/functions/Function0;",
        "Landroidx/compose/runtime/Composer;",
        "I",
    ),
)

internal object AboutSectionFingerprint : Fingerprint(
    name = "AboutSection",
    returnType = "V",
    filters = listOf(resourceLiteral(ResourceType.STRING, "settings_about_section_title")),
)
