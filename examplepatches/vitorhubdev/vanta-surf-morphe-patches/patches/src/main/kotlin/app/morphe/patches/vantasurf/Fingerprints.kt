package app.morphe.patches.vantasurf

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

/**
 * Fingerprints for Vanta Surf.
 *
 * The app is obfuscated and the obfuscator renames the classes on every release,
 * so no fingerprint may rely on a class name. The method names that the
 * obfuscator keeps are stable, and everything else is matched on the
 * instructions of the method.
 */

/**
 * `SemProduceStep.interceptBeforePlay(I)Z` is the single entry point used by the
 * player to ask the ad manager whether playback should be interrupted.
 *
 * Returning `false` means "play the episode", which removes the interstitial ads.
 */
internal object InterceptBeforePlayFingerprint : Fingerprint(
    name = "interceptBeforePlay",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf("I"),
    filters = listOf(
        methodCall(
            name = "shouldShowAd",
            returnType = "Z",
            parameters = listOf("I"),
        ),
    ),
)

/**
 * `SemProtocolPartial.showNativeAd(View)V` injects a native ad into the feed.
 */
internal object ShowNativeAdFingerprint : Fingerprint(
    name = "showNativeAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
)

/**
 * The free ad popup flow of 5.0.2 and newer.
 *
 * The method shows the "watch an ad to keep watching" dialog and returns `true`
 * when it took over the playback. It is matched on its log strings, because the
 * method name is obfuscated.
 */
internal object FreeAdPopupFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("I", "I"),
    strings = listOf("FREE_AD_POPUP_SHOW"),
)

/**
 * Decides whether resuming playback is blocked by the free ad popup of 5.0.2
 * and newer. Returning `false` means playback is never blocked.
 */
internal object ResumeBlockedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("FREE_AD_POPUP_RESUME_BLOCKED"),
)

/**
 * Builds the ad item of the "ad gate" dialog of 5.0.0 and 5.0.1.
 *
 * The gate scenarios are "watch an ad to start this video", "watch an ad to
 * continue" and "unlock this episode". When the ad item cannot be built, the
 * player logs `complete_gate_without_ad` and lets the video play, so returning
 * `null` unlocks the content.
 *
 * The method name and the scenario class are obfuscated on every release, so the
 * fingerprint only uses the return type of the ad item and the calls that are
 * made on the scenario.
 */
internal object AdGateItemFingerprint : Fingerprint(
    returnType = "Lcom/mgs/playercore/commerce/AdItem;",
    parameters = listOf("L"),
    custom = custom@{ method, _ ->
        val calledMethods = method.implementation?.instructions
            ?.filterIsInstance<ReferenceInstruction>()
            ?.mapNotNull { (it.reference as? MethodReference)?.name }
            ?: return@custom false

        calledMethods.contains("getAdTitle") && calledMethods.contains("getDurationMs")
    },
)

/**
 * The application onCreate method that initializes the attribution SDKs.
 *
 * `Adjust` is the first call of the analytics block, so returning before it
 * also skips the Facebook App Events logger, the advertising ID collection
 * and the AppsFlyer SDK.
 */
internal object AnalyticsInitFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/adjust/sdk/Adjust;",
            name = "initSdk",
        ),
    ),
)
