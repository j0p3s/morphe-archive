/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.misc.story

import app.morphe.patcher.Fingerprint

private const val SETTINGS_DELEGATE_SUFFIX = "SettingsDelegate;"

internal object StoryGalleryDurationFingerprint : Fingerprint(
    definingClass = SETTINGS_DELEGATE_SUFFIX,
    name = "getStoryGalleryDurationLimit",
)

internal object StoryRecordDurationFingerprint : Fingerprint(
    definingClass = SETTINGS_DELEGATE_SUFFIX,
    name = "storyRecordDuration",
)
