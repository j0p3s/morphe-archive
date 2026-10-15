package app.template.patches.expertplayer.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_EXPERTPLAYER

@Suppress("unused")
val enablePremiumPatch = bytecodePatch(
    name = "Enable Premium",
    description = "Experimental: marks every RevenueCat entitlement as active on the Java side, " +
        "which is what the app's Dart premium check reads. Server-side features (account sync, " +
        "recordings, the always-current EPG) still depend on the Expert backend and are not changed.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_EXPERTPLAYER)

    execute {
        // Writes isActive = true before the original field store, so the value stored is always true.
        EntitlementInfoConstructorFingerprint.method.addInstructions(
            0,
            "const/4 p2, 0x1",
        )
    }
}
