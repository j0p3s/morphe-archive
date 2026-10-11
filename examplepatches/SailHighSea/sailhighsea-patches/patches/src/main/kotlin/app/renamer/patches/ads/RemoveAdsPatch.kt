package app.renamer.patches.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.renamer.patches.shared.Constants.COMPATIBILITY_RENAMER

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes the banner ad and the interstitial ads.",
    default = true
) {
    compatibleWith(COMPATIBILITY_RENAMER)

    execute {
        // Banner: never load an ad, and hide the AdView (resource id 0x7f09005e, the banner view)
        // so no empty space is left at the bottom of the screen.
        InitBannerAdFingerprint.method.addInstructions(
            0,
            """
                const v0, 0x7f09005e
                invoke-virtual { p0, v0 }, Lcom/aj/renamer/MainActivity;->findViewById(I)Landroid/view/View;
                move-result-object v0
                const/16 v1, 0x8
                invoke-virtual { v0, v1 }, Landroid/view/View;->setVisibility(I)V
                return-void
            """
        )

        // Interstitial: ignore the loaded ad. The app only shows an interstitial
        // when it was stored here, so none is ever shown.
        InterstitialAdLoadedFingerprint.method.addInstructions(0, "return-void")
    }
}
