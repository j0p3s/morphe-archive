package app.morphe.patches.multiaccounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.Constants.COMPATIBILITY_MULTIACCOUNTS
import app.morphe.patches.shared.clearTryBlocks
import app.morphe.patches.shared.ensureRegisterCount
import app.morphe.patches.shared.replaceWithReturnBoolean
import app.morphe.patches.shared.replaceWithReturnVoid

// ── Splash & Ad Gate Fingerprints ──────────────────────────────────────────

private val splashDelayTimerFingerprint = Fingerprint(
    definingClass = "Lcom/pi1d/l6v/ahi33xca/dhy60gm84keol;",
    name = "u",
    parameters = emptyList(),
    returnType = "V",
)

private val adManagerAppStartFingerprint = Fingerprint(
    definingClass = "Li1/v1;",
    name = "k0",
    parameters = listOf("Landroid/app/Activity;", "I", "Lie/a;"),
    returnType = "V",
)

private val adManagerMainSplashFingerprint = Fingerprint(
    definingClass = "Li1/v1;",
    name = "n0",
    parameters = listOf("Landroid/content/Context;", "Lie/a;", "Lie/a;", "Lie/a;"),
    returnType = "V",
)

private val dataStoreCanRequestAdsFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/datastore/DataStore;",
    name = "O1",
    parameters = emptyList(),
    returnType = "Z",
)

// ── Rating & Review Prompt Fingerprints ────────────────────────────────────

private val gradePromptFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/util/j9;",
    name = "o",
    parameters = listOf("Landroid/app/Activity;"),
    returnType = "Z",
)

// ── Main Screen Native Promo Banner Fingerprints ───────────────────────────

private val nativeBannerSetVisibilityFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "m",
    parameters = listOf("Z"),
    returnType = "V",
)

private val nativeBannerLoadData1Fingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "l",
    parameters = listOf("Ljava/util/List;"),
    returnType = "V",
)

private val nativeBannerLoadData2Fingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "z",
    parameters = listOf("Ljava/util/List;"),
    returnType = "V",
)

private val nativeBannerAutoSwitchSFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "s",
    parameters = emptyList(),
    returnType = "V",
)

private val nativeBannerAutoSwitchTFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "t",
    parameters = emptyList(),
    returnType = "V",
)

private val nativeBannerAutoSwitchUFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/ads/view/NativeBanner;",
    name = "u",
    parameters = emptyList(),
    returnType = "V",
)

// ── First-Run Consent / Onboarding Fingerprints ─────────────────────────────

private val privacyRightsShowFingerprint = Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/util/tg;",
    name = "d",
    parameters = listOf("Landroid/content/Context;", "Lcom/excelliance/kxqp/util/tg${'$'}a;"),
    returnType = "V",
)

// ── Main Patch Definition ──────────────────────────────────────────────────

@Suppress("unused")
val multiaccountsUxDeclutterPatch = bytecodePatch(
    name = "UX Declutter",
    description = "Removes splash delays, short-circuits cloned-app launch ad gates, hides main screen promo banners, and suppresses rating prompts.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)

    val skipSplashDelay by booleanOption(
        key = "skipSplashDelay",
        default = true,
        title = "Skip Splash Delay and Ad Stalls",
        description = "Eliminates the 1-second cold start delay timer in the launcher activity and short-circuits splash/launch ad gates so cloned apps and the main screen launch immediately.",
    )

    val suppressRatingPrompt by booleanOption(
        key = "suppressRatingPrompt",
        default = true,
        title = "Suppress Rating Prompts",
        description = "Suppresses in-app 5-star rating dialogs and review prompts.",
    )

    val hideNativeBanner by booleanOption(
        key = "hideNativeBanner",
        default = true,
        title = "Hide Main Screen Native Promo Banner",
        description = "Hides the native ad/promo banner on the main workspace screen and disables banner paging routines.",
    )

    val skipFirstRunWizard by booleanOption(
        key = "skipFirstRunWizard",
        default = true,
        title = "Skip First-Run Privacy Wizard",
        description = "Bypasses the first-run privacy consent dialog and proceeds directly to workspace initialization.",
    )

    execute {
        val doSkipSplash = skipSplashDelay ?: true
        val doSuppressRating = suppressRatingPrompt ?: true
        val doHideBanner = hideNativeBanner ?: true
        val doSkipFirstRun = skipFirstRunWizard ?: true

        if (!doSkipSplash && !doSuppressRating && !doHideBanner && !doSkipFirstRun) {
            println("[Multiaccounts UX] All declutter options disabled - skipping.")
            return@execute
        }

        var patched = 0
        val hookCounts = mutableMapOf<String, Int>()

        // 1. Splash Delay & Ad Load Gates
        if (doSkipSplash) {
            var splashCount = 0

            // Launcher Activity delay cut: dhy60gm84keol.u() -> dhy60gm84keol.v(p0)
            val splashMethod = splashDelayTimerFingerprint.method
            splashMethod.clearTryBlocks()
            splashMethod.ensureRegisterCount(2)
            splashMethod.removeInstructions(0, splashMethod.implementation?.instructions?.count() ?: 0)
            splashMethod.addInstructions(
                0,
                """
                move-object/16 v0, p0
                invoke-static {v0}, Lcom/pi1d/l6v/ahi33xca/dhy60gm84keol;->v(Lcom/pi1d/l6v/ahi33xca/dhy60gm84keol;)V
                return-void
                """.trimIndent(),
            )
            splashCount++

            // Clone launch ad gate: v1.k0(Activity, int, Function0) -> onNext.invoke()
            val k0Method = adManagerAppStartFingerprint.method
            k0Method.clearTryBlocks()
            k0Method.ensureRegisterCount(2)
            k0Method.removeInstructions(0, k0Method.implementation?.instructions?.count() ?: 0)
            k0Method.addInstructionsWithLabels(
                0,
                """
                move-object/16 v0, p3
                if-eqz v0, :cond_skip
                invoke-interface {v0}, Lie/a;->invoke()Ljava/lang/Object;
                :cond_skip
                return-void
                """.trimIndent(),
            )
            splashCount++

            // Main screen splash ad gate: v1.n0(Context, Function0, Function0, Function0) -> onNext.invoke()
            val n0Method = adManagerMainSplashFingerprint.method
            n0Method.clearTryBlocks()
            n0Method.ensureRegisterCount(2)
            n0Method.removeInstructions(0, n0Method.implementation?.instructions?.count() ?: 0)
            n0Method.addInstructionsWithLabels(
                0,
                """
                move-object/16 v0, p3
                if-eqz v0, :cond_skip
                invoke-interface {v0}, Lie/a;->invoke()Ljava/lang/Object;
                :cond_skip
                return-void
                """.trimIndent(),
            )
            splashCount++

            // Global ad request gate: DataStore.O1()Z -> false
            dataStoreCanRequestAdsFingerprint.method.replaceWithReturnBoolean(false)
            splashCount++

            hookCounts["Splash & Launch Gates"] = splashCount
            patched += splashCount
        }

        // 2. Rating & Review Prompts
        if (doSuppressRating) {
            gradePromptFingerprint.method.replaceWithReturnBoolean(false)
            hookCounts["Rating Prompts"] = 1
            patched++
        }

        // 3. Main Screen Native Promo Banner
        if (doHideBanner) {
            var bannerCount = 0

            // Force View.GONE (0x8) in NativeBanner.m(Z)V
            val bannerMethod = nativeBannerSetVisibilityFingerprint.method
            bannerMethod.clearTryBlocks()
            bannerMethod.ensureRegisterCount(2)
            bannerMethod.removeInstructions(0, bannerMethod.implementation?.instructions?.count() ?: 0)
            bannerMethod.addInstructions(
                0,
                """
                move-object/16 v0, p0
                const/16 v1, 0x8
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                return-void
                """.trimIndent(),
            )
            bannerCount++

            // Stub slide/ad list ingestion methods
            nativeBannerLoadData1Fingerprint.method.replaceWithReturnVoid()
            bannerCount++
            nativeBannerLoadData2Fingerprint.method.replaceWithReturnVoid()
            bannerCount++

            // Stub auto-switch page lifecycle handlers
            nativeBannerAutoSwitchSFingerprint.method.replaceWithReturnVoid()
            bannerCount++
            nativeBannerAutoSwitchTFingerprint.method.replaceWithReturnVoid()
            bannerCount++
            nativeBannerAutoSwitchUFingerprint.method.replaceWithReturnVoid()
            bannerCount++

            hookCounts["Native Banner"] = bannerCount
            patched += bannerCount
        }

        // 4. First-Run Privacy Wizard
        if (doSkipFirstRun) {
            // PrivacyRightsUtil.d(Context, tg$a) -> callback.b()
            val privacyMethod = privacyRightsShowFingerprint.method
            privacyMethod.clearTryBlocks()
            privacyMethod.ensureRegisterCount(2)
            privacyMethod.removeInstructions(0, privacyMethod.implementation?.instructions?.count() ?: 0)
            privacyMethod.addInstructionsWithLabels(
                0,
                """
                move-object/16 v0, p2
                if-eqz v0, :cond_skip
                invoke-interface {v0}, Lcom/excelliance/kxqp/util/tg${'$'}a;->b()V
                :cond_skip
                return-void
                """.trimIndent(),
            )
            hookCounts["First-Run Wizard"] = 1
            patched++
        }

        if (patched == 0) {
            error("[Multiaccounts UX] No UX declutter targets were patched - failing closed.")
        }

        val summary = hookCounts.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        println("[Multiaccounts UX] Applied $patched declutter hooks ($summary).")
    }
}
