/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.shared

import app.morphe.patcher.Fingerprint

private const val SETTINGS_DELEGATE_SUFFIX = "SettingsDelegate;"

internal abstract class RemoteSettingFingerprint(getterName: String) :
    Fingerprint(definingClass = SETTINGS_DELEGATE_SUFFIX, name = getterName)
