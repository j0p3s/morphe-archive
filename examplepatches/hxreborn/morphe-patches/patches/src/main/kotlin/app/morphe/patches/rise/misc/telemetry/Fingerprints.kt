/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.rise.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SentryInitFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "L", "L"),
    strings = listOf("Fatal error during SentryAndroid.init(...)"),
)

internal object UsageEventMapFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        opcode(Opcode.CHECK_CAST),
        methodCall(
            definingClass = "this",
            location = MatchAfterImmediately(),
        ),
    ),
    strings = listOf("userActivities"),
)

internal object UsageEventToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf("ApplicationUsageEvent [usageDate="),
)

internal fun inlinedUsageEventMapFingerprint(eventType: String) = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/util/ArrayList;",
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(newInstance(eventType)),
)
