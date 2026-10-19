/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.musicolet.misc.fix.signature

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

internal object CertificateDigestFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("[B"),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Class;", name = "forName"),
        methodCall(definingClass = "Ljava/lang/Class;", name = "getMethod"),
        methodCall(definingClass = "Ljava/lang/reflect/Method;", name = "invoke"),
    ),
)

internal object CertificateChecksumFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("[B"),
    filters = listOf(
        literal(7),
        methodCall(definingClass = "Ljava/lang/Integer;", name = "valueOf"),
        methodCall(definingClass = "Ljava/util/Set;", name = "add"),
    ),
)
