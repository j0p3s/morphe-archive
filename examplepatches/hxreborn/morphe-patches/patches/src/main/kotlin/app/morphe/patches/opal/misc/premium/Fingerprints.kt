/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.opal.misc.premium

import app.morphe.patcher.Fingerprint

internal object BlockScreenCategoryToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    strings = listOf("BlockScreenCategoryUiModel(id="),
)

internal object BlockScreenCategoryConstructorFingerprint : Fingerprint(
    classFingerprint = BlockScreenCategoryToStringFingerprint,
    name = "<init>",
)
