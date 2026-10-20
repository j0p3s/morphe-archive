package app.cinemaniacpatches.cine

import app.cinemaniacpatches.shared.Constants
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * A fixed, well formed but meaningless UUID.
 *
 * It is deliberately identical on every installation: it identifies nobody, and it
 * cannot be correlated over time the way a per-device identifier could be.
 */
private const val FAKE_ADVERTISING_ID = "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"

/**
 * Stops every ad request made by the bundled Google Mobile Ads SDK.
 *
 * Cinemaniac is a React Native app: the ads come from the native AdMob SDK, driven
 * by the io.invertase.googlemobileads bridge, and the ad units are declared in the
 * JavaScript bundle (publisher ca-app-pub-2990132554136404).
 *
 * The JavaScript layer cannot be patched with a dex patcher, because the bundle is
 * Hermes bytecode. The SDK entry points can, though, and that is where it is
 * cleaner to intervene anyway: no request is ever sent, so no ad is downloaded, no
 * impression is reported and no tracking pixel fires.
 *
 * Known limitation: MobileAds.initialize() is deliberately left alone. Neutralising
 * it would leave the "ads initialised" promise of the React Native bridge pending
 * forever and the app would hang on start up.
 *
 * This does not unlock any PRO feature. The PRO tier is validated server side
 * against api.cinemaniac.app, and its entitlement is never read, written or spoofed.
 */
@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Stops the bundled Google Mobile Ads SDK from requesting any ad " +
            "(banner, interstitial, rewarded, app open, native), so no ad is " +
            "downloaded, rendered or reported.",
    default = true,
) {
    compatibleWith(Constants.CINEMANIAC)

    execute {
        for (fingerprint in AdMobLoadFingerprints.all) {
            // All of these methods return void and none of them has a try/catch
            // block, so a single return-void at index 0 is enough and cannot
            // disturb any register or exception range.
            fingerprint.method.addInstructions(
                0,
                """
                    return-void
                """,
            )
        }
    }
}

/**
 * Replaces the Google advertising identifier (GAID) with a fixed, non-unique value
 * and turns on "limit ad tracking".
 *
 * The GAID is what allows an ad or measurement request to be associated with this
 * specific device. Even with the ad requests themselves blocked, the identifier
 * would still be handed to the SDK during initialisation and to the attribution and
 * analytics endpoints.
 */
@Suppress("unused")
val disableAdvertisingIdPatch = bytecodePatch(
    name = "Disable Advertising ID",
    description = "Replaces the Google advertising identifier (GAID) with a fixed, " +
            "non-unique value and enables limit ad tracking, so requests can no longer " +
            "be tied back to this device.",
    default = true,
) {
    compatibleWith(Constants.CINEMANIAC)

    execute {
        // Verified with `dexdump -d classes.dex`:
        //   registers = 8, ins = 1  ->  v0..v6 are free temporaries.
        // AdvertisingIdClient$Info.<init>(String, boolean) is a public constructor.
        AdvertisingIdFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/google/android/gms/ads/identifier/AdvertisingIdClient${'$'}Info;
                const-string v1, "$FAKE_ADVERTISING_ID"
                const/4 v2, 0x1
                invoke-direct {v0, v1, v2}, Lcom/google/android/gms/ads/identifier/AdvertisingIdClient${'$'}Info;-><init>(Ljava/lang/String;Z)V
                return-object v0
            """,
        )
    }
}
