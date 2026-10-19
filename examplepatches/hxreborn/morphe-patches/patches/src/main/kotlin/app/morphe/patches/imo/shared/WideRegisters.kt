/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.shared

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

private val WIDE_OPCODE_MARKERS = listOf("wide", "long", "double")

internal fun Method.wideRegisterPairs() = implementation!!.instructions
    .filter { instruction -> WIDE_OPCODE_MARKERS.any { it in instruction.opcode.name } }
    .flatMap { instruction ->
        when (instruction) {
            is ThreeRegisterInstruction -> listOf(instruction.registerA, instruction.registerB, instruction.registerC)
            is TwoRegisterInstruction -> listOf(instruction.registerA, instruction.registerB)
            is OneRegisterInstruction -> listOf(instruction.registerA)
            else -> emptyList()
        }
    }
    .flatMap { listOf(it, it + 1) }
    .distinct()
