/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.raindrop.layout.settings

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.hermes.hermesVariantsPatch

private const val LINKS_FORM_4_7 =
    "6F 0E 10 00 0F 0E 5A 0D 0E 03 52 07 0D 00 6F 07 0C 00 0A 07 5A 04 07 05"
private const val LINKS_FORM_NULL_4_7 =
    "6F 0E 10 00 0F 0E 5A 0D 0E 03 52 07 0D 00 94 07 94 07 94 07 5A 04 07 05"
private const val LINKS_FORM_4_8 =
    "6F 11 13 01 12 11 5A 10 11 03 52 02 10 00 6F 02 0F 01 08 02 8B 00 11"
private const val LINKS_FORM_NULL_4_8 =
    "6F 11 13 01 12 11 5A 10 11 03 52 02 10 00 94 02 94 02 94 02 8B 00 11"

@Suppress("unused")
val hideSettingsLinksPatch = rawResourcePatch(
    name = "Hide Settings links",
    description = "Hides the Web and Desktop app, Blog, Help and Support links in Settings.",
) {
    compatibleWith(AppCompatibilities.RAINDROP)

    dependsOn(
        hermesVariantsPatch(
            setOf(LINKS_FORM_4_7 to LINKS_FORM_NULL_4_7),
            setOf(LINKS_FORM_4_8 to LINKS_FORM_NULL_4_8),
        ),
    )
}
