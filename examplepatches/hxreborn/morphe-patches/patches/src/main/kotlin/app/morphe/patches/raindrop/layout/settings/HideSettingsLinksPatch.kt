/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.raindrop.layout.settings

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.misc.hermes.hermesPatch
import app.morphe.patches.shared.compat.AppCompatibilities

private const val LINKS_FORM =
    "6F 0E 10 00 0F 0E 5A 0D 0E 03 52 07 0D 00 6F 07 0C 00 0A 07 5A 04 07 05"
private const val LINKS_FORM_NULL =
    "6F 0E 10 00 0F 0E 5A 0D 0E 03 52 07 0D 00 94 07 94 07 94 07 5A 04 07 05"

@Suppress("unused")
val hideSettingsLinksPatch = rawResourcePatch(
    name = "Hide Settings links",
    description = "Hides the Web and Desktop app, Blog, Help and Support links in Settings.",
) {
    compatibleWith(AppCompatibilities.RAINDROP)

    dependsOn(
        hermesPatch {
            setOf(LINKS_FORM to LINKS_FORM_NULL)
        },
    )
}
