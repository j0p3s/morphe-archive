/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.publicvideos.tracking

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method

private val extendsAppsFlyerLib: (Method, ClassDef) -> Boolean =
    { _, classDef -> classDef.superclass == "Lcom/appsflyer/AppsFlyerLib;" }

internal object AppsFlyerInitFingerprint : Fingerprint(
    name = "init",
    custom = extendsAppsFlyerLib,
)

internal object AppsFlyerStartFingerprint : Fingerprint(
    name = "start",
    returnType = "V",
    custom = extendsAppsFlyerLib,
)

internal object FlurrySupportedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    strings = listOf("Device SDK Version older than %d"),
)

internal object FlurryContentProviderCreateFingerprint : Fingerprint(
    definingClass = "Lcom/flurry/android/agent/FlurryContentProvider;",
    name = "onCreate",
    returnType = "Z",
)

internal object ComScoreStartFingerprint : Fingerprint(
    definingClass = "Lcom/comscore/Analytics;",
    name = "start",
    returnType = "V",
)
