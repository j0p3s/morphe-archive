/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.depthpix.ads

import app.morphe.patcher.Fingerprint

internal object AppAdConfigToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf("AppAdConfig(adsEnabled="),
)

internal object AppAdConfigConstructorFingerprint : Fingerprint(
    classFingerprint = AppAdConfigToStringFingerprint,
    name = "<init>",
    custom = { method, _ -> method.parameterTypes.firstOrNull() == "Z" },
)
