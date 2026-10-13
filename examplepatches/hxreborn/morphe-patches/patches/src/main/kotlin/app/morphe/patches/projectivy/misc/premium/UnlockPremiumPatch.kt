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
 * patches/src/main/kotlin/app/revanced/patches/projectivy/misc/premium/UnlockPremiumPatch.kt
 */
package app.morphe.patches.projectivy.misc.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.util.matchSingle
import app.morphe.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all premium features.",
) {
    compatibleWith(AppCompatibilities.PROJECTIVY)

    execute {
        PremiumCheckFingerprint.matchSingle().method.returnEarly(true)

        val trialStateSetter = TrialStateSetterFingerprint.matchSingle().method
        if (trialStateSetter.parameterTypes.lastOrNull()?.toString() != "Z") {
            throw PatchException("Trial state setter does not end in a boolean parameter")
        }
        trialStateSetter.addInstruction(0, "const/4 p1, 0x0")

        if (classDefBy(ACCESSIBILITY_SERVICE_CLASS).staticFields.any { it.type == "B" }) {
            val initializer = AccessibilityServiceByteInitFingerprint.matchSingle()
            val method = initializer.method
            if (method.instructions.count { it.opcode == Opcode.SPUT_BYTE } != 1) {
                throw PatchException("Accessibility service initializer stores more than one byte field")
            }
            val index = initializer.instructionMatches.first().index
            val register = method.getInstruction<OneRegisterInstruction>(index).registerA
            method.addInstruction(index, "const/4 v$register, 0x0")
        }
    }
}
