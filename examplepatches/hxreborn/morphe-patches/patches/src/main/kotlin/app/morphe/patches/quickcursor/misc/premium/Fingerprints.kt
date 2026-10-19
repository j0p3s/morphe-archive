/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.quickcursor.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object ProStateFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "L",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Enum;", name = "name"),
        methodCall(name = "getString", parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;")),
        string("yes"),
        methodCall(name = "valueOf", parameters = listOf("Ljava/lang/String;")),
    ),
)

internal fun proStateDispatchFingerprint(proStateType: String) = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("L", proStateType),
    filters = listOf(
        opcode(Opcode.NEW_INSTANCE),
        methodCall(parameters = listOf("Ljava/lang/Runnable;"), returnType = "V"),
    ),
)
