/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.raindrop.layout.ai

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.hermes.hermesVariantsPatch

private const val HOME_HEADER_BUTTON_4_7 =
    "02 04 C1 07 00 00 D1 4A 01 00 84 0A 08 B7 42 52 04 0A 01 93 00 6F 07 09 00 07 04"
private const val HOME_HEADER_BUTTON_NULL_4_7 =
    "02 04 C1 07 00 00 D1 4A 01 00 84 0A 08 B7 42 52 04 0A 01 93 00 94 07 94 07 94 07"
private const val COLLECTION_HEADER_BUTTON_4_7 =
    "02 05 C1 07 00 00 D1 4A 01 00 84 0A 09 24 43 52 05 0A 01 93 01 6F 06 08 01 06 05"
private const val COLLECTION_HEADER_BUTTON_NULL_4_7 =
    "02 05 C1 07 00 00 D1 4A 01 00 84 0A 09 24 43 52 05 0A 01 93 01 94 06 94 06 94 06"
private const val READER_TOOLBAR_BUTTON_4_7 =
    "02 08 C1 07 00 00 D1 4A 01 00 52 08 0D 01 6F 08 0B 00 0A 08"
private const val READER_TOOLBAR_BUTTON_NULL_4_7 =
    "02 08 C1 07 00 00 D1 4A 01 00 52 08 0D 01 94 08 94 08 94 08"
private const val HOME_HEADER_BUTTON_4_8 =
    "02 04 02 06 00 00 48 49 01 00 84 0A 08 90 42 52 04 0A 01 93 00 6F 07 09 00 07 04"
private const val HOME_HEADER_BUTTON_NULL_4_8 =
    "02 04 02 06 00 00 48 49 01 00 84 0A 08 90 42 52 04 0A 01 93 00 94 07 94 07 94 07"
private const val COLLECTION_HEADER_BUTTON_4_8 =
    "02 05 02 06 00 00 48 49 01 00 84 0A 09 E2 42 52 05 0A 01 93 01 6F 06 08 01 06 05"
private const val COLLECTION_HEADER_BUTTON_NULL_4_8 =
    "02 05 02 06 00 00 48 49 01 00 84 0A 09 E2 42 52 05 0A 01 93 01 94 06 94 06 94 06"
private const val READER_TOOLBAR_BUTTON_4_8 =
    "02 03 02 06 00 00 48 49 01 00 52 03 08 01 6F 03 11 01 10 03"
private const val READER_TOOLBAR_BUTTON_NULL_4_8 =
    "02 03 02 06 00 00 48 49 01 00 52 03 08 01 94 03 94 03 94 03"

@Suppress("unused")
val hideAiAssistantPatch = rawResourcePatch(
    name = "Hide AI assistant",
    description = "Hides the AI assistant button on the home screen, in collections and in the reader.",
) {
    compatibleWith(AppCompatibilities.RAINDROP)

    dependsOn(
        hermesVariantsPatch(
            setOf(
                HOME_HEADER_BUTTON_4_7 to HOME_HEADER_BUTTON_NULL_4_7,
                COLLECTION_HEADER_BUTTON_4_7 to COLLECTION_HEADER_BUTTON_NULL_4_7,
                READER_TOOLBAR_BUTTON_4_7 to READER_TOOLBAR_BUTTON_NULL_4_7,
            ),
            setOf(
                HOME_HEADER_BUTTON_4_8 to HOME_HEADER_BUTTON_NULL_4_8,
                COLLECTION_HEADER_BUTTON_4_8 to COLLECTION_HEADER_BUTTON_NULL_4_8,
                READER_TOOLBAR_BUTTON_4_8 to READER_TOOLBAR_BUTTON_NULL_4_8,
            ),
        ),
    )
}
