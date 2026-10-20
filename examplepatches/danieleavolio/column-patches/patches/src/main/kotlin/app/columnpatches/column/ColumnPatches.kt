package app.columnpatches.column

import app.columnpatches.shared.Constants
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

private const val AD_BLOCKER = "Lapp/columnpatches/extension/AdBlocker;"

/**
 * A fixed, well formed but meaningless UUID.
 *
 * It is deliberately identical on every installation: it identifies nobody, and it
 * cannot be correlated over time the way a per-device identifier could be.
 */
private const val FAKE_ADVERTISING_ID = "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d"

/**
 * The one patch that actually changes the free-tier experience of Column.
 *
 * Column's own PERKS table (see chunk-7JNNW44E.js) lists "Zero pubblicità"
 * as the only premium-exclusive benefit. Everything else that is advertised as
 * premium ("read directly in the app", full articles, audio, Column AI) is
 * enforced by the backend (api.column.news + RevenueCat entitlement) and is not
 * decided by any code shipped inside the APK.
 *
 * This patch therefore implements the only client-side enforceable part:
 * refusing the network requests that fetch ads and trackers.
 *
 * It does not read, write or fake the entitlement/subscription state in any way.
 */
@Suppress("unused")
val blockAdsPatch = bytecodePatch(
    name = "Block ads and trackers",
    description = "Blocks ad and tracker requests made by Column's web view. " +
            "Column Premium (full articles, audio, Column AI) is validated server-side " +
            "and is intentionally not modified.",
    default = true,
) {
    compatibleWith(Constants.COLUMN)

    extendWith("extensions/adblock.mpe")

    execute {
        // Verified with `dexdump -d classes3.dex`:
        //   registers = 3, ins = 3  ->  v0 = p0 (this), v1 = p1 (WebView), v2 = p2 (WebResourceRequest)
        // The original body only touches v0 and v2, so v1 is a free scratch register.
        // v0 must NOT be reused here: it holds `this`, which the original code still needs.
        WebViewShouldInterceptRequestFingerprint.method.addInstructions(
            0,
            """
                invoke-static {p2}, $AD_BLOCKER->isBlockedRequest(Landroid/webkit/WebResourceRequest;)Z
                move-result v1
                if-eqz v1, :column_adblock_continue
                invoke-static {}, $AD_BLOCKER->blockedResponse()Landroid/webkit/WebResourceResponse;
                move-result-object v1
                return-object v1
                :column_adblock_continue
            """,
        )
    }
}

/**
 * Optional privacy patch: stops the AppsFlyer attribution SDK bundled in the app
 * from initialising and from sending the first session payload.
 *
 * Left opt-in (default = false) because AppsFlyer also powers the app's
 * referral/deep-link flow, which is the legitimate way to redeem Column Premium
 * codes. Turning this on can break referral links.
 */
@Suppress("unused")
val disableAttributionPatch = bytecodePatch(
    name = "Disable AppsFlyer attribution",
    description = "Prevents the bundled AppsFlyer SDK from initialising and sending " +
            "attribution data. May break referral / invite deep links.",
    default = false,
) {
    compatibleWith(Constants.COLUMN)

    execute {
        for (fingerprint in listOf(AppsFlyerInitFingerprint, AppsFlyerStartFingerprint)) {
            // Resolve the Capacitor plugin call immediately instead of running the SDK.
            fingerprint.method.addInstructions(
                0,
                """
                    invoke-virtual {p1}, Lcom/getcapacitor/PluginCall;->resolve()V
                    return-void
                """,
            )
        }
    }
}

/**
 * Privacy patch: replaces the Google advertising identifier (GAID) with a fixed,
 * non-unique value and turns on "limit ad tracking".
 *
 * The GAID is the last piece of the puzzle that lets an ad or measurement request be
 * tied back to this specific device. Blocking the ad requests themselves (see
 * blockAdsPatch) is not enough for that: the identifier is also sent to analytics and
 * attribution endpoints, and it is what re-associates a request with the device.
 */
@Suppress("unused")
val disableAdvertisingIdPatch = bytecodePatch(
    name = "Disable Advertising ID",
    description = "Replaces the Google advertising identifier (GAID) with a fixed, " +
            "non-unique value and enables limit ad tracking, so requests can no longer " +
            "be tied back to this device.",
    default = true,
) {
    compatibleWith(Constants.COLUMN)

    execute {
        // Verified with `dexdump -d classes3.dex`:
        //   registers = 10, ins = 1  ->  v0..v8 are free temporaries, p0 = v9.
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
