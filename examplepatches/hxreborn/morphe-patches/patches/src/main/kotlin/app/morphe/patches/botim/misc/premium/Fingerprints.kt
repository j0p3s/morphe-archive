/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.botim.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

internal object SelfProfileMapperFingerprint : Fingerprint(
    parameters = listOf("Lim/thebot/bind/proto/profile/Profile\$UserProfileInfoPb;"),
    filters = listOf(
        methodCall(
            definingClass = "Lim/thebot/bind/proto/profile/Profile\$UserProfileInfoPb;",
            name = "getIsVip",
            returnType = "Z",
        ),
    ),
)
