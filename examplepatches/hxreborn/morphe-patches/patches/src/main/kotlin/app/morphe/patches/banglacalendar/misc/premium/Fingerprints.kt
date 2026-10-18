/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.banglacalendar.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

private const val BOOLEAN = "Ljava/lang/Boolean;"
private const val MUTABLE_STATE_FLOW = "Lkotlinx/coroutines/flow/MutableStateFlow;"
private const val STATE_FLOW_FACTORY = "Lkotlinx/coroutines/flow/StateFlowKt;"

internal object PremiumCodeFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    name = "getPremiumCode",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)

internal object BillingStateConstructorFingerprint : Fingerprint(
    name = "<init>",
    parameters = listOf("Landroid/app/Application;"),
    strings = listOf("Please provide a valid listener for purchases updates."),
    filters = listOf(
        methodCall(definingClass = STATE_FLOW_FACTORY, name = "MutableStateFlow"),
    ),
)

internal object PurchasesQueriedFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("L", "Ljava/util/List;"),
    strings = listOf("purchaseState"),
    filters = listOf(
        methodCall(definingClass = BOOLEAN, name = "valueOf"),
        methodCall(definingClass = MUTABLE_STATE_FLOW, name = "setValue", location = MatchAfterWithin(3)),
    ),
)
