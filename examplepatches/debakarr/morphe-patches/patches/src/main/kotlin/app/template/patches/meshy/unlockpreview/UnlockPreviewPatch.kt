package app.template.patches.meshy.unlockpreview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESHY_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private const val HELPER = "Lapp/template/extension/extension/MeshyHelper;"

/**
 * Unlocks Meshy's gated flows — AR preview, model decrypt/export and sharing —
 * by making the customer info JS reads report an active `pro` entitlement.
 *
 * The three native Meshy modules (`meshyar`, `meshydecrypt`, `meshysharetarget`)
 * are ungated utilities; the only subscription check happens in the Hermes JS
 * layer against `customerInfo.entitlements.active`. That object is produced by
 * `EntitlementInfosMapperKt.map`, so we pipe its return value through
 * `MeshyHelper.meshyInjectProEntitlement`, which seeds a synthetic active `pro`
 * entry when the account has none. Because this is the single serialisation
 * point, every consumer of the mapped customer info (the `getCustomerInfo`
 * promise, update events, purchase callbacks) sees the unlocked state.
 */
@Suppress("unused")
val meshyUnlockPreviewPatch = bytecodePatch(
    name = "Unlock preview (free AR)",
    description = "Unlocks Meshy's AR preview, model decrypt/export and sharing by " +
        "reporting an active pro entitlement to the JS layer.",
    default = false,
) {
    compatibleWith(MESHY_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {
        // The helper must run on the value about to be returned, so insert
        // immediately before each `return-object`, piping its register through
        // `meshyInjectProEntitlement` (which returns the same, mutated map).
        EntitlementInfosMapperMapFingerprint.method.apply {
            val instructions = implementation!!.instructions.toList()
            val returns = instructions.indices.filter {
                instructions[it].opcode == Opcode.RETURN_OBJECT
            }
            if (returns.isEmpty()) {
                throw PatchException("EntitlementInfosMapperKt.map has no return-object")
            }
            // Back to front so earlier indices stay valid after each insert.
            returns.asReversed().forEach { idx ->
                val reg = (instructions[idx] as OneRegisterInstruction).registerA
                addInstructions(
                    idx,
                    """
                        invoke-static/range {v$reg .. v$reg}, $HELPER->meshyInjectProEntitlement(Ljava/util/Map;)Ljava/util/Map;
                        move-result-object v$reg
                    """.trimIndent(),
                )
            }
        }
    }
}
