/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.calcnote.misc.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.calcnote.misc.fix.signature.spoofSignaturePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.revenuecat.BuildCustomerInfoFingerprint
import app.morphe.patches.shared.misc.revenuecat.customerInfoJsonParameter
import app.morphe.util.getFreeRegisterProvider
import app.morphe.util.matchSingle

private const val EXTENSION_CLASS = "Lapp/hxreborn/extension/shared/RevenueCatUnlock;"
private const val ENTITLEMENT = "pro"
private const val PRODUCT = "calcnote_pro_plan"

@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock pro",
    description = "Unlocks Pro features and removes ads.",
) {
    compatibleWith(AppCompatibilities.CALCNOTE)
    extendWith("extensions/extension.mpe")

    dependsOn(spoofSignaturePatch)

    execute {
        BuildCustomerInfoFingerprint.matchSingle().method.apply {
            val registers = getFreeRegisterProvider(0, 3)
            val jsonRegister = registers.getFreeRegister4Bit()
            val entitlementRegister = registers.getFreeRegister4Bit()
            val productRegister = registers.getFreeRegister4Bit()

            addInstructions(
                0,
                """
                    move-object/from16 v$jsonRegister, ${customerInfoJsonParameter()}
                    const-string v$entitlementRegister, "$ENTITLEMENT"
                    const-string v$productRegister, "$PRODUCT"
                    invoke-static { v$jsonRegister, v$entitlementRegister, v$productRegister }, $EXTENSION_CLASS->grantEntitlement(Lorg/json/JSONObject;Ljava/lang/String;Ljava/lang/String;)V
                """,
            )
        }
    }
}
