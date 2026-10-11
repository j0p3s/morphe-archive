package app.clearscanner.patches.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.clearscanner.patches.shared.Constants.COMPATIBILITY_CLEAR_SCANNER
import com.android.tools.smali.dexlib2.Opcode

/**
 * Remote config fields (on the obfuscated config class, names valid for 10.2.18) that turn ads on.
 *
 * Key in Firebase Remote Config -> field
 *   ads_banner_display                -> o   banner ads
 *   ads_app_open_display              -> q   app open ads
 *   ads_app_open_cold_start           -> s   app open ad on cold start
 *   ads_display_on_share_done_v2      -> h   interstitial after share
 *   ads_display_on_print_done         -> i   interstitial after print
 *   ads_display_on_filter_done        -> j   interstitial after filter
 *   ads_display_on_cloud_sync_success -> k   interstitial after cloud sync
 *   ads_inter_cache                   -> l   interstitial preloading
 *   ads_app_open_cache                -> w   app open preloading
 *   ads_show_pro_fallback_banner      -> d0  "get Pro" banner shown when no ad fills
 *   ads_show_pro_fallback_interstitial-> e0  "get Pro" interstitial shown when no ad fills
 */
private val AD_FLAG_FIELDS = listOf("o", "q", "s", "h", "i", "j", "k", "l", "w", "d0", "e0")

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes banner, interstitial and app open ads, and the \"get Pro\" ad banners.",
    default = true
) {
    compatibleWith(COMPATIBILITY_CLEAR_SCANNER)

    execute {
        // Force every ad switch off, both for the defaults (constructor) and after each time
        // the remote config is applied (so a server side config can not turn ads back on).
        listOf(
            RemoteConfigConstructorFingerprint.method,
            RemoteConfigLoadFingerprint.method
        ).forEach { method ->
            val owner = method.definingClass
            val returnIndex = method.implementation!!.instructions
                .indexOfLast { it.opcode == Opcode.RETURN_VOID }

            val smali = buildString {
                appendLine("const/4 v0, 0x0")
                AD_FLAG_FIELDS.forEach { field ->
                    appendLine("iput-boolean v0, p0, $owner->$field:Z")
                }
            }

            method.addInstructions(returnIndex, smali)
        }

        // Interstitials are shown whenever one has been loaded, regardless of the switches above,
        // so also stop the ad manager from ever loading one.
        LoadInterstitialFingerprint.method.addInstructions(0, "return-void")
    }
}
