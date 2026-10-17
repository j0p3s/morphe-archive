/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.protonvpn.misc.netshield

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.anyInstruction
import app.morphe.patcher.checkCast
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patches.protonvpn.misc.restrictions.freeUserCheckFingerprint
import com.android.tools.smali.dexlib2.Opcode

private const val NETSHIELD_AVAILABILITY_READ_DISTANCE = 3
private const val NETSHIELD_RESET_READ_DISTANCE = 8
private const val LOCAL_USER_SETTINGS = "Lcom/protonvpn/android/settings/data/LocalUserSettings;"

internal object NetShieldAvailabilityEnumFingerprint : Fingerprint(
    name = "<clinit>",
    strings = listOf("AVAILABLE", "UPGRADE_VPN_PLUS"),
)

internal fun BytecodePatchContext.netShieldAvailabilityFingerprint(): Fingerprint {
    val netShieldAvailability = NetShieldAvailabilityEnumFingerprint.originalClassDef.type
    return freeUserCheckFingerprint(
        followingFilters = arrayOf(
            fieldAccess(
                opcode = Opcode.SGET_OBJECT,
                definingClass = netShieldAvailability,
                type = netShieldAvailability,
                location = MatchAfterWithin(NETSHIELD_AVAILABILITY_READ_DISTANCE),
            ),
        ),
    )
}

internal fun BytecodePatchContext.netShieldResetFingerprint(): Fingerprint {
    val netShieldGetter = classDefBy(LOCAL_USER_SETTINGS).methods.single { it.name == "getNetShield" }
    return freeUserCheckFingerprint(
        strings = listOf("reset default profile: "),
        followingFilters = arrayOf(
            anyInstruction(
                checkCast(netShieldGetter.returnType),
                methodCall(netShieldGetter),
                location = MatchAfterWithin(NETSHIELD_RESET_READ_DISTANCE),
            ),
        ),
    )
}
