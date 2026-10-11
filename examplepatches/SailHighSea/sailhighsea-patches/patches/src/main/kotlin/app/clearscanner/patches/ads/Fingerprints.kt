package app.clearscanner.patches.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Clear Scanner 10.2.18 (com.indymobileapp.document.scanner) is obfuscated, so these fingerprints
 * anchor on string constants that survive obfuscation.
 */

/**
 * The remote config holder (obfuscated `by3`). Its `f()` method copies Firebase Remote Config
 * values into fields, including the `ads_*` display switches.
 */
object RemoteConfigLoadFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        string("ads_banner_display"),
        string("ads_app_open_display"),
    )
)

/** Constructor of the remote config holder. Sets the defaults used before the config is fetched. */
object RemoteConfigConstructorFingerprint : Fingerprint(
    classFingerprint = RemoteConfigLoadFingerprint,
    name = "<init>",
    parameters = listOf(),
)

/** The app's ad manager singleton (obfuscated `ss3`). Its constructor contains its log tag. */
object AdManagerFingerprint : Fingerprint(
    name = "<init>",
    parameters = listOf(),
    filters = listOf(
        string("PSAdManager:"),
    )
)

/** `AdManager.loadInterstitial(BaseActivity, boolean)`. The only place interstitials get loaded. */
object LoadInterstitialFingerprint : Fingerprint(
    classFingerprint = AdManagerFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L", "Z"),
)
