/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.sygic.misc.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal object CredentialManagerProviderFingerprint : Fingerprint(
    filters = listOf(string("Your device doesn't support credential manager")),
)

internal object CredentialManagerAvailabilityFingerprint : Fingerprint(
    classFingerprint = CredentialManagerProviderFingerprint,
    returnType = "Z",
    parameters = emptyList(),
)

internal object PlayServicesGetCredentialFingerprint : Fingerprint(
    definingClass = "Landroidx/credentials/playservices/CredentialProviderPlayServicesImpl;",
    name = "onGetCredential",
    returnType = "V",
    filters = listOf(
        methodCall(name = "isAvailableOnDevice", parameters = listOf("I"), returnType = "Z"),
        opcode(Opcode.MOVE_RESULT, location = MatchAfterImmediately()),
    ),
)

internal object GooglePlayServicesAvailabilityFingerprint : Fingerprint(
    definingClass = "Lcom/sygic/sdk/low/gps/GpsLocationManager\$Companion;",
    name = "isGoogleApiAvailable",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
)
