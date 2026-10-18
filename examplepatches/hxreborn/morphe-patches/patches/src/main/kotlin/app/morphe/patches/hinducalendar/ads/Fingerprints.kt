/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.hinducalendar.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal object OnQueryPurchasesResponseFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("In onQueryPurchasesResponse"),
        methodCall(name = "isEmpty", returnType = "Z"),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately()),
        string("Purchase Found. Not showing ads"),
    ),
)
