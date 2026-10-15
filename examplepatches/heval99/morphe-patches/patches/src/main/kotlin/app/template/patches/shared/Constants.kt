package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    private const val FLASHSCORE_SOURCE_NOTE = "Download the .apks from apkcube.com"

    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_SOFASCORE = Compatibility(
        name = "Sofascore", // App name as it appears in the Android launcher.
        packageName = "com.sofascore.results",
        apkFileType = ApkFileType.APKM, // Preferred or recommended file type.
        appIconColor = 0x384CF4, // Icon color in Morphe Manager.
        targets = listOf(
            // "version = null" means the patch works with the latest app target
            // and is expected to work with all future app targets.
            //
            // It is highly recommended to always include the exact app version you developed your patches for
            // or the last version you have confirmed as 100% working.
            // History (26.07.27 to 26.09.14 were supported until 2026-10-12, see the last note):
            // Verified 2026-09-17: 26.08.24 (the build users reported failing with a
            // NullPointerException in Disable telemetry - issue #7) patches cleanly with all
            // 5 patches. The crash came from returnEarly() being applied to an abstract
            // AppsFlyerLib.logEvent declaration; the fingerprint now requires an actual
            // method body, and the emitted bytecode was checked on this version.
            // Verified 2026-09-17 against com.sofascore.results 26.09.07 (versionCode 260907002,
            // universal, Android 12L+). This build shuffled a lot: the app-level premium gates
            // the old "Enable Premium" fingerprints targeted (aiInsights/removeAds/PremiumToken/
            // isPremium) are gone, FirebaseAnalytics.logEvent was removed, Adjust is no longer
            // bundled and the Facebook/Crashlytics SDK classes are R8-renamed. The patches were
            // re-anchored:
            //   - Disable ads: unchanged; UserAccount getForceAds/getForceHideAds/getHasServerAds
            //     are still unobfuscated.
            //   - Enable Premium: forces UserAccount.getHasPremium() and ProfileData.getHasPremium(),
            //     the two boxed-Boolean flags every premium gate reads.
            //   - Disable telemetry: AppsFlyer Lib subclass logEvent, AppMeasurementSdk.logEvent,
            //     and Crashlytics (located via its collection-enabled SharedPreferences key).
            //   - Disable Facebook SDK: AudienceNetworkContentProvider onCreate only, plus
            //     manifest flags disabling FB auto-log/advertiser-ID. FacebookInitProvider
            //     must NOT be killed: the login screen builds FB Login handlers and crashes
            //     with "SDK has not been initialized" without sdkInitialize() (issue #24,
            //     fixed 2026-09-30; verified on 26.09.14).
            // The "Block marketing notifications" patch dismisses two in-app promo
            // sheets only; it cannot affect push. Missing match alerts on re-signed
            // builds traced to push delivery itself (issue #25); "GmsCore support" and
            // "Spoof signature" (added 2026-10-12) cover that.
            //   - Block marketing notifications: PromotionModal / tennis promo bottom sheet
            //     onViewCreated dismiss the sheet before it renders.
            // The "Disable Play Integrity" patch was dropped: this build does not bundle the
            // Play Integrity classes (the AppsFlyer SDK only references them, behind a catch),
            // so the patch had nothing to patch.
            // All 5 patches apply cleanly; SofascoreSmokeTest asserts the emitted bytecode.
            // Re-verified 2026-09-28 against 26.09.14 (versionCode 260914002, from
            // APKMirror): all 5 patches apply unchanged and the decompiled output shows
            // the same forced values (getHasPremium -> TRUE, ad flags false).
            // Verified 2026-10-07 against 26.09.28 (versionCode 260928002, APKPure bundle
            // merged). The tennis AI-insights promo sheet was removed in this build, so Block
            // marketing notifications treats it as optional (PromotionModal stays required)
            // and now also keeps the PromotionBannerView / PromotionalOffersBannerView
            // promotion banners GONE. The other four patches apply unchanged. Enable Premium's
            // description now states that server-served premium content (AI insights) is
            // not unlocked (issue #32).
            // 2026-10-12, 26.09.28 (still the latest on APKMirror): added "GmsCore support"
            // (shared builder, off by default; FCM push via FCMListenerService and Google login
            // via androidx.credentials GetGoogleIdOption, launch activity main.start.StartActivity;
            // here R8 inlined isGooglePlayServicesAvailable into GoogleApiAvailabilityLight.b, so
            // the shared fingerprint now anchors on its signature warning) and "Spoof signature"
            // (CN=Sofa Score cert, SHA-1 e235776b...4d23, from the APKMirror base APK; Firebase
            // checks it, issues #24/#25). Block marketing notifications now also no-ops every
            // FirebaseInAppMessagingDisplay.displayMessage, so FIAM campaign popups never show.
            // Assessed and rejected: forcing UserAccount.isLoggedIn (predictions, sync, chat all
            // need a real server token), update/review prompts (no Play Core, no custom update
            // screen), odds/betting, paywall and survey/popup surfaces (server-driven). The older
            // targets were dropped: the new patches were only verified on 26.09.28.
            AppTarget(
                version = "26.09.28",
                versionCode = 260928002
            )
        )
    )

    // FotMob phone build. Note the Wear OS build shares the same package and its versions
    // look like "236.253021660w..."; the APKPure/APKMirror *latest* lookups sometimes return
    // the watch APK, so always check android.hardware.type.watch before using an APK.
    // Verified 2026-09-17 against 236.17398.20260827 (versionCode 17398) and
    // 236.17338.20260822 (the build from issue #8) from APKMirror; both patch cleanly.
    // The subscription manager is R8-obfuscated; the patch anchors it on the staff-account
    // email strings and identifies the subscription getter structurally (see
    // fotmob/plus/Fingerprints.kt), so obfuscated name rotation no longer breaks it.
    // Verified 2026-09-18 against 237.17536.20260911 (versionCode 17536, the current APKMirror
    // phone build). This release swapped the storage layer under the getter (236 read
    // SharedPreferencesRepository.get(Z), 237 goes through a settings holder), so the getter
    // search is now storage-agnostic: the manager's only non-constant no-arg boolean method
    // that does not hold the staff email. Both old 236 targets were re-checked and still pass.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    // 2026-10-13, 238.17794.20261012 (versionCode 17794): "Enable FotMob+" forces the
    // subscription manager's valid_subscription getter true and the staff check false, which
    // removes ads and sends More > FotMob+ to MembershipActivity instead of PaywallActivity (both
    // seen on the emulator). That screen still said "No active subscriptions found" and the old
    // patch never exposed the "Exclusive app icons" picker: MembershipActivity reads the
    // RevenueCat entitlements from the server, so it cannot honestly show an active plan, and
    // the picker was drawn only in its entitlement branches. A new step calls the picker in the
    // NoEntitlement branch too: the screen lambda is the only method with that literal; the
    // picker is the MembershipActivity (I, composer)V method its entitlement branches already
    // call (T, 3 calls, reads string/app_icons 0x7f1300c3), on the same activity register; the
    // composer type comes from the branch's end-group (Z)V call. The step is skipped (logged)
    // when the literal is absent, and skipped with a warning (steps 1-2 still apply) when the
    // screen code has another shape. RevenueCat records and the valid_subscription pref are not
    // faked. "Early access" is marketing text only, with no client gate to unlock.
    // FotMobSmokeTest passes on 238.17794 and 238.17666 (same lambda, composer dj5); 237/236
    // not re-checked for this step (no APK at hand). Emulator (238.17794): no paywall, the
    // membership screen shows the 12-icon picker, switching to Barcelona changed the launcher
    // alias and switching back restored it, no crashes. The staff check is not runtime-testable.
    val COMPATIBILITY_FOTMOB = Compatibility(
        name = "FotMob",
        packageName = "com.mobilefootie.wc2010",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00985F,
        targets = listOf(
            // Verified 2026-10-13 (see the comment above), including the emulator run.
            AppTarget(
                version = "238.17794.20261012",
                versionCode = 17794
            ),
            // Verified 2026-10-07 against 238.17666.20260928 (versionCode 17666, APKMirror
            // phone build): all patches apply unchanged; FotMobSmokeTest passes.
            AppTarget(
                version = "238.17666.20260928",
                versionCode = 17666
            ),
            AppTarget(
                version = "237.17536.20260911",
                versionCode = 17536
            ),
            AppTarget(
                version = "236.17398.20260827",
                versionCode = 17398
            ),
            AppTarget(
                version = "236.17338.20260822",
                versionCode = 17338
            )
        )
    )

    // Verified 2026-09-18 against com.myfitnesspal.android 26.37.0 (versionCode 51401) from
    // APKPure. 26.37.0 removed the old local `SubscriptionPreferences.getPremiumPlusEnabled`
    // gate; premium is now server-driven through queryenvoy `FeatureState` objects
    // (feature + entitlement + tier). The patch overrides the two enum-companion parsers
    // (Entitlement -> Entitled, Tier -> PremiumPlus), which is the single conversion point
    // for every feature state deserialized from the server.
    // Re-verified 2026-09-28 against 26.38.0 (versionCode 51448) from APKPure: the same
    // companion parsers still match and the patched output returns Entitled/PremiumPlus.
    // Verified 2026-10-07 against 26.39.0 (versionCode 51509, APKPure base split): all
    // patches apply unchanged; MyFitnessPalSmokeTest passes.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_MYFITNESSPAL = Compatibility(
        name = "MyFitnessPal",
        packageName = "com.myfitnesspal.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0072BC,
        targets = listOf(
            AppTarget(version = "26.37.0", versionCode = 51401),
            AppTarget(version = "26.38.0", versionCode = 51448),
            AppTarget(version = "26.39.0", versionCode = 51509),
            AppTarget(version = "26.40.0", versionCode = 51555)
        )
    )

    // Verified 2026-09-18 against club.boxbox.android 5.4.9 (versionCode 251, the current
    // APKMirror build) from APKMirror. The telemetry and interstitial patches used to call
    // returnEarly() on the first fingerprint match only - on 5.4.9 that hit an abstract
    // AppsFlyer declaration (patcher NPE) and the overridden showAd overload, leaving the
    // terminal showAd(String, String, Activity) alive. Both now patch every concrete match
    // by class scan; bytecode-verified in BoxBoxSmokeTest.
    // Verified 2026-10-07 against 5.4.16 (versionCode 260, APKPure XAPK merged, arm64 only).
    // A decompile check found two silent no-ops, present since 5.4.9: Enable Premium's
    // "block launchBillingFlow" step matched nothing (no method returns BillingResult in
    // either build) and was removed - EntitlementInfo.isActive() alone is the gate; and
    // FirebaseAnalytics has no logEvent left (R8 inlines it into the app's call sites), so
    // Disable telemetry now neuters the measurement service's
    // logEvent(String, String, Bundle, Z, Z, J) on both AppMeasurementDynamiteService and the
    // R8-renamed Binder proxy (mandatory anchor on the service). The ads patch, the AppsFlyer
    // and Crashlytics branches apply unchanged; BoxBoxSmokeTest passes on 5.4.9 and 5.4.16.
    // Runtime check 2026-10-08 (emulator, 5.4.16): app runs without crashes, but "Get Pro"
    // upsells still show and RevenueCat logs "Entitlement missing; keeping existing plan" -
    // Enable Premium is NOT confirmed working at runtime (bytecode change only).
    // Re-anchored 2026-10-13 for 5.4.17 (versionCode 261, merged universal), which replaced
    // AppLovin with Google Mobile Ads. Disable ads now forces AppConfigModel.isAdsEnabled to
    // false in its constructor (class found by its data-class toString "AppConfigModel(" +
    // ", isAdsEnabled="; Boolean from 5.4.16, primitive boolean in 5.4.9) - every placement
    // checks "plan == Free && isAdsEnabled" - plus optional GMA hooks (MobileAds init, the
    // static full-screen load with "AdRequest cannot be null.", BaseAdView.loadAd with "#008")
    // and the optional AppLovin MaxInterstitialAd no-op. Enable Premium never worked for
    // never-subscribed accounts: the app reads entitlements["Pro_Android"], which is null (not
    // inactive) for them, so forcing EntitlementInfo.isActive() left the plan on Free. It now
    // forces the plan enum (enum <clinit> "Free"/"Pro") to Pro where PlanDataStore (<clinit>
    // "plans_data_store"/"plan_type") decodes the cached ordinal via entries.get(I) (all three
    // versions), and in 5.4.17's static plan-returning store methods (live RevenueCat check and
    // cache read). BoxBoxSmokeTest passes on 5.4.9 (APKMirror bundle, re-downloaded),
    // 5.4.16 and 5.4.17; CLI-patched and decompile-checked on 5.4.16 and 5.4.17. Emulator
    // (5.4.17, logged out): the "AD" promo card and the Standings ad slot are gone with no
    // gap and AdMob never initializes; "Get Pro" and the paywall are gone, a Pro-locked widget
    // shows "Add" instead of "Unlock" and the app logs plan Pro; no crashes. The "PRO" labels
    // in the widget list stay. Signed-in features were not tried; server-side
    // UserProfiles.isPro is untouched.
    val COMPATIBILITY_BOXBOX = Compatibility(
        name = "BoxBox",
        packageName = "club.boxbox.android",
        apkFileType = ApkFileType.APKM, // bundle-only on APKMirror and APKPure (checked 2026-10-07)
        appIconColor = 0xFF0000,
        targets = listOf(
            AppTarget(version = "5.4.9", versionCode = 251),
            AppTarget(version = "5.4.16", versionCode = 260),
            AppTarget(version = "5.4.17", versionCode = 261)
        )
    )

    // Verified 2026-08-19 against 365scores.apkm v14.8.8 (universal, Android 7.0+).
    // App uses Google Mobile Ads (AdMob) loaded via the Blaze GAM SDK wrapper.
    // MobileAds.initialize(Landroid/content/Context;)V and the (Context, Listener)
    // overload are both present unobfuscated in com.google.android.gms.ads.MobileAds.
    // Ad SDKs observed: Google Mobile Ads, Unity, InMobi, Vungle, Mintegral,
    // ByteDance Pangle, Meta Audience Network (all GAM-mediated).
    // Re-verified 2026-09-17 on 14.9.4 with morphe-cli -f: Disable ads still applies.
    // Re-verified 2026-09-28 on 14.9.5 (versionCode 1495, APKPure): both initialize
    // overloads are empty in the decompiled output.
    // Verified 2026-10-07 against 14.9.6 (versionCode 1496, APKPure XAPK merged): Disable
    // ads applies unchanged; Scores365SmokeTest passes.
    // 2026-10-12, 14.9.6 (still the latest on APKMirror and APKPure): Disable ads also forces
    // every concrete showAdsForContext()/isBannerNeedToBeShown() app ad gate to false. Added
    // "Enable Premium" (EntitlementState constructor forces adsFree/tipster/plus/
    // delayedNotifications/notificationSounds true and its feature getters return true, since
    // the startup cache is read with Gson and skips the constructor; entitlements come from the
    // 365 purchase gateway and are only mapped locally; the Remove ads screen reads its grace
    // start key "iabc_fnrts" under another name, because the forced state starts a grace period
    // that would make it show the purchase screen), "Disable update prompts" (BaseActivity and
    // BaseActionBarActivity onCreate no longer find the ForceUpdateDialog/NewVersionPopup
    // extras) and "GmsCore support" (shared builder, off by default; social login uses
    // play-services-auth GoogleSignIn, push uses FCM; signing cert SHA-1 47f994bd...f1a1 read
    // from the APKMirror bundle's v2/v3 block; launch activity com.scores365.ui.Splash; pulls in
    // "Spoof signature", which routes every getPackageInfo call through the signature extension
    // so Firebase's X-Android-Cert carries the original certificate and its API key stops
    // answering 403 API_KEY_ANDROID_APP_BLOCKED). Ideas
    // from github.com/0xtalos/365score-patches; its FirebaseAnalytics.logEvent hook has no
    // target in 14.9.6 (R8 removed the method), so no telemetry patch here. 14.9.4 and 14.9.5
    // were dropped from the targets: the new patches were only verified on 14.9.6. The GmsCore
    // "Clone" option renames the package, which Spoof signature does not follow (it spoofs
    // com.scores365 only), so keep Clone off when notifications matter.
    val COMPATIBILITY_365SCORES = Compatibility(
        name = "365Scores",
        packageName = "com.scores365",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFFC107,
        targets = listOf(
            AppTarget(version = "14.9.6", versionCode = 1496)
        )
    )

    // Verified 2026-08-20 against livescore.apk v9.9.1 (universal, Android 7.0+).
    // AdsRemovalSettings.areAdsDisabled()Z is the single ad gate checked by
    // BannerManagerImpl, InterstitialAdsUseCase, MpuAdsConfig, HeroPlacementUseCase,
    // CoverageSponsorshipSettings and AnnouncementBannerUseCase. Class and method
    // are un-obfuscated in com.livescore.ads.config.
    // Re-verified 2026-09-17 on 10.1 with morphe-cli -f: Disable ads still applies.
    // Verified 2026-10-07 against 10.2.1 (versionCode 2188, APKPure): Disable ads applies
    // unchanged; LivescoreSmokeTest passes.
    val COMPATIBILITY_LIVESCORE = Compatibility(
        name = "Livescore",
        packageName = "com.livescore",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xC8102E,
        targets = listOf(
            AppTarget(version = "10.1", versionCode = 2144),
            AppTarget(version = "10.2.1", versionCode = 2188),
            AppTarget(version = "10.3", versionCode = 2204)
        )
    )

    // Verified 2026-08-20 against FishBuddy v11.0.84 (universal, Android 7.0+).
    // Premium gate is User.hasPremium()Z (no.fishbuddy.app.data.models.User),
    // reads the server-populated `roles` HashMap for the "premium" entitlement.
    // Subscription managed via RevenueCat (no ad SDKs). Class + method names
    // un-obfuscated.
    // Re-verified 2026-09-17 on 11.0.101 with morphe-cli -f: Enable Premium still applies.
    // Verified 2026-10-07 against 11.0.125 (versionCode 110125, APKPure): Enable Premium
    // applies unchanged; FishBuddySmokeTest passes.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_FISHBUDDY = Compatibility(
        name = "FishBuddy",
        packageName = "no.fishbuddy_playground.app",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0288D1,
        targets = listOf(
            AppTarget(version = "11.0.101", versionCode = 110101),
            AppTarget(version = "11.0.125", versionCode = 110125)
        )
    )

    // Verified 2026-08-20 against ru.iptvremote.android.iptv apkm v9.1.25
    // (universal, Android 12L+). Free app package; Pro is a separate paid app
    // (ru.iptvremote.android.iptv.pro) with its own build.
    // Re-anchored 2026-10-07 on the same 9.1.25 build (still APKPure latest) for issue #35,
    // where both patches were found to be ineffective:
    //   - Disable ads: killing WortiseSdk.initialize left Yandex banners/instream running.
    //     Ads go through an in-app mediation provider (b4, located by its
    //     "instream_preload_lead_sec" remote-config getter); every override was delegated
    //     to the app's built-in no-ads sibling provider (i5), found structurally (narrowed
    //     on 2026-10-09, see below).
    //   - Enable Premium: IptvFreeApplication.k()Z was never a Pro/trial gate (it is a
    //     20-minute ad-closed cooldown that only suppresses the review prompt). Pro features
    //     are XML stub preferences linking to the Pro listing; the access-control (parental
    //     PIN) feature ships in the free app and is unlocked by swapping its stubs for the
    //     real preference classes. Start on boot / autoplay last channel have no code in the
    //     free build and stay locked.
    //     The free build's main-settings Playlists entry has no key, so "Lock playlist
    //     settings" (a PIN locker attached to "screen_playlists") was never enforced; the
    //     resource patch adds that key.
    // IptvSmokeTest asserts the delegation and the rewritten XML; the CLI-patched APK was
    // decompiled to confirm both. Runtime-tested 2026-10-07 on an Android 35 emulator (full
    // APKMirror bundle, merged): app starts, PIN can be set, the playlist lock prompts for it
    // and the correct PIN lets the user through (the app needs a second tap afterwards, which
    // is its own behaviour for intent-based entries). That runtime pass covered Enable Premium
    // only.
    // 2026-10-09 (same 9.1.25 build, issue #35 follow-up): the 2026-10-07 runtime claim did not
    // hold for Disable ads. With it applied, the app crashed on launch: the banner fragment's
    // onAttach asked the provider for a placement view, which was delegated to i5.a, and i5.a
    // wraps Collections.EMPTY_LIST in a composite (bv) whose constructor throws when the list is
    // empty. The placement factory (the only (Context, placement) override) is no longer
    // delegated. Its callers are cut off instead: the provider's static "banners allowed"
    // check (!isTv, its only static (Context)Z) returns false, and the ad consent gate (found
    // by its "AdConsentGate" / "Deferred consent action..." log in the replay method) drops
    // deferred ad actions, which stops the idle placement preloader, interstitial loads and
    // instream preload. The other overrides still delegate to i5; each caller handles the
    // value it returns. IptvSmokeTest asserts all of this. Runtime re-test pending.
    val COMPATIBILITY_IPTVREMOTE = Compatibility(
        name = "IPTV",
        packageName = "ru.iptvremote.android.iptv",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x1976D2,
        targets = listOf(AppTarget(version = "9.1.25", versionCode = 5010))
    )

    // Verified 2026-08-19 against brave.apkm v1.93.136, re-anchored 2026-09-18 against
    // v1.95.104 (versionCode 429510404, the current APKMirror build). All Brave Origin
    // anchor strings and non-obfuscated classes (BraveOriginPreferences,
    // BraveOriginSettingsLauncherHelper, profiles/Profile) are present. Obfuscated class
    // names rotate between versions, but every fingerprint is string/shape-based so it
    // resolves at patch time. 1.95.104 swapped the parameter order of the package/product
    // pref writer from (Profile, String) to (String, Profile), which broke the fingerprint
    // pinning v1.93.136; the patch matches the new order and is bytecode-verified in
    // BraveSmokeTest.
    val COMPATIBILITY_BRAVE = Compatibility(
        name = "Brave Browser",
        packageName = "com.brave.browser",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF4500,
        targets = listOf(
            AppTarget(version = "1.95.104"),
            // Verified 2026-10-07 against com.brave.browser 1.96.61 (versionCode 429606104,
            // APKMirror bundle merged with APKEditor). Two Origin log messages were reworded
            // ("getIsSubscriptionActive prefs are unavailable", "requestCredentialSummary
            // profile is null or destroyed"), which broke the full-string anchors; both now
            // match by prefix. All other anchors (pref keys, policy strings, Origin
            // preferences shape) were unchanged. BraveSmokeTest passes on 1.95.104 and 1.96.61.
            // Also 2026-10-07: the Origin "Privacy preserving analytics" and "Statistics
            // reporting" switches only wrote a SharedPreferences key nothing read (P3A and the
            // usage ping are native). The listener now also writes brave.p3a.enabled /
            // brave.stats.reporting_enabled through LocalStatePrefs + PrefService (real class
            // names, shape-matched methods). CLI-applied and decompile-checked on 1.95.104 and
            // 1.96.61; runtime-checked on 1.96.61 (switch flips the native pref).
            AppTarget(version = "1.96.61"),
            // Verified 2026-10-07 against 1.97.56 (versionCode 429705604, GitHub release
            // Bravearm64Universal.apk): all patches apply unchanged; BraveSmokeTest passes.
            AppTarget(version = "1.97.56"),
        )
    )

    // Same app, used only by the experimental Brave patches (Disable telemetry, Disable ads,
    // Hide promotional prompts), so Morphe Manager flags them as experimental. Researched
    // 2026-10-07 against 1.96.61 (versionCode 429606104): smoke-tested, CLI-applied and
    // decompile-checked, and runtime-checked on the emulator with the x86_64 1.96.61 build
    // (no crashes; P3A/usage ping off, crash consent false, no promoCode, normal NTP). Anchors
    // are unobfuscated (JNI/manifest-kept) class names, pref keys and method shape. Not yet
    // field-tested by users.
    val COMPATIBILITY_BRAVE_EXPERIMENTAL = Compatibility(
        name = "Brave Browser",
        packageName = "com.brave.browser",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF4500,
        targets = listOf(
            AppTarget(
                version = "1.96.61",
                isExperimental = true,
            ),
            // Verified 2026-10-07 against 1.97.56 (versionCode 429705604, GitHub release
            // Bravearm64Universal.apk): the experimental patches apply unchanged; BraveSmokeTest
            // passes. Not runtime-tested on this version.
            AppTarget(
                version = "1.97.56",
                isExperimental = true,
            ),
        )
    )

    // Verified 2026-08-27 against librepods_1.0.0-rc1-play-63 .apkm from APKMirror
    // (arm64-v8a + x86, Android 16+ / minSdk 36). App logic is R8-obfuscated into
    // top-level classes (e.g. PlayBillingProvider -> uo) around native protocol
    // libs (libbluetooth_socket.so, libl2c_fcr_hook.so). Premium gates all read
    // PurchaseUiState.isPremium which is fed solely by PlayBillingProvider's
    // _isPremium StateFlow written in processPurchases(); a FOSS variant of the
    // provider also ships in this build. In-app product:
    // "librepods.advanced_features.v2". No license server (upstream GPL-3.0,
    // github.com/librepods-org/librepods).
    // 2026-10-13, 1.0.1-rc1-play (versionCode 64): premium logic unchanged; the CLI applies
    // Enable Premium without -f and the patched processPurchases() (uo.e(List)) loads true
    // before Boolean.valueOf feeds the premium state on every path, including the empty list.
    val COMPATIBILITY_LIBREPODS = Compatibility(
        name = "LibrePods",
        packageName = "me.kavishdevar.librepods",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x406C5C,
        targets = listOf(
            AppTarget(
                version = "1.0.0-rc1-play",
                versionCode = 63
            ),
            AppTarget(
                version = "1.0.1-rc1-play",
                versionCode = 64
            )
        )
    )

    // Verified 2026-08-30 against easynotes..._1.3.59.0819-10565 .apkm from APKMirror
    // (arm64-v8a + armeabi-v7a, Android 6.0+ / minSdk 23). App code is NOT obfuscated:
    // 3,589 readable classes under notes.easy.android.mynotes.
    //
    // Entitlement is entirely local — there is no license server. Everything routes through
    // three public static booleans on notes.easy.android.mynotes.App:
    //   isVip()Z       38 call sites - master gate for premium assets (fonts, backgrounds,
    //                  stickers, drawing tools). Per-asset flags such as DrawPatternRes.isVip()
    //                  are only consulted *inside* an `if (!App.isVip())` check.
    //   isAdFree()Z    4 call sites  - gates banner loading in utils/AdManager
    //                  (e.g. the "notes_edit_banner" unit) and App$AppOpenAdManager.
    //   isGoogleVip()Z drives the "Google VIP" badge only.
    //
    // Both isVip() and isGoogleVip() reduce to
    //   userConfig.getHasBuyed() || userConfig.getHasSubscribe()
    // with isVip() additionally early-outing on getBillingMonthlyTestOpen() (false) and
    // isReferralVip() (true). State lives in SharedPreferences via constant/UserConfig
    // (keys has_buyed, has_subscribe, has_m_subscribe, has_y_subscribe).
    //
    // Important: billing/BillingManager runs queryPurchaseState() on startup and its
    // callbacks (BillingManager$5/$7/$8) write those prefs back from whatever Play reports,
    // so a non-payer gets them reset to false. That is why we patch the *consumers*
    // (App.isVip/isAdFree) rather than the writers — forced return values ignore the reset.
    //
    // SKUs: BILLING_MONTH / BILLING_YEAR / BILLING_LIFETIME / BILLING_MONTH_TO_YEAR /
    // BILLING_UP_TO_LIFETIME (analytics suffixes "1m", "1y", "lifetime").
    // Ad SDKs: Google Mobile Ads (app-open), Meta Audience Network, PubMatic.
    // Re-verified 2026-09-17 on 1.3.61.0907 with morphe-cli -f: both patches still apply.
    val COMPATIBILITY_EASYNOTES = Compatibility(
        name = "EasyNotes",
        packageName = "easynotes.notes.notepad.notebook.privatenotes.note",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00ACFF,
        targets = listOf(
            AppTarget(
                version = "1.3.61.0907",
                versionCode = 10567
            ),
            // Verified 2026-10-07 against 1.3.63.0921 (versionCode 10569, APKPure XAPK
            // merged): both patches apply unchanged; EasyNotesSmokeTest passes.
            AppTarget(
                version = "1.3.63.0921",
                versionCode = 10569
            )
        )
    )

    // Verified 2026-09-17 against eu.faircode.email 1.2337 (versionCode 2337) from APKPure.
    // FairEmail is FOSS and not obfuscated: the pro entitlement is a single SharedPreferences
    // boolean ("pro") read through ActivityBilling.isPro(Context). The billing client rewrites
    // the pref from Play on launch, so the patch forces the getter instead of the pref.
    // Note: Gmail OAuth cannot work on a re-signed build. FairEmail gets Gmail tokens through
    // AccountManager and Google only issues them to packages signed with the certificates
    // registered for the OAuth client (the Play/GitHub builds). For Gmail accounts use an app
    // password instead (Gmail requires 2FA for that); non-Google accounts are unaffected.
    // Verified 2026-10-07 against 1.2338 (versionCode 2338, APKPure): all patches apply
    // unchanged; FairEmailSmokeTest passes.
    val COMPATIBILITY_FAIREMAIL = Compatibility(
        name = "FairEmail",
        packageName = "eu.faircode.email",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2196F3,
        targets = listOf(
            AppTarget(version = "1.2337", versionCode = 2337),
            AppTarget(version = "1.2338", versionCode = 2338)
        )
    )

    // Verified 2026-09-17 against net.dinglisch.android.taskerm 6.6.18 (versionCode 5443)
    // from APKPure. Paid app: the Play purchase is validated with Google Play LVL plus a
    // licence server (Patreon keys). The licensing code is R8-obfuscated (rf.a0, rf.w$a);
    // the 2026-09-17 fingerprints pinned those names.
    // 2026-10-13: fingerprints no longer pin obfuscated names. Anchors: the LVL response
    // mapper's <init>(I) with the "Licensed"/"Retrying"/"Unlicensed" labels (its no-arg
    // LicenseStatus getters return Licensed); the (Context, long)Z cached check by its
    // "Checking cached only" log prefix; in that class the public (Context)Z overload and the
    // private static (?, boolean, Context)Z result helper reading LicenseStatus.Retry/Licensed
    // (new hook: covers the Unlicensed fallback when Play cannot be reached); and the public
    // static (Context)Z facade that calls the overload. Smoke test and CLI disassembly pass on
    // 6.6.18 (5443) and 6.6.20 (5445). Emulator 2026-10-14 (6.6.20, Play Store image, no Google
    // account, so LVL answers with a server error): stock shows "Trial Over - Tasker can't be
    // used until you purchase it"; patched shows no licence dialog or notification and runs a
    // Flash task; no crashes. A real NOT_LICENSED response and the timeout path were not reachable.
    val COMPATIBILITY_TASKER = Compatibility(
        name = "Tasker",
        packageName = "net.dinglisch.android.taskerm",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x4CAF50,
        targets = listOf(AppTarget(version = "6.6.20", versionCode = 5445))
    )

    // Verified 2026-09-17 against org.kman.AquaMail 2.7.0 (versionCode 200700061) from
    // APKPure. LicenseManager computes a licence level (0 free / 10-20 Pro / 30 migration /
    // 40 Pro+ subscription) and LockFeatures answers the per-feature locks; both classes and
    // methods are unobfuscated. The UI reads getLicenseLevel() directly, so the patch forces
    // the level to 40 as well as the derived booleans.
    // Issue #16 (reported against bundle 1.3.1, the free version still displayed): the
    // account list, the prefs license line and the account-limit logic all gate on
    // getLicenseData() != null, which is null on a free install, so the forced getters
    // were never consulted past that gate. getLicenseData() now returns a licensed
    // snapshot instead (state licensed, confirm deadline + expiry far future; the
    // snapshot's R8-renamed fields are discovered structurally from the licensed checks
    // themselves). Re-verified against the same APK on 2026-10-06 - AquaMailSmokeTest
    // asserts the snapshot prefix in the emitted bytecode.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_AQUAMAIL = Compatibility(
        name = "Aqua Mail",
        packageName = "org.kman.AquaMail",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00B0FF,
        targets = listOf(AppTarget(version = "2.7.0", versionCode = 200700061))
    )

    // Verified 2026-09-17 against com.wunderground.android.weather 6.20.1
    // (versionCode 2019070035) from APKPure. The ad-free purchase is evaluated locally by the
    // Adobe Airlock SDK ("ads.Ad Free" entitlement); the getters in AirlockValueUtil,
    // PremiumHelper and WUApplication are unobfuscated. FeatureManager.initAdManagers() reads the
    // raw Airlock feature and starts the ad SDKs, so it is also neutered (2026-10-10, issue #45:
    // ads came back after a few launches when only the getters were forced).
    val COMPATIBILITY_WUNDERGROUND = Compatibility(
        name = "Weather Underground",
        packageName = "com.wunderground.android.weather",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "6.20.1", versionCode = 2019070035))
    )

    // Verified 2026-09-17 against com.monefy.app.lite 1.22.11 (versionCode 2229) from APKPure.
    // The app code is R8-obfuscated but the entitlement helpers are readable:
    // ClearCashApplication.o() is the master pro check (pro-app package id or cached IAP flag),
    // GeneralSettingsProvider.F() reads the cached "MONEFY_PRO_IN_APP_BOUGHT" pref that the
    // RevenueCat callback writes, and zt.p() reports the subscription-expired state.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_MONEFY = Compatibility(
        name = "Monefy",
        packageName = "com.monefy.app.lite",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x8BC34A,
        targets = listOf(AppTarget(version = "1.22.11", versionCode = 2229))
    )

    // Verified 2026-09-18 against com.rammigsoftware.bluecoins 13.1.79 (versionCode 33145)
    // from APKMirror. The app is not obfuscated: every premium check collects
    // BillingDomain.isPremiumVersionFlow(), whose only implementation is BillingDomainManager.
    // The manager delegates to an encrypted "premiumKey" preference; the patch returns a
    // constant flow of true instead, so all screens see premium without touching the
    // encrypted storage or the billing client.
    // Re-anchored 2026-10-07 for 13.1.149 (versionCode 33215): this release is fully
    // R8-obfuscated - BillingDomainManager and every kotlinx.coroutines.flow name are gone, so
    // the flowOf(TRUE) replacement no longer applies. The patch now forces the value inside
    // the premium pipeline instead, anchored only on Timber log prefixes + method shape:
    // (1) the salted "premiumKey" verifier's downstream emit (log "Startup: Encryption:
    // Premium is ") is fed Boolean.TRUE - this is the final stage in 13.1.79; (2) in 13.1.149
    // the use case (strings "premiumKey" + "versionOverride") combines that with a Google Play
    // version override that wins when set, so the combine lambda (log "Startup: Encryption:
    // Google Play premium override") returns TRUE; step 2 is mandatory whenever that use case
    // exists. BluecoinsSmokeTest passes on both 13.1.79 and 13.1.149.
    val COMPATIBILITY_BLUECOINS = Compatibility(
        name = "Bluecoins",
        packageName = "com.rammigsoftware.bluecoins",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2979FF,
        targets = listOf(
            AppTarget(version = "13.1.79", versionCode = 33145),
            AppTarget(version = "13.1.149", versionCode = 33215),
        )
    )

    // Verified 2026-09-18 against Flashscore 26.9.2 (versionCode 517) supplied from
    // apks/flashscore. The app is obfuscated, but ads all go through Google's next-generation
    // Mobile Ads SDK (com.google.android.libraries.ads.mobile.sdk), whose class and method
    // names are stable library API; the "Disable ads" patch hooks that surface. There is no
    // Play Billing client at all - the subscription is validated server-side
    // (/api/v2/android/validate_subscription_v2), so premium cannot be unlocked client-side.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    // Verified 2026-10-13 against 26.10.2 (versionCode 520, owner-supplied apkcube .apks; same
    // CN=Pavel Cvetler / LiveSport signer as 26.9.2, v3 verifies on every split): Disable ads
    // applies unchanged and FlashscoreSmokeTest passes on its base APK.
    // Verified 2026-10-13 on both 26.9.2 (517) and 26.10.2 (520): FlashscoreSmokeTest applies all
    // five patches and asserts every hook. Anchors are kept names, stable strings and shape only:
    //   - Disable ads, extended: the kept AdvertZone's load method ("Unsupported ad provider: ")
    //     goes GONE and calls the (I)V setMinHeight helper (bookmaker WebView banners too); the
    //     video media item (toString ", vmapResponse=", ctor (ArrayList, MediaProvider$DrmData,
    //     String)) gets a null VMAP, so IMA video ads never load.
    //   - Disable telemetry: AppsFlyerLib subclass start/logEvent/logSession/logAdRevenue;
    //     Crashlytics via "firebase_crashlytics_collection_enabled" (app wrapper + arbiter) and
    //     the wrapper ("FirebaseCrashlytics component is not present.") enable method; Apptentive
    //     register/engage; manifest switches for Firebase Analytics/Performance, ad ID and
    //     Facebook auto-logging (the Facebook SDK stays initialised for Facebook Login).
    //   - Disable rating prompts: the settings getBool (" using getBool!") returns false for
    //     CAN_SHOW_RATING_BANNER / CAN_SHOW_INAPP_RATING.
    //   - Block marketing popups: remote dialog (ctor "dialogSupport", show "btnGoUrl" over
    //     EventListActivity), FirebaseInAppMessagingDisplay.displayMessage, InAppStrip parser.
    //   - Disable update prompts: the SOFT/HARD dialog ("unssupported_version_reporter" class,
    //     "last_notify_" + Dialog.show()).
    // Push is untouched: no Firebase Messaging/Installations, notification or OneTrust code is
    // patched, and the test checks the MESSAGING_EVENT service against the stock APK.
    // CLI decompile (no -f) shows exactly 45 changed classes, none push-related. On the emulator
    // (26.10.2) the push token registers with Livesport's token service as in stock, following a
    // match and the per-match/global notification settings work, and list ads are gone. A
    // "NotificationsSettingsSportRepository 401" before token registration also occurs on stock.
    // 2026-10-13: "Hide betting content" (default off) added and FlashscoreSmokeTest passes on
    // 26.10.2. The app's own paid "Hide betting content" setting is dead code
    // (its HIDE_BETTING_CONTENT / HAS_ACTIVE_SUBSCRIPTION prefs are never written, the toggle has
    // no click handler, and its predicate only hides list odds), so the patch drives the
    // remote-config odds kill switch the app already uses for geo blocks and underage users:
    // the config value class (ctor "USER_PREFS_CONFIG_KEY-") routes get() through a new static
    // helper that answers ODDS_ENABLE, the DETAIL_ODDS_* switches and
    // DETAIL_SUMMARY_GAMBLING_TEXT_ENABLED with false, DETAIL_BROADCASTING_BOOKMAKERS_DISABLE and
    // DETAIL_ODDS_SUMMARY_IMAGES_DISABLED with true, BONUS_PROMO_GIFT_ICON_URL with "" and
    // LEAGUE_DETAIL_HEADER_STRIP_SPORTS_ENABLED with an empty list. The keys are the key enum's
    // constants (the ctor's 2nd parameter type), resolved by name from its <clinit>; all 15 exist
    // in both versions. The sponsored strip DTO (toString "SponsoredStripWidgetDTO(sponsoredItems=")
    // and the bonus strip DTO (toString prefix "BonusStripWidgetDTO(": "(bonuses=" in 26.9.2,
    // "(summaryBonuses=" in 26.10.2) get empty lists in their constructors. Bookmaker
    // AdvertZone banners stay with Disable ads; the age-verification flow itself is not touched
    // (only the league summary params' copy of isAgeVerified is forced false, see below).
    // 2026-10-13, extended after the emulator still showed the league ODDS tab and the "Winner
    // Odds" (outright) block: neither is behind a remote-config key. The league tab bar comes from
    // the tab set the API sends, so the league header model (toString
    // "LeagueDetailHeaderModel(tournamentId=" + ", detailTabs=") routes its constructor's Set
    // through a new helper that drops the tab-id enum constant named ODDS. The summary only
    // creates the outright widget when its params' isAgeVerified is true, so the params class
    // (toString "LeagueDetailSummaryScreenParams(tournamentId=" + ", isAgeVerified=") stores
    // false there and the widget stays in its Hidden state (renders nothing). FlashscoreSmokeTest
    // passes on 26.10.2; the CLI-patched 26.10.2 disassembly shows both hooks.
    // 2026-10-13: the 26.9.2 target was dropped at the owner's request; only 26.10.2 is supported.
    // 2026-10-13 emulator run (26.10.2 full-bundle merge, all six patches, stock control): the
    // league ODDS tab, the match-detail odds section, the bookmaker bonus strip with its 18+ line,
    // the gift icon and the ad slots are gone; no crashes. List-row odds and bookmaker broadcasting
    // info were not shown by stock in that region, so they are untested.
    val COMPATIBILITY_FLASHSCORE = Compatibility(
        name = "Flashscore",
        packageName = "eu.livesport.FlashScore_com",
        // The owner gets Flashscore from apkcube.com, which serves .apks split archives. The
        // bundle cannot choose Manager's download site (Morphe's API picks it); the preferred
        // format and the per-version note point users there instead. APKM still works.
        apkFileType = ApkFileType.APKS,
        appIconColor = 0x1E88E5,
        targets = listOf(
            AppTarget(version = "26.10.2", versionCode = 520, description = FLASHSCORE_SOURCE_NOTE),
        )
    )

    // Verified 2026-09-18 against OneFootball 15.142.0 (versionCode 1044958885) from APKMirror.
    // The app code is obfuscated; all ads run through Google Mobile Ads
    // (com.google.android.gms.ads), so the "Disable ads" patch hooks that stable library
    // surface (initialize, every load/loadAd and the app-open preloader). No premium tier
    // was found in the readable code - the patch targets ads only.
    // Verified 2026-10-07 against 15.145.0 (versionCode 1044999406, APKMirror): Disable ads
    // applies unchanged; OneFootballSmokeTest passes.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_ONEFOOTBALL = Compatibility(
        name = "OneFootball",
        packageName = "de.motain.iliga",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00C853,
        targets = listOf(
            AppTarget(version = "15.142.0", versionCode = 1044958885),
            AppTarget(version = "15.145.0", versionCode = 1044999406)
        )
    )

    // Verified 2026-09-18 against net.osmand 5.4.5 (versionCode 5405) from APKMirror.
    // Not obfuscated: InAppPurchaseHelper.isPurchased(String) walks the purchase list and
    // InAppPurchaseHelperImpl answers the cached local entitlement getters. applyPurchases()
    // derives the OSMAND_PRO_PURCHASED / OSMAND_MAPS_PURCHASED / LIVE_UPDATES_PURCHASED
    // settings from those same getters, so forcing them also stops the writer resetting them.
    // Verified 2026-10-07 against 5.4.9 (versionCode 5409, APKPure XAPK merged): all patches
    // apply unchanged; OsmAndSmokeTest passes.
    val COMPATIBILITY_OSMAND = Compatibility(
        name = "OsmAnd",
        packageName = "net.osmand",
        apkFileType = ApkFileType.APKM, // bundle-only on APKMirror and APKPure (checked 2026-10-07)
        appIconColor = 0x7CB342,
        targets = listOf(
            AppTarget(version = "5.4.5", versionCode = 5405),
            AppTarget(version = "5.4.9", versionCode = 5409),
            AppTarget(version = "5.4.10", versionCode = 5410)
        )
    )

    // Verified 2026-09-19 against com.textra 4.85 (versionCode 48561) from APKMirror.
    // Textra app code is R8-obfuscated. Purchase state is a single integer preference
    // (key "lc": -1 unknown, 0 free, 1 licensed) owned by a dedicated obfuscated class.
    // That class is the only one in the whole app declaring all four methods
    // g()Ljava/lang/Integer;, i(Ljava/lang/Integer;)V, k()Z and l()Z, so the patch finds
    // it structurally (no obfuscated names) and forces l() -> true (licensed) and
    // k() -> false (state 1). Every gate reads those: ad placement, settings visibility
    // and upgrade prompts.
    // Verified 2026-10-07 against 4.87 (versionCode 48761, APKPure XAPK merged): all patches
    // apply unchanged; TextraSmokeTest passes.
    val COMPATIBILITY_TEXTRA = Compatibility(
        name = "Textra",
        packageName = "com.textra",
        apkFileType = ApkFileType.APKM, // bundle-only on APKMirror and APKPure (checked 2026-10-07)
        appIconColor = 0x5C6BC0,
        targets = listOf(
            AppTarget(version = "4.85", versionCode = 48561),
            AppTarget(version = "4.87", versionCode = 48761)
        )
    )

    // Verified 2026-09-19 against com.jetappfactory.jetaudio 13.1.2 (versionCode 261320)
    // from APKMirror. Purchase state lives in one obfuscated class (default-package "uy",
    // compiled from JInAppInfo.java) holding per-plugin static booleans. The class is
    // located by its unique inline literal (IAB: QueryInventory info: ...) instead of its rotating
    // name. Leaf boolean getters (no args or a Context) are forced true; the compound
    // getters and the inverted upsell check OR those leaves and flip by themselves.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_JETAUDIO = Compatibility(
        name = "jetAudio",
        packageName = "com.jetappfactory.jetaudio",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF6D00,
        targets = listOf(AppTarget(version = "13.1.2", versionCode = 261320))
    )

// Verified 2026-09-19 against com.Relmtech.Remote 3.25.1 (versionCode 325001) from
// APKMirror. The app code is R8-obfuscated (`bh0` here); "Full" is a
// `License.Status` SharedPreferences int (0 = Free, 1 = Locked, 2 = Full) and the
// canonical Full check is the single `Z(Context)` method that reads it through the
// same-class `I(Context)` getter - called from ~20 feature sites. The patch locates
// that class by the unique "License.Status" literal and forces the check true.
// (RevenueCat only feeds the paywall UI for actual buyers - non-buyers get an empty
// entitlements map, so an `isActive()` force can never unlock anything here.)
    val COMPATIBILITY_UNIFIEDREMOTE = Compatibility(
        name = "Unified Remote",
        packageName = "com.Relmtech.Remote",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2962FF,
        targets = listOf(AppTarget(version = "3.25.1", versionCode = 325001))
    )

    // Verified 2026-09-27 against com.futbin 27.02 (versionCode 7002, user-supplied
    // APKPure APK). The app code is obfuscated, but all ads run through Google Mobile
    // Ads (the app's own `x.cv3` ad manager only calls the GMA surface, and the Nimbus
    // mediation SDK renders AdMob through it), so the "Disable ads" patch hooks that
    // stable library surface (initialize, every load/loadAd and the app-open
    // preloader). Futbin+ premium is account-linked (Play purchase is linked to the
    // Futbin account server-side), so premium is out of reach - ads only.
    // 2026-10-13: the "27.5" (versionCode 7005) target added 2026-10-07 was REMOVED. That APK
    // (Aptoide user store "superpocket") is a rebuilt repack, not a Futbin release: signed by
    // CN=Jx Clarynx (cert SHA-256 bd6515d3...fc6b) instead of FUTBIN (1cf7ff75...e904), with
    // an injected classes7.dex whose exported launcher activity fetches remote JSON, collects
    // device/carrier info and shows WebView overlays (adware). Genuine releases are numbered
    // 27.05, 27.06; check the FUTBIN signer before using any Futbin APK.
    // 2026-10-14 re-verified on genuine builds (signer O=FUTBIN, 1cf7ff75...e904): 27.02 (7002),
    // 27.05 (7005), 27.06 (7006). The "only GMA" claim above was wrong. Nimbus also renders through
    // its own Moloco/Mintegral/Vungle demand: the GAM controller falls back to a Nimbus interstitial
    // when it has no GAM one. The squad builder shows an Anzu video panel that only premium hides.
    // GMA-only hooks also left in-list ad slots visible (empty). "Disable ads" now forces the app's
    // isAdAllowed preference ("preferences.ad.allowed.key") to false, so every placement hides
    // itself and the SDKs are never started. It also no-ops, by log-line prefix (R8 names rotate:
    // x.cv3 -> x.nk4), the FbApplication GAM/Nimbus init ("Ad Log -> int GAM"), the GAM
    // interstitial show, Nimbus init and its interstitial/bottom/in-feed requests. It forces the
    // premium branch of the Anzu init ("Ad Log -> init Anzu") and keeps the GMA hooks. Every hook is
    // mandatory. The app calls no MobileAds static besides initialize. Smoke test and CLI
    // disassembly pass on all three. Emulator 2026-10-14 (27.06, 2 GB and 4 GB AVDs): the patched
    // app starts and stays up with no crash; the stock app logs "Initialized AdMob" and the patched
    // app starts no ad SDK. Neither stock nor patched gets past the first GlobalActivity screen on
    // this emulator (pause timeout on stock too), so the screens with ads, the squad-builder panel
    // and interstitials were NOT checked at runtime. Only 27.06 is supported now (owner request).
    val COMPATIBILITY_FUTBIN = Compatibility(
        name = "Futbin",
        packageName = "com.futbin",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x26A69A,
        targets = listOf(AppTarget(version = "27.06", versionCode = 7006))
    )

    // Verified 2026-09-27 against com.pocketcolorwheel.PCW 3.26 (versionCode 57,
    // user-supplied APKPure APK). The free app has no premium gate: no billing client,
    // no license checks and no references to the separate Pro package - it monetizes
    // purely through Google Mobile Ads initialized from `ApplicationClass.onCreate`.
    // The "Disable ads" patch hooks that stable library surface (initialize, every
    // load/loadAd and the app-open preloader). The Pro app
    // (com.pocketcolorwheelpro.pro) is a standalone paid listing that mirrors do not
    // serve, so there is nothing to verify against it.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_PCW = Compatibility(
        name = "Pocket Color Wheel",
        packageName = "com.pocketcolorwheel.PCW",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x9C27B0,
        targets = listOf(AppTarget(version = "3.26", versionCode = 57))
    )

    // YouCut (com.camerasideas.trimmer) removed 2026-10-09 (issue #44): see
    // docs/patch-blocklist.md. The Pro gate is client-side and forceable, but every
    // re-signed build self-kills via a native signing-cert + anti-debug SIGKILL gate in
    // libisvideoengine.so JNI_OnLoad, which a bytecode/resource patch cannot neutralise.
    // Revisit only if the engine gains native-lib patching.

    // Verified 2026-10-06 against com.flyersoft.moonreader 10.7 (versionCode 1007000,
    // universal APK from APKPure). All ads run through the app's own unobfuscated ad
    // manager com.flyersoft.components.MrAd, whose private static no-arg boolean
    // disableAds() gates every ad path: the MrAd constructor returns before
    // initializing the ad SDK (AdMob + Facebook Audience Network) when it is true,
    // and the interstitial/exit/rewarded show paths consult it first. MrAd is the
    // only app class that touches the ad SDK, so forcing that one consumer true
    // disables banner, interstitial, exit and native ads. isProVersion only
    // distinguishes the separate paid Pro listing (backup suffixes .mrpro/.mrstd),
    // so there is no in-app premium gate; ads-only.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_MOONREADER = Compatibility(
        name = "Moon+ Reader",
        packageName = "com.flyersoft.moonreader",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x283593,
        targets = listOf(AppTarget(version = "10.7", versionCode = 1007000))
    )

    // Verified 2026-10-10 against com.foobnix.pdf.reader 9.4.20 (versionCode 7175) from APKPure.
    // No PairIP and no billing library in the free app. Ads are Google Mobile Ads, gated by
    // AppsConfig.isShowAdsInApp(Context) (MobileAds init, interstitial/banner/reward checks) and
    // ADS.isRewardActivated() (the interstitial, banner and reward show paths). The Pro build is a
    // separate paid listing, so this patch is ads-only.
    val COMPATIBILITY_LIBRERA = Compatibility(
        name = "Librera",
        packageName = "com.foobnix.pdf.reader",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1E88E5,
        targets = listOf(AppTarget(version = "9.4.20", versionCode = 7175))
    )

    // Verified 2026-10-10 against com.alarmclock.xtreme.free 26.06.0 (versionCode 70004221) from
    // APKPure. Split base (requiredSplitTypes), so the bundle is APKM. No PairIP. The ad-free
    // entitlement is ShopFeature AD_FREE, read through the shop data's d(ShopFeature) check
    // (the implementation takes the "feature" argument and returns Z). Ad SDKs: GMS ads, Vungle,
    // Facebook Audience Network and Unity Ads. Only the AD_FREE entitlement is forced true, so the
    // paid all-in-one and other shop features are not unlocked.
    val COMPATIBILITY_ALARMCLOCK = Compatibility(
        name = "Alarm Clock Xtreme",
        packageName = "com.alarmclock.xtreme.free",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xF44336,
        targets = listOf(AppTarget(version = "26.06.0", versionCode = 70004221))
    )

    // Verified 2026-10-11 against com.appgenix.bizcal 2.55.5 (versionCode 255501) from the
    // existing apks/bizcal/base.apk. Single APK, no PairIP. The app's own code is readable.
    // AdsUtil.showAdsForUser(Context) is the one user-level ad gate: it guards the mediation SDK
    // start (StoreUtil.initializeMobileAds, Fyber/Chartboost/AppLovin/Vungle/Facebook/InMobi/Unity)
    // and the interstitial preload and show path. Pro (ProUtil) is untouched: ads only.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_BUSINESSCALENDAR = Compatibility(
        name = "Business Calendar 2",
        packageName = "com.appgenix.bizcal",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x009688,
        targets = listOf(AppTarget(version = "2.55.5", versionCode = 255501))
    )

    // Verified 2026-10-11 against app.expert.player 11.2.0 (versionCode 306) from APKPure's XAPK
    // (base + arm64 and locale splits, merged with APKEditor). Flutter app: its logic and premium
    // checks are native Dart (libapp.so), which bytecode patches cannot change. The ads are the
    // Flutter google_mobile_ads plugin: FlutterMobileAdsWrapper.initialize starts a thread whose
    // run() calls MobileAds.initialize, so that run() is the single Java-side ad startup. No PairIP.
    val COMPATIBILITY_EXPERTPLAYER = Compatibility(
        name = "TV Smarters Player Expert",
        packageName = "app.expert.player",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "11.2.0", versionCode = 306))
    )

    // Verified 2026-09-29 against com.streema.simpleradio 6.2.0 (versionCode 872,

    // APKPure universal). App code is not obfuscated. Premium state is entirely local:
    // SimpleRadioBaseActivity.isPremium() returns mIabService.isInitialized() &&
    // mIabService.c(), and the IAB service (b9/j) answers c() from SharedPreferences
    // ("iab_premium" boolean OR "iab_subscription_date_end" timestamp in the future).
    // Ads run through AppLovin MAX mediation (the SDK init key is inline) plus Google
    // Mobile Ads (AdMob native/interstitial), so "Disable ads" hooks those stable
    // library surfaces (MobileAds.initialize overloads, MaxInterstitialAd loadAd/showAd).
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    // 2026-10-13 "Disable ads" fix, smoke-tested on 6.2.0 (872) and 6.2.4 (895, split base).
    // Before: the banner hook targeted AdView, which has no loadAd, so it matched nothing and
    // banners kept loading while the patch reported Applied; AdManager interstitials (the
    // path every non-"admob" AdsExperiment bucket takes), rewarded, rewarded-interstitial,
    // app-open and MAX banner/rewarded/app-open/native loads were not hooked at all. Now every
    // listed entry point is mandatory (missing class or method = patch failure): GMA
    // BaseAdView.loadAd, AdManagerAdView.loadAd, AdLoader.loadAd/loadAds, static load() on
    // InterstitialAd, AdManagerInterstitialAd, RewardedAd, RewardedInterstitialAd, AppOpenAd;
    // MAX loadAd on MaxInterstitialAd/MaxAdView/MaxRewardedAd/MaxAppOpenAd/MaxNativeAdLoader
    // plus every MaxInterstitialAd.showAd. GMA/MAX names are unobfuscated in both versions.
    // Prebid (6.2.4 only, obfuscated) is not hooked: the app blocks on its demand future with
    // an untimed get(), and the AdManagerInterstitialAd.load no-op already stops the ad.
    // 2026-10-14 crash fix: with MobileAds.initialize a no-op (released patch too), the ads
    // manager's MobileAds.setAppVolume(0f) threw IllegalStateException on every cold start while
    // Remote Config "android_ump_enabled" was not active (fresh install), with or without Enable
    // Premium. Every concrete static void on MobileAds now returns early and
    // putPublisherFirstPartyIdEnabled answers false. New anchor for the empty native "Ad" row in
    // the station lists: the AdsExperiment Boolean stored after "android_search_nativeads_show"
    // (getter s2() on 6.2.0, x2() on 6.2.4) answers false. Smoke test (both patches, one run)
    // passes on 6.2.0 (872) and 6.2.4 (895); 6.2.4 CLI-patched without -f and disassembly
    // checked. Emulator 2026-10-14 (6.2.4, fresh installs, Disable ads alone and with Enable
    // Premium, 3-4 cold starts each incl. after UMP consent): no crash and no "must be called
    // prior" exception; no "Ad" card (stock shows it at the top of Recommended); audio plays and
    // survives background/return; no interstitial; Enable Premium hides the paywall and the
    // Premium tab. 6.2.0 dropped: only the latest verified version is supported.
    val COMPATIBILITY_SIMPLERADIO = Compatibility(
        name = "Simple Radio",
        packageName = "com.streema.simpleradio",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFF6D00,
        targets = listOf(AppTarget(version = "6.2.4", versionCode = 895))
    )

    // Verified 2026-09-29 against com.farproc.wifi.analyzer 3.10.5-L (versionCode 999,
    // APKPure). Tiny single-dex app, not obfuscated. Banner ads are gated by
    // Settings.a(Context), which reads the "next_show_ad_time_millisec" preference and
    // returns true once the hide-until date passes; MainScreen.N() shows the legacy
    // AdMob banner (com.google.android.gms.ads.e) only when the gate is true. Forcing
    // the gate false hides the banner everywhere.
    // 2026-10-13, 3.11.2 (versionCode 139): patch unchanged, the CLI applies it without -f.
    // MainScreen's ad methods were renamed (gate wrapper E(), banner H(), interstitial D());
    // the gate Settings.a(Context) still has exactly two callers, MainScreen.E and
    // LanNeighborsScreen.j, so the main banner, the interstitial and the LAN banner all reach
    // the forced-false gate (checked in the patched bytecode). The LeadBolt web ad that
    // 3.10.5-L carried is gone from 3.11.2; on 3.10.5-L it is not blocked, and only the banner
    // was checked there. 3.11.2 has a lower versionCode (139) than 3.10.5-L (999), so moving
    // from 3.10.5-L needs an uninstall first.
    val COMPATIBILITY_WIFIANALYZER = Compatibility(
        name = "WiFi Analyzer",
        packageName = "com.farproc.wifi.analyzer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x00ACC1,
        targets = listOf(
            AppTarget(version = "3.10.5-L", versionCode = 999),
            AppTarget(version = "3.11.2", versionCode = 139)
        )
    )

    // Verified 2026-09-29 against com.Project100Pi.themusicplayer 3.2.0.0_release_2
    // (versionCode 32001, APKPure universal). Premium state is a static boolean flag
    // plus a 5-element purchase list in an R8-obfuscated holder class (`v7/g` here):
    // a() checks the list size, b() returns flag && a(), and ~20 call sites read the
    // flag field directly. Purchases (remove_ads / combo SKUs) set the flag via c(Z);
    // a temp-ad-free resetter clears it when the trial timestamp expires. The patch
    // forces a()/b() true, neuters the resetter and seeds the flag true in <clinit>,
    // so every consumer sees premium without touching Play Billing.
    // Ads run through AppLovin MAX mediation plus Google Mobile Ads, so "Disable ads"
    // hooks those stable library surfaces (MobileAds.initialize overloads, GMA loads,
    // MaxInterstitialAd loadAd/showAd).
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    val COMPATIBILITY_PIMUSICPLAYER = Compatibility(
        name = "Pi Music Player",
        packageName = "com.Project100Pi.themusicplayer",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x3F51B5,
        targets = listOf(AppTarget(version = "3.2.0.0_release_2", versionCode = 32001))
    )

    // Verified 2026-10-05 against com.shazam.android 16.62.0 (versionCode 1606200,
    // APKPure universal). No ad SDK, no billing client, no shields in the dex, so
    // the only patchable surface is telemetry: FirebaseAnalytics.logEvent(String,
    // Bundle) is public and concrete, and FirebaseCrashlytics is fully public
    // (isCrashlyticsCollectionEnabled/recordException/log). No premium gate exists
    // (Apple-owned free app).
    // Verified 2026-10-11 against the same build: "Open in YouTube (Music)" rows come
    // from server config (streamingproviders[]: packagename + intent:// song/artist/album
    // URIs with package=<stock>;). Every consumer reads the StreamingProvider data class
    // (R8 Hn.u), found by its unique toString string "StreamingProvider(streamingProviderId=";
    // its only constructor (obj, String x5, Z) is hooked so p3..p6 pass through the shazam
    // extension's remap(). Cross-checked against the config parser (List-returning, strings
    // "packageName(...)"/"songUri(...)"/"albumUri(...)") constructing that class. The
    // manifest's <queries> lists only the stock packages (targetSdk 36), so the patch adds
    // the Morphe packages there too. Version names carry a build suffix ("16.62.0-261001");
    // the old bare "16.62.0" target made Manager/CLI skip Shazam patches (fixed 2026-10-11).
    // 16.62.1-261006 (APKMirror bundle, merged) verified 2026-10-11: both patches' smoke
    // test + CLI without -f, and on the emulator with Morphe YouTube 21.20.405 / YT Music
    // 9.18.50 + MicroG RE the rows open the Morphe apps (START pkg=app.morphe.android.*).
    // Shazam ships as a bundle only (requiredSplitTypes base__abi,base__density).
    val COMPATIBILITY_SHAZAM = Compatibility(
        name = "Shazam",
        packageName = "com.shazam.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0088FF,
        targets = listOf(
            AppTarget(version = "16.63.0-261008", versionCode = 1606300),
            AppTarget(version = "16.62.1-261006", versionCode = 1606210),
            AppTarget(version = "16.62.0-261001", versionCode = 1606200),
        )
    )

    // Verified 2026-10-05 against com.podcast.podcasts 9.17.0 (versionCode 260909124,
    // APKPure universal). The app code is partly obfuscated; all ads run through a
    // heavy mediation stack (GMA, AppLovin MAX, Meta Audience Network, InMobi, Vungle
    // strings, Pangle, Huawei), so "Disable ads" hooks those stable library surfaces
    // (initialize/init, every load/loadAd and the MAX terminal showAd overload).
    // No premium patch: the only ad-free path is Firebase invite referral state
    // (server-driven); no local purchase gate was found (no queryPurchases /
    // onPurchasesUpdated in app code).
    // 2026-10-12: the real versionName carries a build suffix; the bare version made
    // Manager/CLI treat the patches as incompatible.
    val COMPATIBILITY_PODCASTREPUBLIC = Compatibility(
        name = "Podcast Republic",
        packageName = "com.podcast.podcasts",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF5722,
        targets = listOf(AppTarget(version = "9.17.0-260909124", versionCode = 260909124))
    )

    // Verified 2026-10-05 against fm.castbox.audiobook.radio.podcast 11.26.1
    // (versionCode 260915290, APKPure universal). App code is not obfuscated. All ads
    // run through a heavy mediation stack (GMA, AppLovin MAX, Meta Audience Network,
    // InMobi, Vungle strings, Pangle, Huawei), so "Disable ads" hooks those stable
    // library surfaces (initialize/init, every load/loadAd and the MAX terminal
    // showAd overload).
    // No premium patch: premium reads server-synced vip lists (UserProperties filled
    // from the account backend) and local purchases are RSA-verified in
    // BillingRepository, so there is no client-side gate to force.
    // 2026-10-12 APK-finder audit: only distributed as a split bundle (APKMirror BUNDLE /
    // APKPure-Uptodown XAPK), so apkFileType is APKM; any "universal APK" note below is outdated.
    // 2026-10-12: the real versionName carries a build suffix; the bare version made
    // Manager/CLI treat the patches as incompatible.
    val COMPATIBILITY_CASTBOX = Compatibility(
        name = "Castbox",
        packageName = "fm.castbox.audiobook.radio.podcast",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xF44336,
        targets = listOf(AppTarget(version = "11.26.1-260915290", versionCode = 260915290))
    )

    // Verified 2026-10-07 against com.melodis.midomiMusicIdentifier.freemium 10.5.8
    // (versionCode 21134, APKPure universal). NOTE the package: the free app is
    // com.melodis.midomiMusicIdentifier.freemium - the com.melodis.soundhound.android
    // name the candidates doc previously guessed does not exist. The app code is
    // R8-obfuscated, but every ad runs through the full public Google Mobile Ads API
    // (banner AdView, native AdLoader, interstitial, rewarded, rewarded-interstitial,
    // app-open, Ad Manager banner/interstitial, GMA preloading) plus Meta Audience
    // Network, so the patch hooks those stable library surfaces: the SDKs never
    // initialize, preload never starts and every load/loadAd/loadAds becomes a no-op.
    // No PairIP or other shields in the dex. Play Billing is present for the Pro
    // subscription but the premium gates are obfuscated; premium deep-dive parked -
    // ads only.
    val COMPATIBILITY_SOUNDHOUND = Compatibility(
        name = "SoundHound",
        packageName = "com.melodis.midomiMusicIdentifier.freemium",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x000000,
        targets = listOf(AppTarget(version = "10.5.8", versionCode = 21134))
    )

    // Verified 2026-10-07 against one.cricket.app 26.08.01 (versionCode 90, APKPure base
    // split). The app ships as a split bundle: libnative-lib.so (loaded by the ad loaders'
    // static initializers and by MyApplication for its API URLs) lives in the ABI split,
    // so users must patch the full APKM/XAPK. App classes keep their names but R8 renames
    // their methods, so the ad wrappers (one/cricket/app/ads/*) are matched by class +
    // parameter shape; AppOpenManager.onStart keeps its name (@OnLifecycleEvent). The
    // other bundled networks (Mintegral, IronSource, Vungle, ...) are only reached via
    // AdMob mediation, so the AdMob loads are the backstop. No premium tier exists (no
    // billing code). No PairIP.
    val COMPATIBILITY_ONECRICKET = Compatibility(
        name = "OneCricket",
        packageName = "one.cricket.app",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0D47A1,
        targets = listOf(AppTarget(version = "26.08.01", versionCode = 90))
    )

    // Verified 2026-10-07 against com.gregacucnik.fishingpoints 4.7.3 (versionCode 410,
    // APKPure). Premium is client-side: RevenueCat entitlements are pushed into four tier
    // flags on AppClass (the Application, name kept), and every gate plus the ad manager
    // reads aggregate getters that OR those flags (A/B/K in this build, R8-renamed, found by
    // shape). The forecast/weather API sends no entitlement, so the unlock is real. Ads
    // (AdMob + Pangle/Meta mediation) are skipped for premium users. Play Integrity is only
    // the Billing library's transitive dependency and gates nothing. No PairIP.
    val COMPATIBILITY_FISHINGPOINTS = Compatibility(
        name = "Fishing Points",
        packageName = "com.gregacucnik.fishingpoints",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1565C0,
        targets = listOf(AppTarget(version = "4.7.3", versionCode = 410))
    )

    // Verified 2026-10-07 against com.onesports.score 4.3.1 (versionCode 296, APKPure split
    // bundle merged). VIP is server-authoritative but cached: the server's vip int/expiry are
    // written to MMKV and every client gate reads UserPreference (name kept): a "logged in AND
    // vip == 1" getter and the raw vip int getter, both R8-renamed and found by shape. Forcing
    // them unlocks the VIP UI and removes ads (ad fragments hide for VIP). Premium data the
    // server delivers (predictions, dropping odds) is validated server-side and stays gated.
    // Also covered by rushiranpise/morphe-patches (coverage is informational only).
    // 2026-10-13: 4.3.2 (versionCode 297, merged arm64) verified, patch unchanged; AiScoreSmokeTest
    // passes. 4.3.2 adds direct AdMob units (an AdMob/MAX network switch) next to AppLovin MAX
    // in the match-detail banner, stats-detail banner and match-list native row, but each sits
    // in the same VIP-gated block as the MAX path: the match-detail runnable checks a field set
    // from UserPreference's VIP gate in onCreate, the stats activity checks the gate before
    // posting its banner runnable, and the list builder inserts native-ad rows only when the
    // gate (passed as a "no ads" flag) is false. No GMA hooks are needed.
    // 2026-10-14, 4.3.2: added "Spoof signature" (shared builder, on by default) for the reported
    // "goal alerts never arrive". AiScore's Firebase API key (project aiscore-abb71) is restricted
    // to the original signing cert (CN=onescore, SHA-1 d34738b4...a84c, read from the bundle's
    // v2/v3 block), so a re-signed build gets 403 API_KEY_ANDROID_APP_BLOCKED from Firebase
    // Installations, FCM never gets a token and AppFcmTokenWorker never reports one (POST
    // v1/app/device/report/fcm). FIS builds X-Android-Cert through AndroidUtilsLight and the
    // R8-renamed PackageManagerWrapper's getPackageInfo(String, 0x40); the patch routes every
    // PackageManager.getPackageInfo call through the signature extension. Push is plain FCM and
    // the VIP patch is unrelated. FotMob's key is unrestricted, which is why it needs no spoof.
    // No GmsCore patch (works with Play services). Spoof signature was only verified on 4.3.2.
    // Emulator 2026-10-14 (Play services, fresh installs): Enable Premium alone -> FIS 403
    // API_KEY_ANDROID_APP_BLOCKED, PersistedInstallation Status 4, no FCM token; with Spoof
    // signature -> Status 3 with an auth token, "Token successfully retrieved", an FCM token in
    // appid.xml and AppFcmTokenWorker SUCCEEDED. VIP badge unlocked in both; no crashes.
    val COMPATIBILITY_AISCORE = Compatibility(
        name = "AiScore",
        packageName = "com.onesports.score",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x00C853,
        targets = listOf(AppTarget(version = "4.3.2", versionCode = 297))
    )

    // Verified 2026-10-07 against com.resultadosfutbol.mobile 6.6.0 (versionCode 23005536,
    // APKPure). The blocklist previously had this app under the wrong package
    // (com.besoccer). PairIP license check only (no VM shield): the manifest Application is
    // com.pairip.application.Application, whose attachBaseContext only calls
    // LicenseClient.checkLicense, so that one call is neutralised for re-signed builds. The
    // ad-free subscription is server-validated (backend returns an empty AdsConfig for
    // subscribers) and the app's ad engine is obfuscated, so ads are removed at the mediation
    // SDKs' init (AppLovin MAX, Unity, Meta, Vungle; AdMob has no initialize in this build).
    val COMPATIBILITY_BESOCCER = Compatibility(
        name = "BeSoccer",
        packageName = "com.resultadosfutbol.mobile",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x2E7D32,
        targets = listOf(AppTarget(version = "6.6.0", versionCode = 23005536))
    )

    // Verified 2026-10-07 against com.rawcam.app 1.4.3 (versionCode 55, APKPure split bundle
    // merged). Method adapted from the WaggBR and franticg33k Native Camera patches (GPL-3.0,
    // last targeting 1.4.2): premium is the local "is_premium" flag in "rawcam_prefs". 1.4.3
    // moved the read/write into shared helpers o()/p(SharedPreferences, String, Z), so the
    // patch persists true right before the CameraViewModel constructor reads the flag (instead
    // of flipping the read's default register, which R8 may reuse) and forces the setter's
    // argument true. PairIP license check only (Application wrapper, no VM shield). No ads.
    // Device-tested 2026-10-07 by the owner on a Galaxy S25 Ultra (Android 17, arm64-only;
    // Play Store 1.4.3 base + splits, base patched, all re-signed): app starts without the
    // license wall and all premium features work. Note APKPure's bundle only carries the
    // armeabi-v7a split, which arm64-only phones cannot install.
    val COMPATIBILITY_NATIVECAMERA = Compatibility(
        name = "Native Camera",
        packageName = "com.rawcam.app",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xD0BCFF,
        targets = listOf(
            AppTarget(version = "1.4.3", versionCode = 55),
            AppTarget(version = "1.4.4", versionCode = 56)
        )
    )

    // Verified 2026-10-07 against com.ztnstudio.notepad 5.4.3.19019 (versionCode 19019,
    // APKPure split bundle merged). App classes keep their names, methods are R8-renamed.
    // Premium: every gate reads UserPremiumData's premium getter (b() here, found by shape)
    // and some also BuyAdFreePreferenceHelper's "isPurchased" getter. Ads: the bundled
    // Calldorado SDK (in-app ad manager started unconditionally, plus the after-call screen);
    // its public start/startInAppAdManager entry points are neutralised. RevenueCat + Play
    // Billing handle purchases. No PairIP.
    val COMPATIBILITY_ZTNNOTEPAD = Compatibility(
        name = "#Notepad",
        packageName = "com.ztnstudio.notepad",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFFC107,
        targets = listOf(AppTarget(version = "5.4.3.19019", versionCode = 19019))
    )

    // Verified 2026-10-12 against net.smartwod.timer 1.46.9 (versionCode 10000399, APKMirror
    // bundle merged, all four ABIs). React Native + Hermes; no PairIP or packer. Ads are Google
    // Mobile Ads 23.6.0 only, driven from JS through react-native-google-mobile-ads. "Disable
    // ads" hooks stable library API: MobileAds.initialize (both overloads), startPreload and the
    // setAppMuted/setAppVolume/openDebugMenu calls that throw without init; every concrete
    // load/loadAd/loadAds on BaseAdView, AdLoader, Interstitial/Rewarded/RewardedInterstitial/
    // AppOpen and the AdManager variants; and the RN module's initialize(Promise)
    // (io.invertase.googlemobileads, kept for RN reflection) resolves an empty adapter list.
    // UMP consent is untouched. Premium (2026-10-12): the Hermes JS decides it from
    // react-native-iap's getAvailableItemsByType("inapp"/"subs") results and only checks each
    // entry's productId (no state/signature/token/server check), revoking premium whenever the
    // product is missing. "Enable Premium" hooks RNIapModule.getAvailableItemsByType (class and
    // @ReactMethod name kept for RN reflection) to call the smartwod extension, which resolves
    // net.smartwod.timer.premium.lifetime (Premium+) and inapp.adfree as acknowledged purchases
    // ("subs" stays empty) without touching Play Billing. The JS only runs that check after
    // initConnection succeeds, which needs Play Services and a reachable Play Store; that path
    // is left unpatched.
    // Runtime 2026-10-12 (Play-certified API 35 emulator): patched app starts cleanly, drawer
    // shows "Premium+ / Lifetime", MIX unlocked, no banner on the AMRAP screen (stock shows a
    // filled banner + paywall); no crashes or JS errors.
    val COMPATIBILITY_SMARTWOD = Compatibility(
        name = "SmartWOD Timer",
        packageName = "net.smartwod.timer",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x333333,
        targets = listOf(AppTarget(version = "1.46.9", versionCode = 10000399))
    )

    // Verified 2026-10-12 against me.bluemail.mail 2.2.408 (versionCode 47587, APKMirror bundle
    // merged, arm64-v8a + armeabi-v7a). Native K-9-based mail app (com.trtf.blue) with a React
    // Native + Hermes layer. Ads are Google Mobile Ads only, driven from JS through
    // react-native-google-mobile-ads; the native app code never calls GMA directly. "Disable ads"
    // hooks stable library API: MobileAds.initialize (the only overload R8 kept) and the
    // setAppMuted/setAppVolume/openDebugMenu calls that throw without init; every concrete
    // load/loadAd on BaseAdView, AdLoader, Interstitial/Rewarded/RewardedInterstitial/AppOpen and
    // AdManagerInterstitialAd; and the RN module's initialize(Promise)
    // (io.invertase.googlemobileads, kept for RN reflection) resolves an empty adapter list.
    // UMP consent is untouched. The premium/ad-free entitlement is server-side
    // (hasServerAdFreeEntitlement, verifyPurchaseWithProvider), not patched.
    // Not runtime-tested: the APK ships only ARM libs with extractNativeLibs=false, so even the
    // stock app dies at startup on the x86_64 emulator (libreactnative.so not found). Bytecode
    // verified only; the same GMA/RN design was runtime-checked on SmartWOD.
    val COMPATIBILITY_BLUEMAIL = Compatibility(
        name = "BlueMail",
        packageName = "me.bluemail.mail",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x006CEF,
        targets = listOf(AppTarget(version = "2.2.408", versionCode = 47587))
    )

    // Verified 2026-10-12 against com.truecaller 26.39.6 (versionCode 2639006, APKMirror bundle
    // merged; APKPure also ships a standalone APK). Fully R8-obfuscated outside kept model/worker
    // classes, so every anchor is a pref key, a kept type in the signature, or method shape:
    //   - Enable Premium: the "premiumLevel" tier getter (returns PremiumTierType) -> GOLD; the
    //     plain "isPremiumExpired" getter in the same class (not the "isInGracePeriod" one) ->
    //     true; the static (PremiumFeatureStatus)Z gate that compares with INCLUDED -> true; the
    //     (PremiumFeature, Z)Z feature check that reads "disabledPremiumFeatures" -> true (it is
    //     the server kill-switch list *and* the availableFeatures INCLUDED lookup in one method);
    //     PremiumState's constructor (kept types ZLPremiumTierType;LProductKind;LPremiumScope;
    //     LInsuranceState;) gets isPremium = true and tier = GOLD; and the kept
    //     PremiumSubscriptionStatusNotificationWorker.doWork returns a work Result at once (Success
    //     has no no-arg constructor in this build, so the obfuscated Failure class, found by its
    //     "Failure {mOutputData=" toString, is used; doWork already returns it on its own paths).
    //   - Disable telemetry: AppStartTracker.enableTracking (kept), push(String) and
    //     push(String, Map) on the CleverTapManager implementation (kept interface), and every
    //     concrete start(Context, ...) on the AppsFlyerLib subclass.
    //   - Hide Assistant tab / Hide Scam feed: the feature toggles read by exact key
    //     "featureCallAssistant" / "featureScamFeed" (exact-match filters; the keys are prefixes
    //     of other toggles). featureScamFeed is the master switch for the Scam Feed tab, menu
    //     entry, deep link and its notification worker.
    //   - Use SMS for verification codes: the OtpSmsApi selection branch (GOOGLE vs SMS) of an
    //     R8-merged Function0 lambda; only that branch is changed, the other 28 cases are not.
    //   - Disable ads: the ads module never reads Premium or NO_ADS (Enable Premium's NO_ADS is
    //     at most partial for ads). Banner/native: every void bind method of the kept
    //     ads.ui.AdsContainer / AdsContainerLight / AdsSwitchView that takes the banner holder,
    //     a native holder or AdLayoutTypeX sets the view GONE and returns; the holder types are
    //     read at patch time from AdsSwitchView's (holder, AdLayoutTypeX) method and the shared
    //     layout-config parameter. Full-screen: AdInterstitialActivity, AdAppOpeningActivity and
    //     adsrouter AdRouterInterstitialActivity finish right after super.onCreate. Rewarded ads
    //     (WhoViewedMeAdActivity) are left alone: they gate a feature.
    //   - Disable update prompts: the in-app update manager's suspend (Activity, UpdateTrigger,
    //     continuation) entry, scoped by its (Activity, UpdateTrigger)CompletableFuture sibling,
    //     returns Boolean.FALSE; every soft and HardUpgrade Play Core flow starts there. Also the
    //     app's own forced-update state getter (strings "forcedUpdate_updateType" and
    //     "forcedUpdate_appVersion", eak.a() here) returns UpdateType.NONE, so
    //     forcedupdate.ui.ForcedUpdateActivity never shows a REQUIRED or DISCONTINUED page.
    //     SimpleForceUpdateActivity is left alone: WizardActivity only starts it when a drawable
    //     is missing (broken install), not from the server state.
    //   - GmsCore support / Spoof signature: shared builders, launch activity
    //     ui.TruecallerInit.onCreate, original cert CN=truecaller (SHA-1 0ac1169a...1f25e31);
    //     androidx.credentials routing applies (onGetCredential tests isAvailableOnDevice(int)).
    // Server-side and not unlocked: Who viewed / searched for me data, Identify AI, cloud call
    // recording, fraud insurance, the verified badge, and phone verification (server-side Play
    // Integrity attestation; nothing here targets Play Integrity).
    // Ideas taken from the GPLv3 Paresh and bufferk Truecaller patches (anchors there were for
    // older builds and pinned obfuscated names). Rejected: pinning a6x/pol/myw/x5x/hqa/co70/es7/
    // nk00 names; AttributesDTO.<init>(Z) (Gson-built, nothing outside the DTO reads isPremium in
    // this build); returning SMS at offset 0 of the OtpSmsApi lambda (it would break every other
    // merged case).
    // Re-verified 2026-10-14 against 26.39.6 (smoke test + jadx/smali review, not runtime-tested)
    // for user reports #52/#54 and request #53:
    //   - Enable Premium: (PremiumFeature, Z)Z now falls through to the stock check for
    //     CALL_ASSISTANT and FAMILY_SHARING (kept enum constants; both server-backed, and a
    //     forced Call Assistant let "Assistant"-handled spam calls ring through). The premium
    //     status sync writer (Object return, 2 params, strings "premiumHadPremiumBefore" +
    //     "entitledCatalogItemIds", only i3x.e here) returns kotlin.Unit (singleton field found by
    //     type) at once, the equivalent of the reference "SavePremiumState": its
    //     PremiumStatusUpdate event treated the forced isPremium plus the server's "expired" as a
    //     lost subscription, which reset Max spam protection to Basic, the premium blocking
    //     options and the call blocking method, re-enabled ads and posted "subscription expired".
    //   - Disable ads: interstitials are gated at the source, the ()Z check that reads exactly
    //     "featureInterstitialAd" (krk.e here) -> false, so placements run the user's action at
    //     once; finishing AdInterstitialActivity / AdRouterInterstitialActivity dropped Click to
    //     WhatsApp, block, details, pay and rewarded callbacks and was removed. Only
    //     AdAppOpeningActivity (a loading screen without a pending action, reached only from the
    //     gated flow) still finishes after super.onCreate.
    //   - Hide Premium tab / Hide Blocking tab (opt-in): the bottom bar builder ((SparseIntArray)V
    //     reading BottomBarButtonType.CALLS / MESSAGES, then a static ([Object)List filterNotNull
    //     and CollectionsKt (Iterable, I)List take) calls a new static helper per patch on the tab
    //     array right before filterNotNull; it nulls PREMIUM, or BLOCKING and
    //     FAMILY_PROTECTION_CONFIG (both labelled "Blocking"), read through the tab base class's
    //     BottomBarButtonType getter. Calls and Messages always stay; selecting a hidden tab
    //     (deep links, upsells) is a no-op in NavigationBarView.setSelectedItemId.
    //   - Hide family button / Restore classic call log (opt-in; the latter experimental): the
    //     central toggle resolver (String, FeatureState)Z that calls SharedPreferences.getBoolean
    //     (twins with the same signature do not) returns false for "featureFamilyProtect" /
    //     "featureCallLogRevamp", via a shared key -> value override helper.
    //   Emulator 2026-10-14 (logged out): stock, the default patches, and the default patches plus
    //   the four new opt-in ones all install, run onboarding and survive 3 cold starts with no
    //   crash. Everything past onboarding needs SMS registration, so the premium-sync fix, the
    //   interstitial change, the hidden tabs, the family button and the classic call log were NOT
    //   checked at runtime (smoke test + CLI disassembly only).
    val COMPATIBILITY_TRUECALLER = Compatibility(
        name = "Truecaller",
        packageName = "com.truecaller",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x0087FF,
        targets = listOf(AppTarget(version = "26.39.6", versionCode = 2639006))
    )

    // Verified 2026-10-13 against com.kakao.talk 26.8.3 (versionCode 29260830, APKMirror bundle
    // merged, ~212k classes). App classes are R8-renamed but keep their Kotlin .source names and
    // coroutine @DebugMetadata/EnclosingMethod annotations; the AdFit SDK keeps its public API.
    // Ads, unread limits, mobile-only messages, the tab patches, Strip image metadata and the
    // Sentry / S2Event / TalkShareLog parts of Disable telemetry are ported from
    // AmpleReVanced/revanced-patches (GPL-3.0, written for 26.7.2) with the Settings toggles
    // dropped; Ghost typing, Ghost read and the Tiara / Crashlytics hooks are new. Anchors:
    //   - Disable ads: NativeAdLoader.load (both overloads) and the FocusAdLoader implementation's
    //     loadAd return true; by .source + shape: OpenLinkChatAdController (BizBoardAdView.loadAd),
    //     ChatListAdHelper (portrait check, global suspend check), OpenChatTabFeedAdViewHolder,
    //     PayAdPlatformBannerState/AdFit, SftAdFragment load/render, the birthday BizBoard binder and
    //     the FriendTabGlobalAdViewModel model lambda; BizBoardFrameLayout.onMeasure and FeedAdLayout
    //     (constructor, setFeedAd, added onMeasure) by kept class name. The reference's
    //     FriendListChipBizBoard anchor does not match 26.8.3 and is not ported.
    //   - Show messages restricted to mobile: AlimtalkType$Companion and LeverageType cover state
    //     -> NONE; the Universal check right before "new MobileOnlyCover" (found by its toString)
    //     -> false (the first UniversalPublicData check in 26.8.3 is the lock cover, untouched).
    //   - Remove 300+ / 99+ unread limit: the "300+" if-lt in BaseChatRoomItemViewHolder and
    //     OpenLinkChatsItem, the Math.min(count, 300) in UnreadCountInfo; the 99 clamps in
    //     ChatLogRecyclerItem and WatermarksManager.
    //   - Remove shop tab: MainTabConfig's tab list nulls a SHOPPING_TAB result in slot 3 (that
    //     slot is CALL_TAB without shopping, which the reference's slot delete would remove).
    //   - Disable community tab: OpenChatTabFragment's ConcatAdapter keeps only the chat list.
    //   - Disable friend feed tab: TabConfig(isFriendFeedTabEnabled=...) constructor clears the
    //     flag; the FriendTab gate called by FriendSettingsViewModel and its identical twin return
    //     false. The reference's third edit hits isCheckedDisplayUpdatedFriends here; dropped.
    //   - Disable telemetry: TiaraTracker.java enqueue(TiaraLog)/flush by log strings; SentryAndroid
    //     init by its two error strings; FirebaseCrashlytics send/record/log/user/key methods; the
    //     Available2 flag resolver returns false for USE_S2_EVENTS and USE_TALK_SHARE_LOG.
    //   - Ghost typing / Ghost read: ChatRoomApiHelper$Companion requestAction and
    //     requestChatOnRoom, resolved through @DebugMetadata + EnclosingMethod and cross-checked
    //     against a .source + shape fallback; MsgJob's "notiRead" value forced to FALSE. Ghost read
    //     is inferred from the client code and not verified against the server.
    //   - Strip image metadata: every return of ImageItemSendHelper's (File, I, J)File resize step
    //     goes through extensions/kakaotalk StripImageMetadata.sanitize (framework ExifInterface).
    // A re-signed build, unpatched, with the 3 default patches and with all 11, launches to the
    // login screen on the emulator with no crash or verifier error (the all-patches build hit 2
    // slow-startup ANRs in 4 cold launches under ARM translation, stacks in app code, not patches);
    // login and the patched features were not runtime-tested. Deliberately not ported from the
    // reference: the integrity, signature, attestation/abuse-detection, Moat, packet-handler and
    // package-rename patches (they defeat Kakao's security controls, and the packet server opens
    // an unauthenticated network port), and "Disable AdFit environment detection" (only spoofs the
    // ad SDK's root/emulator check, pointless with Disable ads).
    val COMPATIBILITY_KAKAOTALK = Compatibility(
        name = "KakaoTalk",
        packageName = "com.kakao.talk",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0xFEE500,
        // A patched build is re-signed, so features that check KakaoTalk's signature or device
        // integrity (Kakao Pay, Kakao certificates, some bank and identity logins) may not work.
        // Raised by a Korean user on Reddit, 2026-10-14.
        targets = listOf(
            AppTarget(
                version = "26.8.3",
                versionCode = 29260830,
                description = "Patched builds are re-signed: Kakao Pay, Kakao certificates and " +
                    "bank logins may not work"
            )
        )
    )


    // Verified 2026-10-13 against jp.naver.line.android 26.15.1 (versionCode 261510192, APKMirror
    // bundle merged with APKEditor, minSdk 32). Ported from andrewliang25/morphe-patches (built for
    // 26.14.0) without its settings extension; R8 renames LINE's classes, androidx and even the
    // Thrift and DebugMetadata types every release, and the references' resource ids are stale
    // (andrew's bottom-bar badge id is now bnb_button_badge_live), so nothing is pinned to those:
    //   - Disable ads: the getBanners / getPrefetchableBanners Thrift receive branches (found by
    //     their "<op> failed: unknown result" strings, inside shared packed-switch lambdas) return
    //     null after the op-name string; the Smart Channel controller (FrameLayout constructor that
    //     casts to SmartChannelViewLayout) hides its host; LadAdView / LyadAdView collapse in
    //     onAttachedToWindow.
    //   - Fix push notifications (on by default): Firebase Installations' (URL, String) request
    //     builder sends LINE's original certificate SHA-1 in X-Android-Cert.
    //   - Hide LINE TODAY / Shopping / VOOM / Wallet tab: the ()List tab builder (HOME, CHAT, SQUARE,
    //     CALL reads) loses the NEWS+NEWS_ROW, COMMERCE+COMMERCE_TW, TIMELINE, MINI+WALLET pairs.
    //   - Hide community / calendar button: the Chats tab header builder (the <init> referencing
    //     "createEndButtons") loses its OPEN_CHAT / CALENDAR pair; the icon enum is derived from its
    //     PLUS_MENU read.
    //   - Hide AI friends button / Disable Agent i: the accessors behind the server flags
    //     function.ai.character.display_button, function.ai.agenti.enabled and
    //     function.search.line_ai_entry.enabled return false (agenti is Agent i's master switch).
    //   - Hide new badges: HeaderButton's (I, Z) dot setter, the chat header "N" badge, the settings
    //     row setters, the Home service badge enum's isVisible, the friend row GREEN_DOT_BADGE dot
    //     and the Chats tab green dot set (coroutine metadata c/m). Chat menu rows, the attach button
    //     dot and the bottom-bar "new" badge are not covered (only resource ids tell them apart).
    //   - Disable VOOM: the line://home/ handler ("hashtaglist" + "deleteCache") returns its
    //     Error(...) result; LineVoomActivity finishes after super.onCreate.
    //   - Keep unsent messages: the unsend lambda's already-unsent guard is forced after
    //     extensions/line KeepUnsentMessages inserts LINE's UNSENT notice row.
    //   - Keep chats unread: the read manager's (String, String) open-on-view wrapper returns; its
    //     worker is cross-checked against the "sendChatChecked" lambda's EnclosingMethod.
    //   - Open links in external browser: OpenUriActivity's intent builder (its extra keys) gets
    //     EXTERNAL_WITHOUT_CUSTOMTABS as its browser mode.
    // LineSmokeTest asserts every hook; a CLI run without -f applies all 15 patches and the
    // disassembled methods match. Runtime: launch and login only (see below).
    // 2026-10-13: "Read without receipt" (chat-list long-press row) and "Read notifications without
    // receipt", after linimal's viewing session (no typing-indicator patch: LINE sends no typing
    // state, only a local onTypingStatusChanged UI callback). ChatHistoryActivity.onCreate (string
    // "chatDataComponentProvider") claims a reserved chat; the read worker and the sendChatChecked
    // implementation return early for it. 26.15's menu is a list of ChatRowMenuItem (toString
    // "ChatRowMenuItem(labelRes=") built by a static (ChatPreviewItemData)ArrayList method that the
    // chat preview screen's menu shares; that menu returns its action through an Intent and an
    // exhaustive when, so it must never get the row. Only the bottom-sheet composable in the
    // builder's own class calls a generated menuRows helper (builder + the row). The row's action is
    // an extension class re-parented onto the sealed action type, the bottom sheet's dispatcher
    // (check-cast to that type, then the menu close) hands it to the extension, and both label
    // lambdas that read labelRes go through a generated labelText helper (the sentinel label id is
    // not a resource). Notification launches are reserved in the (Intent, Z) launch dispatcher
    // ("shouldLaunchChatHistoryActivityOnly") from BackNavigationArgs.isFromNotification.
    // LineSmokeTest and the CLI disassembly cover every hook; a CLI run without -f applies all 17
    // patches. Emulator (all 17 patches): dex2oat verify accepts every patched and extension class,
    // and a new account logs in and reaches the chat list with no errors. The menu row,
    // notification opens and receipt suppression are not runtime-tested (no second account to send
    // messages or to watch for "Read").
    val COMPATIBILITY_LINE = Compatibility(
        name = "LINE",
        packageName = "jp.naver.line.android",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x06C755,
        targets = listOf(AppTarget(version = "26.15.1", versionCode = 261510192))
    )

    // Verified 2026-10-13 against com.hevy 3.1.16 (versionCode 3325935, XAPK base + arm64 + en +
    // mdpi splits merged with APKEditor). Hevy is React Native on Hermes: the Pro logic is Hermes
    // bytecode (HBC v96) in assets/index.android.bundle, which ships in the base split, so patch a
    // merged APK. No expo-updates or CodePush, so no OTA bundle replaces the patched one.
    //   - Unlock Pro (rawResourcePatch): hevy/pro/HermesBytecode parses the HBC header, function
    //     headers and string table; the locator resolves the string ids of
    //     "isProStatusOverrideEnabled", "is_pro" and "isWithinProOfflineGracePeriod" and requires
    //     exactly one function whose bytecode holds all three as operands: HevyProStore's isPro
    //     getter (#32111 at 0x11b9d9c in this build, cross-checked with hermes-dec 0.1.7). Its first
    //     4 bytes become LoadConstTrue r0; Ret r0 (length-preserving; the footer SHA-1 is not checked
    //     at runtime). arandomhooman's fixed byte signature (isWithinProOfflineGracePeriod prologue, 3.1.0)
    //     no longer matches. Server-side limits (cloud-backed Pro features, Hevy Coach) stay locked.
    //   - Block telemetry & trackers (ported from kveld9): RNSentryModuleImpl.initNativeSdk
    //     resolves true without SentryAndroid.init; Adjust PackageHandler addPackage/
    //     sendFirstPackage, Facebook AppEventQueue.flush and Amplitude AndroidContextProvider.prefetch
    //     return at once; isLimitAdTrackingEnabled and Branch.isTrackingDisabled -> true; Amplitude
    //     getLegacyEvents resolves an empty array; SentryInitProvider/SentryPerformanceProvider are
    //     disabled in the manifest. Firebase Messaging, push and RevenueCat are untouched.
    // The bundle's trailing 20-byte SHA-1 is recomputed after the edit. HevySmokeTest asserts the
    // rewritten getter, the footer and every telemetry hook; a CLI decompile (no -f) shows only the
    // getter's first bytes and the 6 telemetry classes changed. On the emulator the patched bundle
    // loads through Hermes and the app reaches its welcome screen like stock (no crash or JS
    // error); Pro screens need an account and were not reached.
    val COMPATIBILITY_HEVY = Compatibility(
        name = "Hevy",
        packageName = "com.hevy",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x101012,
        targets = listOf(AppTarget(version = "3.1.16", versionCode = 3325935))
    )

    // Verified 2026-10-13 against easy.launcher 1.10.28 (versionCode 11028, the owner's APKMirror
    // bundle merged, 4 ABIs, minSdk 32). Play title "Easy Launcher"; the launcher label
    // (string/app_name) is "Easy Homescreen". App code is R8-renamed, but the com.eet.* packages,
    // kept model/worker/service classes, log lines and preference keys anchor every patch:
    //   - Disable ads: the static (Context)Z gate reading "ads.enabled" + "INSIDE_WALDO" -> false;
    //     in com.eet.core.ads.consent the "initializeMobileAdsSdk: " (GMA next-gen + CloudX start)
    //     and "requestConsentInfoUpdate: activity=" (UMP form) methods return at once; in
    //     com.eet.core.ads.activity the app-open loadAd ("loadAd: new ad wrapper created for key, ")
    //     returns and showAd ("showAd: failed to show ad, ") only runs its Function0 callback; the
    //     native cache's "replenishAds: size=" step returns; AdFreeViewModel.isAdsEnabled -> false.
    //   - Disable telemetry: every concrete logEvent/logException/setUserId/setUserProperties/
    //     setUserProperty/setCustomKey/dropBreadcrumb/sendScreenshot on all 8 AnalyticsService
    //     subclasses; AmplitudeAnalyticsService.initializeAmplitude; the Rudderstack constructor's
    //     write-key length reads 0 (its own "key empty" guard skips init); Singular.init (both
    //     overloads) -> false; BrazePushService.initialize returns Unit; Firebase
    //     setCrashCollectionEnabled always passes false; manifest switches for Crashlytics,
    //     Analytics, ad ID, deferred deep links and Facebook auto-init / advertiser ID. Remote
    //     Config, FCM, Firebase Installations and com.eet.core.push are untouched.
    //   - Disable rating prompts: the review observer's onResume ("app_launch_count_for_review" +
    //     "getDaysSinceInstall: error") returns; the "isUserPrimedForReview -> true" and
    //     "shouldRequestAppReview -> true" checks -> false.
    //   - Disable promotions: getEnabled/getActionCardEnabled/getSuggestEnabled of the GetJar and
    //     SportsPass Banner/Icon/Tile/Search remote configs -> false; CpaOffersActionCardProvider
    //     .isEnabled -> false; ImePromoHandler.pitchIme returns.
    //   - Disable default launcher reminder (off by default): in HomeRoleReminderWorker's work
    //     method the role-held check (the helper method that calls RoleManager.isRoleHeld) reads
    //     true, so the worker takes its "already held" path.
    //   - Hide sponsored content (off by default): SponsoredLinksResponse.getData,
    //     SponsoredSites.getSponsoredSites, SponsoredPostsResponse.getArticles and
    //     SponsoredLinkSuggestionProvider.getSuggestions return emptyList().
    // No premium patch: the paywall is dead code in this build. PremiumActivity is abstract with
    // no subclass and is not in the manifest; the declared PremiumBottomSheetDialog is never
    // started; BillingViewModel is only registered with Koin and never injected, and
    // BillingWorker / PurchasesBillingWorker are never enqueued. App Check / Play Integrity and
    // RootBeer are deliberately not touched. EasyLauncherSmokeTest asserts every hook, and a CLI
    // decompile (no -f) shows exactly 30 changed classes, none in the push stack. On the emulator
    // the patched launcher starts with no crash, the ad consent form and the news-feed native ad
    // are gone, the feed still shows articles with "Hide sponsored content" on, and the app-open
    // path still opens its destination. Known limit: the Braze SDK still initialises and registers
    // for push through another path; its event logging is blocked by the AnalyticsService no-ops.
    val COMPATIBILITY_EASYLAUNCHER = Compatibility(
        name = "Easy Homescreen",
        packageName = "easy.launcher",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x212121,
        targets = listOf(AppTarget(version = "1.10.28", versionCode = 11028))
    )

    // Verified 2026-10-13 against in.cricketexchange.app.cricketexchange 26.10.06 (versionCode
    // 667, APKMirror bundle merged with APKEditor: BNDLTOOL-signed, all four ABIs). App classes
    // keep their names, methods are R8-renamed. Premium (ad-free) is client-side: the server's
    // plan is cached as "expiry_date" in the "payment" prefs, and two checks compare it with
    // today, both meaning "expired, show ads": MyApplication's no-arg boolean getter (found by
    // "expiry_date" + "0000-00-00" + Date.after; ~160 call sites incl. the ad managers, app-open,
    // floating score and the adsVisibility flags passed between screens) and HomeActivity's
    // static copy (the "payment" + "expiry_date" + Date.after method that stores it; default true
    // in <clinit>). Enable Premium forces both to false. The purchase screen reads the prefs
    // itself and still shows no plan; FanCode stream entitlements are server-side. Disable ads
    // is a library-level backstop: GMA next-gen load entry points (kept classes, matched by
    // load-callback parameter), Parth Ads AdRequest (CREX's own exchange) public loads and Meta
    // AdView.loadAd. No PairIP, no signature check (libnative-lib.so only returns config
    // strings); Play Integrity is only used by Cashfree, AppsFlyer, Firebase Auth and reCAPTCHA.
    // Emulator (logged out, both patches): stock showed test-fill ads (in-feed native, bottom
    // banner, an interstitial while switching tabs); the patched app showed none on Home, the
    // live match screen, Series, stats/points table, Social and Videos, with no gaps, and no
    // interstitial over 12 tab switches. The pinned floating score starts the Premium service
    // with no ad; no crashes. The menu's "CREX PREMIUM" row opens a login sheet. Live/Upcoming
    // tabs and the quiz were not reached.
    val COMPATIBILITY_CREX = Compatibility(
        name = "CREX",
        packageName = "in.cricketexchange.app.cricketexchange",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x262626,
        targets = listOf(AppTarget(version = "26.10.06", versionCode = 667))
    )
}
