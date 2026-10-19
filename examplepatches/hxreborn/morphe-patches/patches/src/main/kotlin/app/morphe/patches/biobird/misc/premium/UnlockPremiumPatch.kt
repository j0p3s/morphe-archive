/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.biobird.misc.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.pairip.removePairipProtectionPatch
import app.morphe.patches.shared.misc.requireArm64
import app.morphe.patches.shared.replaceMasked

private const val DART_AOT_LIBRARY = "libapp.so"
private const val ARM64 = "arm64-v8a"

internal val ACTIVE_SUBSCRIPTION_PROVIDER_PROLOGUE =
    ("fd79bfa9fd030faaef6100d1500340f9ff0110eb09000054400340f900cc4cf9700340f9" +
        "1f00106b0100005462034091427041f90000009470034091107641f9be0b40f9fec100a9" +
        "e00100f9640340f90000009401f05ff8217c4cd3f00300aae00301aae10310aa1edc3fd1" +
        "be7a7ef8c0033fd61f00166b").hexToByteArray()

internal val CURRENT_ENTITLEMENT_PROVIDER_PROLOGUE =
    ("fd79bfa9fd030faaef6100d1500340f9ff0110eb09000054400340f900cc4cf9700340f9" +
        "1f00106b0100005462034091427041f90000009470034091107641f9be0b40f9fec100a9" +
        "e00100f9640340f90000009401f05ff8217c4cd3f00300aae00301aae10310aa1edc3fd1" +
        "be7a7ef8c0033fd6ef031daa").hexToByteArray()

internal val PROVIDER_PROLOGUE_MASK =
    ("ffffffffffffffffffffffffff03c0ffffffffff1f0000ffff03c0ffffffffffff03c0ff" +
        "ffffffff1f0000ffff03c0ffffffffff000000fcff03c0ffffffffffffffffffffffffff" +
        "ffffffffff03c0ff000000fcffffffffffffffffffffffffffffffffffffffffffffffff" +
        "ffffffffffffffffffffffff").hexToByteArray()

internal val ENTITLEMENT_FETCH_SEQUENCE =
    ("e10303aa6203409142a441f900000094a00358f801f041b821801c8ba28358f800000094" +
        "e10300aaa18317f800000094").hexToByteArray()

internal val ENTITLEMENT_FETCH_SEQUENCE_MASK =
    ("ffffffffff03c0ffffffffff000000fcffffffffffffffffffffffffffffffff000000fc" +
        "ffffffffffffffff000000fc").hexToByteArray()

internal const val FETCH_USER_ENTITLEMENT_CALL_OFFSET = 28

internal val RETURN_TRUE = "c0820091c0035fd6".hexToByteArray()

internal val LOAD_PCB_ENTITLEMENT = "606b409100f444f9".hexToByteArray()

internal val RETURN_PCB_ENTITLEMENT = LOAD_PCB_ENTITLEMENT + "c0035fd6".hexToByteArray()

@Suppress("unused")
val unlockPremiumPatch = resourcePatch(
    name = "Unlock premium",
    description = "Unlocks all chapters and previous-year papers. " +
        "Signing in requires GmsCore support.",
) {
    compatibleWith(AppCompatibilities.BIOBIRD)
    availability(requireArm64)
    dependsOn(removePairipProtectionPatch)

    execute {
        val library = get("lib/$ARM64/$DART_AOT_LIBRARY")
        if (!library.exists()) throw PatchException("Could not find $DART_AOT_LIBRARY for $ARM64")

        mapOf(
            "active subscription provider" to (ACTIVE_SUBSCRIPTION_PROVIDER_PROLOGUE to RETURN_TRUE),
            "current entitlement provider" to (CURRENT_ENTITLEMENT_PROVIDER_PROLOGUE to RETURN_PCB_ENTITLEMENT),
        ).forEach { (provider, edit) ->
            val (pattern, replacement) = edit
            if (!library.replaceMasked(pattern, PROVIDER_PROLOGUE_MASK, mapOf(0 to replacement))) {
                throw PatchException("Could not find the $provider in $DART_AOT_LIBRARY")
            }
        }

        val callReplacement = mapOf(FETCH_USER_ENTITLEMENT_CALL_OFFSET to LOAD_PCB_ENTITLEMENT)
        if (!library.replaceMasked(ENTITLEMENT_FETCH_SEQUENCE, ENTITLEMENT_FETCH_SEQUENCE_MASK, callReplacement)) {
            throw PatchException("Could not find the fetchUserEntitlement call in $DART_AOT_LIBRARY")
        }
    }
}
