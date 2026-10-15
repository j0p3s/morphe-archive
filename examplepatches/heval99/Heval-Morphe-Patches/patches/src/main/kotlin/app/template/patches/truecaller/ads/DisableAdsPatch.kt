package app.template.patches.truecaller.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val MutableMethod.parameterList get() = parameterTypes.map(CharSequence::toString)

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Hides banner and native ads and turns off interstitial and app-open ads, so " +
        "actions such as Click to WhatsApp run at once.",
) {
    compatibleWith(COMPATIBILITY_TRUECALLER)

    execute {
        // The ads module never consults Premium or the NO_ADS feature, so "Enable Premium" does
        // not remove ads by itself.

        // 1. Banner and native ads. Each bind method makes its container VISIBLE and attaches
        // the ad view; instead it now sets the container GONE and returns, so no ad view is
        // attached, rendered or reported as inflated. Hooking the bind methods (rather than
        // pinning setVisibility to GONE) also skips the ad's own impression and click wiring.
        val bannerHolder = AdsSwitchViewBindFingerprint.method.parameterList.first()
        val adsContainer = mutableClassDefBy(ADS_CONTAINER)
        val adsContainerLight = mutableClassDefBy(ADS_CONTAINER_LIGHT)
        // The banner bind's second parameter is the layout config; the native binds take the same
        // config after their native ad holder, which gives the native holder types.
        val layoutConfig = adsContainer.methods.firstOrNull {
            it.returnType == "V" && it.parameterList.size >= 2 && it.parameterList.first() == bannerHolder
        }?.parameterList?.get(1) ?: throw PatchException("$ADS_CONTAINER has no banner bind method")
        val nativeHolders = (adsContainer.methods + adsContainerLight.methods)
            .map { it.parameterList }
            .filter { it.size >= 2 && it[1] == layoutConfig && it[0] != bannerHolder }
            .map { it.first() }
            .toSet()
        if (nativeHolders.isEmpty()) throw PatchException("No native ad bind method found")
        val adTypes = nativeHolders + bannerHolder + AD_LAYOUT_TYPE

        for (classDef in listOf(adsContainer, adsContainerLight, mutableClassDefBy(ADS_SWITCH_VIEW))) {
            val binds = classDef.methods.filter { method ->
                method.returnType == "V" && method.name != "<init>" && method.implementation != null &&
                    !AccessFlags.STATIC.isSet(method.accessFlags) &&
                    method.parameterList.firstOrNull()?.startsWith("L") == true &&
                    method.parameterList.any { it in adTypes }
            }
            if (binds.isEmpty()) throw PatchException("${classDef.type} has no ad bind method")
            // p1 (an object reference) is free once the method returns at once; invoke/range
            // reaches p0..p1 whatever the register count.
            binds.forEach {
                it.addInstructions(
                    0,
                    """
                        const/16 p1, 0x8
                        invoke-virtual/range {p0 .. p1}, Landroid/view/View;->setVisibility(I)V
                        return-void
                    """,
                )
            }
        }

        // 2. Interstitial ads: the manager reports them as off, so each placement runs the user's
        // action (open WhatsApp, call details, block, pay, ...) at once. Closing the ad activities
        // instead dropped that action, which only runs when the ad is dismissed. The ads-router
        // interstitial and app-open flows start only from this gated flow. Rewarded ads, which
        // share AdInterstitialActivity, are left alone: they gate a feature.
        InterstitialAdsEnabledFingerprint.method.returnEarly(false)

        // 3. App-open loading screen: finish it right after super.onCreate, the point where the
        // framework expects onCreate to have run. It is only reached from the flow gated above.
        val onCreate = mutableClassDefBy(AD_APP_OPENING_ACTIVITY).methods.firstOrNull {
            it.name == "onCreate" && it.parameterList == listOf("Landroid/os/Bundle;") && it.implementation != null
        } ?: throw PatchException("$AD_APP_OPENING_ACTIVITY has no onCreate(Bundle)")
        val superCall = onCreate.implementation!!.instructions.indexOfFirst { instruction ->
            val call = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            (instruction.opcode == Opcode.INVOKE_SUPER || instruction.opcode == Opcode.INVOKE_SUPER_RANGE) &&
                call?.name == "onCreate"
        }
        if (superCall < 0) throw PatchException("$AD_APP_OPENING_ACTIVITY.onCreate does not call super.onCreate")
        onCreate.addInstructions(
            superCall + 1,
            """
                invoke-virtual/range {p0 .. p0}, Landroid/app/Activity;->finish()V
                return-void
            """,
        )
    }
}
