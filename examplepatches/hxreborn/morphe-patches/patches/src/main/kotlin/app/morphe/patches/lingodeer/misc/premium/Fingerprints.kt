/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.lingodeer.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

internal object PremiumStateFingerprint : Fingerprint(
    name = "invokeSuspend",
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/System;", name = "currentTimeMillis"),
        methodCall(name = "getExpiredDateMs", returnType = "J", location = MatchAfterWithin(4)),
        opcode(Opcode.CMP_LONG, location = MatchAfterWithin(2)),
        methodCall(definingClass = "Landroid/text/TextUtils;", name = "isEmpty", location = MatchAfterWithin(12)),
    ),
)
