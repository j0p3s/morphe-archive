/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.musicolet.misc.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.resource.ResourceType
import app.morphe.patcher.resourceLiteral
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal object ProAccessLabelFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(type = "Z", opcode = Opcode.SGET_BOOLEAN),
        resourceLiteral(ResourceType.STRING, "lifetime_access", location = MatchAfterWithin(8)),
        fieldAccess(type = "Z", opcode = Opcode.SGET_BOOLEAN),
        resourceLiteral(ResourceType.STRING, "subscription", location = MatchAfterWithin(8)),
    ),
)

internal fun proAccessFingerprint(lifetime: FieldReference, subscription: FieldReference) = Fingerprint(
    definingClass = lifetime.definingClass,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(lifetime, Opcode.SGET_BOOLEAN),
        fieldAccess(subscription, Opcode.SGET_BOOLEAN),
    ),
)

internal fun proPurchaseStateFingerprint(lifetime: FieldReference) = Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(lifetime, Opcode.SGET_BOOLEAN),
        resourceLiteral(ResourceType.STRING, "thanks_msg_pro_sub"),
        resourceLiteral(ResourceType.STRING, "thanks_msg_pro"),
        resourceLiteral(ResourceType.STRING, "lifetime_access"),
    ),
)
