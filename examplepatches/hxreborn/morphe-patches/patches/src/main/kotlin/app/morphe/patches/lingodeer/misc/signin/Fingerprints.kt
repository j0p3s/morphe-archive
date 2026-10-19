/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.lingodeer.misc.signin

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

internal val promptBypassFlagRead =
    fieldAccess(type = "Z", opcode = Opcode.SGET_BOOLEAN, location = MatchAfterWithin(10))

internal object SignInPromptFingerprint : Fingerprint(
    filters = listOf(
        methodCall(name = "isUnloginUser", returnType = "Z"),
        promptBypassFlagRead,
        opcode(Opcode.IF_NEZ, location = MatchAfterImmediately()),
        newInstance("Landroid/content/Intent;", location = MatchAfterWithin(8)),
    ),
)
