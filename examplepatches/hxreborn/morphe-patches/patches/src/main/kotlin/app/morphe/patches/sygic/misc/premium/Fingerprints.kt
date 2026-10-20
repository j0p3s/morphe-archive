/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.sygic.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private const val LICENSE_CLASS = "Lcom/sygic/navi/licensing/LicenseManager\$License;"
internal const val PREMIUM_LICENSE_CLASS = "Lcom/sygic/navi/licensing/LicenseManager\$License\$Premium;"

internal object LicenseParserFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("com.sygic.aura_android"),
        methodCall(
            name = "<init>",
            parameters = listOf(LICENSE_CLASS, "Ljava/util/LinkedHashMap;"),
            returnType = "V",
        ),
    ),
)

internal object FeatureToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    filters = listOf(string("Feature(type=")),
)

internal object FeatureIsValidFingerprint : Fingerprint(
    classFingerprint = FeatureToStringFingerprint,
    returnType = "Z",
    parameters = emptyList(),
)

internal object PremiumLicenseConstructorFingerprint : Fingerprint(
    definingClass = PREMIUM_LICENSE_CLASS,
    name = "<init>",
    parameters = listOf("Z", "Ljava/lang/Long;"),
)
