/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.periodcalendar.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

private const val PRODUCT_CLASS = "Lcom/digitalchemy/foundation/applicationmanagement/market/Product;"
private const val METHOD_CALL_CLASS = "Lio/flutter/plugin/common/MethodCall;"
private const val METHOD_RESULT_CLASS = "Lio/flutter/plugin/common/MethodChannel\$Result;"

internal object IsProFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(string("Pro")),
)

internal object IsAdFreeFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(string("Remove ads")),
)

internal object WorkbookPurchasedFingerprint : Fingerprint(
    name = "onMethodCall",
    returnType = "V",
    parameters = listOf(METHOD_CALL_CLASS, METHOD_RESULT_CLASS),
    filters = listOf(
        string("getIapConfiguration"),
        string("isWorkbookPurchased"),
        methodCall(
            parameters = listOf(PRODUCT_CLASS),
            returnType = "Z",
            opcode = Opcode.INVOKE_INTERFACE,
            location = MatchAfterWithin(12),
        ),
        opcode(Opcode.MOVE_RESULT, MatchAfterImmediately()),
    ),
)
