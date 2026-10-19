/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.moonreader.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

internal object DisableAdsFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(name = "subscribeAdFree", type = "Z"),
        methodCall(name = "inRewardedVideoRemoveAdTime", returnType = "Z"),
        fieldAccess(name = "runCount", type = "I"),
    ),
)

internal object CanShowAdsFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Landroid/content/Context;", name = "getPackageName"),
        string("r"),
        methodCall(definingClass = "Ljava/lang/String;", name = "endsWith"),
    ),
)
