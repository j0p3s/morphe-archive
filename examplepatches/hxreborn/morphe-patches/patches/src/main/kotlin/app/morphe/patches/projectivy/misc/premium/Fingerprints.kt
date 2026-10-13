/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Additional terms under GPLv3 section 7:
 * - You must preserve reasonable legal notices and author attributions in this file.
 * - Modified versions must not misrepresent the origin of this file.
 *
 * Ported from hxreborn/revanced-patches:
 * https://gitlab.com/hxreborn/revanced-patches/-/commit/ea27afb265862656079dea62697b42032a4704a3
 * Commit ea27afb265862656079dea62697b42032a4704a3 (2026-05-17),
 * patches/src/main/kotlin/app/revanced/patches/projectivy/misc/premium/Fingerprints.kt
 */
package app.morphe.patches.projectivy.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object PtApplicationMarkerFingerprint : Fingerprint(
    strings = listOf("ptutils"),
)

internal object PremiumCheckFingerprint : Fingerprint(
    classFingerprint = PtApplicationMarkerFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "this",
            parameters = listOf("Z"),
            returnType = "Z",
            opcode = Opcode.INVOKE_STATIC,
        ),
    ),
)

internal object TrialStateSetterFingerprint : Fingerprint(
    classFingerprint = PtApplicationMarkerFingerprint,
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis",
        ),
    ),
)

internal const val ACCESSIBILITY_SERVICE_CLASS =
    "Lcom/spocky/projengmenu/services/ProjectivyAccessibilityService;"

internal object AccessibilityServiceByteInitFingerprint : Fingerprint(
    definingClass = ACCESSIBILITY_SERVICE_CLASS,
    name = "<clinit>",
    filters = listOf(fieldAccess(type = "B", opcode = Opcode.SPUT_BYTE)),
)

