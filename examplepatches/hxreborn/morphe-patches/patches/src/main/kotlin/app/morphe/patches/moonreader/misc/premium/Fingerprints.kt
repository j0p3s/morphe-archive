/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.moonreader.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val PRO_PACKAGE = "com.flyersoft.moonreaderp"

internal object IsProFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(name = "isProVersion", type = "Z"),
        fieldAccess(name = "subscribeAdFree", type = "Z"),
    ),
)

internal object PdfLicenceInitFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Ljava/util/ArrayList;"),
    filters = listOf(
        methodCall(name = "getPackageName", returnType = "Ljava/lang/String;"),
        string(PRO_PACKAGE),
    ),
)

internal object FreeTextButtonFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Z"),
    filters = listOf(
        fieldAccess(name = "antFreeText", type = "Landroid/widget/ImageView;"),
        methodCall(name = "getPackageName", returnType = "Ljava/lang/String;"),
        string("r"),
        methodCall(
            definingClass = "Ljava/lang/String;",
            name = "endsWith",
            location = MatchAfterImmediately(),
        ),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately()),
    ),
)
