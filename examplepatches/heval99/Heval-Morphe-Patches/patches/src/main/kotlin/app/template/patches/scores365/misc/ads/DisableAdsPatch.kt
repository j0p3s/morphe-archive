package app.template.patches.scores365.misc.ads

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_365SCORES
import com.android.tools.smali.dexlib2.iface.ClassDef

// The app's own ad gates. Declared on an R8-renamed interface and implemented by the readable
// base activities (BaseActionBarActivity, BottomNavigationActivity, SingleEntityDashboardActivity,
// GameCenterBaseActivity, WebViewActivity) plus a few R8-renamed classes. The names survive R8,
// so every concrete override is found by name and shape.
private val APP_AD_GATES = setOf("showAdsForContext", "isBannerNeedToBeShown")
private const val BASE_ACTION_BAR_ACTIVITY = "Lcom/scores365/Design/Activities/BaseActionBarActivity;"

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Disables Google Mobile Ads (AdMob) initialization and the app's own ad " +
            "placements, removing banner, interstitial, rewarded, native and mediated ads."
) {
    compatibleWith(COMPATIBILITY_365SCORES)

    execute {
        // Both overloads are entry points used by the Blaze GAM SDK wrapper.
        MobileAdsInitializeFingerprint.method.returnEarly()
        MobileAdsInitializeWithListenerFingerprint.method.returnEarly()

        // The app-side gates return false, so no banner slot or interstitial is ever requested
        // and the layout keeps no empty ad space.
        val owners = mutableListOf<ClassDef>()
        classDefForEach { classDef ->
            if (classDef.methods.any { it.isAppAdGate() }) owners += classDef
        }
        if (owners.none { it.type == BASE_ACTION_BAR_ACTIVITY }) {
            throw PatchException("$BASE_ACTION_BAR_ACTIVITY no longer implements the app ad gates")
        }
        for (owner in owners) {
            mutableClassDefBy(owner).methods.filter { it.isAppAdGate() }.forEach { it.returnEarly(false) }
        }
    }
}

private fun com.android.tools.smali.dexlib2.iface.Method.isAppAdGate() =
    name in APP_AD_GATES && returnType == "Z" && parameterTypes.isEmpty() && implementation != null
