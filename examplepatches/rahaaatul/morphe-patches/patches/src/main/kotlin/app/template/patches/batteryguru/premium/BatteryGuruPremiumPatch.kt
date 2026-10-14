package app.template.patches.batteryguru.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants
import app.template.patches.shared.returnEarly

fun MutableMethod.ensureRegisters(needed: Int) {
    val impl = implementation ?: return
    if (impl.registerCount >= needed) return
    try {
        val field = impl.javaClass.getDeclaredField("registerCount")
        field.isAccessible = true
        field.setInt(impl, needed)
    } catch (e: Exception) {}
}

fun MutableMethod.clearBody() {
    val impl = implementation ?: return
    val instructions = impl.instructions.toList()
    instructions.forEach { impl.removeInstruction(0) }
    try {
        val field = impl.javaClass.getDeclaredField("tryBlocks")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        (field.get(impl) as? MutableList<*>)?.clear()
    } catch (e: Exception) {}
}

@Suppress("unused")
val batteryGuruPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features and removes ads",
) {
    compatibleWith(Constants.BATTERYGURU_COMPATIBILITY)

    execute {
        IsPremiumFingerprint.method.returnEarly(true)

        To0HasPurchaseFingerprint.method.returnEarly(true)
    }
}
