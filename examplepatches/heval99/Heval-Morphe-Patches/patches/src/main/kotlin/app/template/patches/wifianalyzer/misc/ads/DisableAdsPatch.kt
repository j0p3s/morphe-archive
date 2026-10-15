package app.template.patches.wifianalyzer.misc.ads

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_WIFIANALYZER

/**
 * Ads are gated by Settings.a(Context), which reads the "next_show_ad_time_millisec"
 * preference and returns true once the hide-until date passes. Every ad checks it:
 * the main banner and the interstitial (through MainScreen's gate wrapper) and the
 * LAN neighbors banner (directly). Forcing the gate false disables all three.
 *
 * Settings is not obfuscated, but the gate is located by its stable preference
 * key plus shape (the only static no-arg... single-Context boolean with a body
 * reading the key), so the patch survives any future rename.
 */
private const val HIDE_AD_KEY = "next_show_ad_time_millisec"
private const val CONTEXT = "Landroid/content/Context;"

private fun BytecodePatchContext.adGateClass(): MutableClass {
    val classDef = classDefByStrings(HIDE_AD_KEY).singleOrNull()
        ?: error("WiFi Analyzer ad gate class not found (key '$HIDE_AD_KEY')")
    return mutableClassDefBy(classDef)
}

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Disables the banner and full-screen ads."
) {
    compatibleWith(COMPATIBILITY_WIFIANALYZER)

    execute {
        // The show-ad gate: the only static single-Context boolean with a body
        // in the class owning the hide-until preference key.
        val settings = adGateClass()
        val adGate = settings.methods.firstOrNull { method ->
            method.returnType == "Z" && method.implementation != null &&
                method.parameterTypes.map(CharSequence::toString) == listOf(CONTEXT)
        } ?: error("WiFi Analyzer ad gate not found in ${settings.type}")

        adGate.returnEarly(false)
    }
}
