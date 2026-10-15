package app.morphe.patches.shared.extension.app.morphe.patcher.util.proxy.mutableTypes

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod

fun MutableMethod.clearInstructions() {
    removeInstructions(0, instructions.size)
}
