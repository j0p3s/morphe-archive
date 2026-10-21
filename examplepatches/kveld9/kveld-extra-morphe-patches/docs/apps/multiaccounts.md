# Multiple Accounts Patches Guide

Technical documentation and patch catalog for Multiple Accounts (com.excelliance.multiaccounts) on Android.

---

## 1. Target & Compatibility

| Property | Value |
| :--- | :--- |
| **Target App** | Multiple Accounts |
| **Package Name** | `com.excelliance.multiaccounts` |
| **Target Version** | `5.8.2` |
| **Target Package Format** | APK (standalone) |
| **Primary Architecture** | `arm64-v8a` |
| **Download Target** | [APKMirror](https://www.apkmirror.com/apk/excelliance-technology/multiple-accounts-dual-accounts/) (`com.excelliance.multiaccounts` v5.8.2 arm64-v8a versionCode 672) |
| **Authoritative Source** | APKMirror |

---

## 2. Applied Patches Catalog

| Patch Name | Typology | Default State | Dependencies | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Multiaccounts Telemetry Manifest Purge** | `resourcePatch` | `false` (Opt-in) | None | Strips advertising permissions, disables ad network providers, activities, services, and receivers, prunes telemetry registrars, and injects opt-out metadata flags in AndroidManifest.xml. |
| **Block Telemetry & Trackers** | `bytecodePatch` | `true` (Enabled) | `Multiaccounts Telemetry Manifest Purge` | Neutralizes in-house statistics, ByteDance AppLog, AppLovin MAX, Google Measurement, AppMetrica, Tenjin, IronSource, and Pangle ad/telemetry dispatchers, redirects isolated tracking host literals to 0.0.0.0, disables ad and tracking manifest components, and injects opt-out metadata flags. |
| **Multiaccounts Privacy Setup** | `resourcePatch` | `false` (Opt-in) | None | Configures network_security_config.xml to trust user CA certificates for TLS inspection while preserving cleartext traffic. |
| **Privacy Controls** | `bytecodePatch` | `false` (Opt-in) | `Multiaccounts Privacy Setup` | Neutralizes clipboard snooping in ByteDance AppLog attribution and disables OkHttp certificate pinning for network traffic auditing. |
| **Sideload Compatibility** | `bytecodePatch` | `true` (Enabled) | None | Ensures compatibility for sideloaded installations by spoofing the official APK signature certificate for self-verification and neutralizing launch-blocking Google Play in-app update checks. |
| **UX Declutter** | `bytecodePatch` | `true` (Enabled) | None | Removes splash delays, short-circuits cloned-app launch ad gates, hides main screen promo banners, and suppresses rating prompts. |
| **Multiaccounts Debloat (Setup)** | `resourcePatch` | `false` (Opt-in) | None | Disables non-functional ad-network WorkManager services and receivers, removes WorkManagerInitializer startup metadata, and strips RECEIVE_BOOT_COMPLETED in AndroidManifest.xml while strictly preserving clone container services. |
| **Stop Background Wakeups** | `bytecodePatch` | `false` (Opt-in) | `Multiaccounts Debloat (Setup)` | Neutralizes ad-SDK WorkManager background task execution to eliminate background wakeups and battery drain. Container virtualization processes, push notifications, and clone keep-alive services are strictly preserved. |

---

## 3. Deep Technical Breakdown

### Target Architecture & Cloned Space Overview

Multiple Accounts (2Accounts dual-space clone) is a virtualization container application enabling dual and multi-instance execution of installed Android applications within isolated sandboxed processes.

### A. Manifest Purge Layer (`resourcePatch`)

1. **Multiaccounts Telemetry Manifest Purge**:
   - **Objective**: Prevent ad network auto-initialization, advertising ID collection, ad rendering activities, background measurement services, and telemetry scheduling in `AndroidManifest.xml`.
   - **How**:
     - **Permissions**: Strips `com.google.android.gms.permission.AD_ID`, `com.google.android.gms.permission.AD_ID_NOTIFICATION`, `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE`, `BIND_GET_INSTALL_REFERRER_SERVICE`, and `android.permission.ACCESS_ADSERVICES_*` (`ATTRIBUTION`, `AD_ID`, `TOPICS`).
     - **Providers (11 disabled)**: Disables `com.applovin.sdk.AppLovinInitProvider`, `com.yandex.mobile.ads.core.initializer.MobileAdsInitializeProvider`, `com.yandex.mobile.ads.features.debugpanel.data.local.DebugPanelFileProvider`, `io.appmetrica.analytics.internal.PreloadInfoContentProvider`, `com.google.android.gms.ads.MobileAdsInitProvider`, `com.vungle.ads.VungleProvider`, `com.ironsource.lifecycle.IronsourceLifecycleProvider`, `com.ironsource.lifecycle.LevelPlayActivityLifecycleProvider`, `com.mbridge.msdk.config.component.status.MBComponentLifecycleProvider`, `com.secmtp.sdk.core.api.ATInitializationProvider`, and `com.smartdigimkt.sdk.api.SDMInitializationProvider`; `com.android.staticslio.StaticDataContentProvider` is preserved because it serves the startup-queried `<package>.staticslioprovider` authority (disabling it kills the process).
     - **Services (8 disabled)**: Disables AppLovin `FullscreenAdService` and `AppRestartDuringAdDetectionService`, Google `AppMeasurementService`, `AppMeasurementJobService`, and `AdService`, Yandex `AppMetricaService`, and Google DataTransport `TransportBackendDiscovery` and `JobInfoSchedulerService`.
     - **Receivers (4 disabled)**: Disables Google `AppMeasurementReceiver`, DataTransport `AlarmManagerSchedulerBroadcastReceiver`, ByteDance AppLog `Collector`, and Mintegral `NetWorkChangeReceiver`.
     - **Activities (81 disabled)**: Disables all interstitial, rewarded, banner, debugger, and mediation activities across AppLovin MAX (20 activities including `MaxDebugger*`, `MaxCreativeDebugger*`), Yandex (`AdActivity`, `IntegrationInspectorActivity`), Unity Ads / IronSource (10 activities), Verve / HyprMX (7 activities), Google Ads (3 activities), ByteDance / Pangle (18 activities), Vungle (`VungleActivity`), Mintegral (4 activities), TopOn / Secmtp / SmartDigiMkt (14 activities), and app-internal splash ad activities (`TopOnAdSplashActivity`, `ApiAdSplashActivity`).
     - **Component Discovery**: Prunes `AnalyticsConnectorRegistrar` from `com.google.firebase.components.ComponentDiscoveryService` while preserving Firebase Messaging, RemoteConfig, Abt, and Installations registrars.
     - **Opt-Out Metadata**: Injects declarative opt-out flags for Firebase Analytics, Crashlytics, Performance, Google Analytics ADID / personalization, Yandex Mobile Ads automatic initialization (`false`), MyTarget autoInitMode (`0`), and Google Ads initialization optimization (`false`).
     - **Preservation**: Core guest container virtualization components (`com.excelliance.kxqp.platform.proxy.*`), app-owned push (`MyFirebaseMessagingService`), Firebase push infra (`FirebaseMessagingService`, `FirebaseInstanceIdReceiver`, `FirebaseInitProvider`), Google Play Billing (`ProxyBillingActivity`, `ProxyBillingActivityV2`), and Zendesk support components (`DeepLinkingBroadcastReceiver`, `SupportSdkStartupProvider`, etc.) are strictly preserved.

2. **Multiaccounts Privacy Setup**:
   - **Objective**: Configure network security policy to trust user CA certificates for TLS proxy auditing without breaking container IPC or cleartext communication.
   - **How**: Injects `<trust-anchors>` with `<certificates src="system" />` and `<certificates src="user" />` into `<base-config>` inside `res/xml/network_security_config.xml` while strictly preserving `cleartextTrafficPermitted="true"` for guest application and local proxy compatibility.

### B. Bytecode Dispatcher Neutralization Layer (`bytecodePatch`)

1. **Block Telemetry & Trackers**:
   - **Objective**: Neutralize active runtime telemetry, analytics, and ad measurement pipelines directly at Dalvik bytecode entrypoints and sinkhole isolated host literals without breaking asynchronous promises or application lifecycles.
   - **How**:
     - **In-House Statistics (`com.android.staticslio.StatisticsManager`)**: 16 void dispatchers stubbed with early `return-void` (`uploadData`, `uploadBeanImmediateInternal`, `startImmediateUploadData`, `startNextUploadDataTask`, `uploadAllData`, `uploadBean`, `uploadBeanImmediate`, `uploadStaticData`, `upLoadBasicInfoStaticData`).
     - **ByteDance AppLog (`com.bytedance.applog.AppLog`)**: 21 void event/session tracking methods stubbed with `return-void` (`onEventV3`, `onMiscEvent`, `trackClick`, `trackPage`, `startDurationEvent`, `stopDurationEvent`, `pauseDurationEvent`, `resumeDurationEvent`, `profileSet`, `profileSetOnce`, `profileIncrement`, `profileAppend`, `flush`).
     - **TikTok Business SDK (`com.tiktok.TikTokBusinessSdk`)**: 12 void tracking dispatchers stubbed with `return-void` (`initializeSdk`, `startTrack`, `trackTTEvent`, `trackEvent`, `trackGooglePlayPurchase`, `flush`). Initialized by wrapper `com.excelliance.kxqp.util.ql` to emit event telemetries to `analytics.us.tiktok.com`; the first 21 AppLog-facade stubs missed it because it resides in an independent namespace/package.
     - **AppLovin MAX (`com.applovin.impl.sdk.EventServiceImpl`, `com.applovin.sdk.AppLovinSdk`)**: 7 event dispatchers stubbed with `return-void` (`trackEvent`, `trackEventSynchronously`, `trackCheckout`, `trackInAppPurchase`, `maybeTrackAppOpenEvent`), and 3 mediation debugger entrypoints stubbed (`showCreativeDebugger`, `showMediationDebugger`).
     - **Google Measurement (`com.google.android.gms.measurement.AppMeasurement`)**: 2 void event dispatchers stubbed (`logEventInternal`, `logEventInternalNoInterceptor`).
     - **Yandex AppMetrica (`io.appmetrica.analytics.AppMetrica`)**: 16 void event, revenue, attribution, and error reporters stubbed with `return-void` (`reportEvent`, `reportError`, `reportUnhandledException`, `reportAppOpen`, `reportRevenue`, `reportAdRevenue`, `reportExternalAdRevenue`, `reportExternalAttribution`, `reportUserProfile`, `sendEventsBuffer`).
     - **Tenjin Attribution SDK (`com.tenjin.android.TenjinSDK`)**: 32 void connection, event, ad impression, and transaction tracking dispatchers stubbed with `return-void` (`connect`, `eventWithName`, `eventWithNameAndValue`, `eventAdImpression*`, `transaction*`). Direction-sensitive consent/opt-out setters (`optOut`, `optIn`) are strictly excluded per Section 1.E.
     - **Yandex Mobile Ads (`com.yandex.mobile.ads.common.MobileAds`)**: Logging and debug panel display stubbed (`enableLogging`, `showDebugPanel`).
     - **IronSource Mediation (`com.ironsource.mediationsdk.IronSource`)**: Ad revenue reporter stubbed (`setAdRevenueData`).
     - **Pangle PAGSdk (`com.bytedance.sdk.openadsdk.api.init.PAGSdk`)**: Ad revenue reporter stubbed (`setAdRevenue`).
     - **DNS-over-HTTPS & In-House Endpoint Host Redirection**: Indexed string fingerprints redirect isolated host literals (`log.multispaces.net`, `ad.multispaces.net`) and telemetry URL hosts (`statis.multispaces.net`) to `0.0.0.0` while strictly preserving scheme, path, and URL structure for URI parser stability.

2. **Privacy Controls**:
   - **Objective**: Neutralize active first-party behavioral data collection (clipboard snooping) and eliminate OkHttp certificate pinning to enable privacy traffic inspection.
   - **How**:
     - **Clipboard Snooping Neutralization (`com.bytedance.applog.alink.util.LinkUtils`)**: Stubs `getParamFromClipboard(Context)` to return `null` via `replaceWithReturnNull()`. Eliminates background clipboard inspection for attribution links (`datatracer:`) on application launch while leaving system UI text operations and `AppCompatReceiveContentHelper` intact.
     - **OkHttp Certificate Pinning Neutralization (`okhttp3.CertificatePinner`)**: Stubs all certificate pinner verification overloads with `replaceWithReturnVoid()` (`check(String, List)`, `check(String, Certificate[])`, and Kotlin-mangled `check$okhttp(String, Function0)` with relocated signature `check$okhttp(String, Lie/a;)`). Prevents `SSLPeerUnverifiedException` when routing application traffic through a local inspection proxy (mitmproxy, Charles) with user CA certificates.
   - **Universal Classification & Non-Applicable Surfaces**:
     - **Remove Permissions**: `UNIVERSAL_OK`. The host container declares sensitive permissions (e.g. `CAMERA`, `RECORD_AUDIO`, `READ_CONTACTS`, `ACCESS_FINE_LOCATION`) needed by cloned guest applications. The universal `Remove Permissions` patch (`UniversalPrivacyPermissionsPatch`) operates cleanly via manifest stripping if the user opts in; no dedicated bytecode check bypass is required.
     - **Go Fully Offline**: `NOT_APPLICABLE`. Fails the Offline Applicability Gate because Multiple Accounts is a virtualization container whose cloned guest applications (e.g. messaging, social feeds) depend on internet connectivity; stripping `INTERNET` host-wide breaks guest process sockets.
     - **Allow Screen Capture / Screenshots**: `UNIVERSAL_OK`. Audited across 72 capture guard hits; no app-own activities set `FLAG_SECURE` or `setSecure(true)`. Window flag call sites exist solely in ad SDK activities (already suppressed by Telemetry Manifest Purge).
     - **Lock Screen Brightness / Force Screen Timeout**: `UNIVERSAL_OK`. Universal-only patches; no custom brightness or screen timeout overrides exist in app bytecode.
     - **Share-Link Tracking Stripping**: `UNIVERSAL_OK` / `TRANSITIVELY_DEAD`. The single `gclid=%s` measurement hit resides in GMS measurement (`zzkx`), neutralized by the telemetry patch; app-own `AppealActivity` email builder does not append tracking query parameters.
     - **Identifiers & Telephony**: 46 identifier and 93 telephony hits audited. Ad SDK callers are `TRANSITIVELY_DEAD`; app-own `la` calls GMS `AdvertisingIdClient` and `AppSetIdClient` via coroutine suspend lambdas/callbacks; `getInstalledPackages` is virtualization-critical for selecting clone targets.
     - **Sensors, Contacts, Battery**: Sensor listeners and battery receivers reside exclusively in ad SDKs and AndroidX media/workmanager; contacts URI reference in Picasso image handler is passive; all ad SDK collectors are `TRANSITIVELY_DEAD`.

### C. Sideload & Compatibility Bypass Layer (`bytecodePatch`)

1. **Sideload Compatibility**:
   - **Objective**: Ensure modified and sideloaded APKMirror builds install, launch, and execute cleanly outside Google Play, withstand re-signing without client-side signature integrity failures, and prevent forced store updates from blocking application launch.
   - **How**:
     - **Signature Self-Query Spoofing (`com.excelliance.kxqp.util.g4.h`)**: `g4.h(Context)` reads `PackageManager.getPackageInfo(..., 64).signatures[0].toCharsString()` truncated to 128 characters. When repackaged and re-signed by Morphe or custom keystores, this query returns foreign signature strings. The method is replaced with `const-string v0, "$ORIGINAL_SIGNATURE_HEX_128"` returning the official ZhuoMeng release certificate prefix (`30820271308201daa0030201020204520332c2...`), ensuring signature consumers (`g4.g`, `mm.g`) receive the official certificate bytes.
     - **Play Core In-App Update Gate Suppression (`com.excelliance.kxqp.util.jm.H`)**: `jm.H(Activity)` is the startup entrypoint called by `ih.e(Activity)` that queries Google Play's `IAppUpdateService` (`b5/*`). On sideloaded builds without Google Play services, update queries fail or trigger modal forced-update dialogs (`jm.K`) whose exit button calls `activity.finish()`. `jm.H(Activity)` is stubbed with `return-void`, neutralizing the launch update check.
     - **Update Dialog Dispatcher Defense-in-Depth (`com.excelliance.kxqp.util.jm.t`)**: `jm.t(Activity, boolean)` builds and displays the blocking update dialog. It is stubbed with `return-void` as a secondary guard to prevent modal display if invoked from any alternative path.

### D. Entitlement & Feature Flag Three-Yes Feasibility Evaluation

| Method Signature | Location | Three-Yes Criteria Evaluation | Verdict |
| :--- | :--- | :--- | :--- |
| `Lp1/l;->B(Landroid/content/Context;)V` | `classes3.dex:181-196` | **Q1 (Local decision)**: NO. `p1.l` is `BiManager` (ByteDance AppLog analytics manager). Method `B` is a telemetry sink that compiles tracking parameters `Pair("is_vip", if (fn.f12512a.i(context)) "是" else "否")` and logs them via `AppLog.setHeaderInfo(...)`. It performs no authorization and gates no feature.<br>**Q2 (Local flow)**: NO. VIP entitlements (multi-account container quotas, cloud sync) require backend grant from `https://api.multispaces.net/`.<br>**Q3 (No credential/receipt forgery)**: NO. Forcing this flag would forge analytics state while remaining unentitled at the backend. | **FAILS (No / No / No)**<br>Status: `TRANSITIVELY_INERT` / `NOT_APPLICABLE`<br>Action: PRESERVED (no normalization). |
| `Lp1/l;->w(Landroid/app/Application;)Ltd/j0;` | `classes3.dex:401-405` | **Q1 (Local decision)**: NO. Observer callback on `DataStore.M2()` that re-emits `"is_vip"` to `AppLog` when VIP status changes.<br>**Q2 (Local flow)**: NO. Server-authoritative VIP status.<br>**Q3 (No credential/receipt forgery)**: NO. Forges telemetry reporting. | **FAILS (No / No / No)**<br>Status: `TRANSITIVELY_INERT` / `NOT_APPLICABLE`<br>Action: PRESERVED (no normalization). |

*VIP Architecture Note*:
The true VIP evaluation in Multiple Accounts runs through `com.excelliance.kxqp.util.fn` (`VipManager.kt`) which delegates to native methods in `com.excelliance.kxqp.util.vf` (`vf.g`, `vf.b`, `vf.c`, etc.). These native methods verify subscription data populated by server login (`LoginResponse` via `c.x` in `SpUserInfo.kt`) and Google Play Billing receipt tokens. Client-side forgery of `fn` or `vf` would cause container desynchronization and backend API rejection (symptom index row 1: server-side authority). Consequently, VIP normalization is strictly out of scope per Three-Yes rule and repo invariants.

### E. Comprehensive Compatibility Surface Audit Matrix

| Surface Area | Hits (Total/Classes) | Representative Location | Classification | Rationale & Architectural Handling |
| :--- | :--- | :--- | :--- | :--- |
| **Re-signing tolerance** (`signature-check`) | 70 / 17 | `classes4.dex` `Lcom/excelliance/kxqp/util/g4;->h(Landroid/content/Context;)Ljava/lang/String;` | `NORMALIZED` | Single app-own signature query reading `signatures[0]`. Pinned to official ZhuoMeng release certificate hex to prevent foreign-key re-signing rejections. 69 other hits reside in AndroidX framework and disabled ad SDKs (`TRANSITIVELY_INERT`). |
| **In-app update** (`update-gate`) | 3 / 3 | `classes4.dex` `Lcom/excelliance/kxqp/util/jm;->H(Landroid/app/Activity;)V` & `jm;->t(Landroid/app/Activity;Z)V` | `NORMALIZED` | Play Core `IAppUpdateService` query (`b5/*`) and modal update dialog (`jm.K`) stubbed with `return-void` to prevent launch blocks on sideloaded APKMirror installs lacking Play services. |
| **Store navigation** (`store-redirect`) | 34 / 9 | `classes4.dex` `Lcom/excelliance/kxqp/util/nd;->f(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V` | `PRESERVED` | Single app-own store redirect (`market://details?id=`) in `MultiAppStatusUtil` is user-initiated when user taps "Install Now" for an uninstalled cloned host app. Preserved per Rule. 33 other hits are ad SDK click redirects (`TRANSITIVELY_INERT`). |
| **Billing boundary** (`billing-boundary`) | 38 / 4 | `classes3.dex` `La2/a0;` (BillingClient wrapper), `AndroidManifest.xml` `ProxyBillingActivity`, `ProxyBillingActivityV2` | `PRESERVED` | Google Play Billing client wrappers and manifest activities strictly preserved unchanged without receipt forgery. |
| **Feature flag / Entitlement** (`entitlement`) | 4 / 3 | `classes3.dex` `Lp1/l;->B` & `Lp1/l;->w` (`is_vip`), `classes5.dex` `AcquisitionType` | `NOT_APPLICABLE` | Evaluated under Three-Yes test: `p1.l` is a telemetry sink, not an access gate. Real VIP is server-authoritative (`LoginResponse`). Unity Ads enum is third-party metadata. |
| **Installer identity** (`installer-check`) | 17 / 15 | `classes3.dex` `Lcom/applovin/impl/sdk/m;->a`, `classes5.dex` `Lcom/google/android/gms/measurement/internal/zzgi;->zzf` | `TRANSITIVELY_INERT` | 0 app-own installer checks. All 17 hits reside in third-party ad/analytics SDKs (AppLovin, Pangle, Google Measurement, IronSource, MBridge, Secmtp, Singular, Unity, AppMetrica) neutralized by telemetry patches. |
| **Root & emulator detection** (`root-emulator`) | 55 / 16 | `classes3.dex` `Lcom/bytedance/sdk/openadsdk/utils/ls;->wai`, `classes6.dex` `Lcom/secmtp/sdk/core/common/vv/vu;-><clinit>` | `TRANSITIVELY_INERT` | 0 app-own root/emulator checks. Hits are in third-party ad SDKs or false-positive pattern matches (`java.time.d.FRIDAY`, `PatternsCompat`). Ordinary physical devices (Redmi Note 5) run unblocked. |
| **Debugging detection** (`debug-check`) | 9 / 8 | `classes5.dex` `Lcom/ironsource/a;->run`, `classes8.dex` `Lio/appmetrica/analytics/impl/d;->run` | `TRANSITIVELY_INERT` | 0 app-own debug checks. All 9 hits belong to ad/measurement SDKs (IronSource, MBridge, AppMetrica, Pangle, Secmtp, Unity). |
| **APK self-read** (`apk-self-read`) | 18 / 7 | `classes4.dex` `Lcom/excelliance/kxqp/util/dl;->w`, `classes9.dex` `Lcom/excelliance/kxqp/nat/b;->a` | `PRESERVED` | App-own `ApplicationInfo.sourceDir` references belong to the dual-space virtualization engine (reading host and guest APK file paths to clone components and resources). Preserved for container functionality. |
| **Zero-hit anchors** (`login-gate`, `shared-user`, `pairip`, `licensing`, `integrity-api`) | 0 / 0 | N/A | `NOT_APPLICABLE` | Verified 0 hits across all DEX files and `AndroidManifest.xml` during reconnaissance sweep. No code required. |

### F. UX Declutter Layer (`bytecodePatch`)

1. **UX Declutter**:
   - **Objective**: Eliminate artificial cold-start delays, ad gating stalls, native promotional banners, rating prompts, and first-run consent dialogs without compromising guest container execution lifecycles, push notifications, or Google Play billing.
   - **How**:
     - **Splash Delay Cut (`dhy60gm84keol.u()V`)**: Launcher activity posted a 1000ms delay timer via `ml.s` before calling `dhy60gm84keol.v(p0)` to launch `cdp45zj40leka` and finish self. Replaced with immediate static invocation `dhy60gm84keol.v(p0)` followed by `return-void` (`REMOVED_DELAY`).
     - **Cloned-App Launch Acceleration (`i1.v1.k0`)**: `AppStartUtil` (`m1.java:541`) invokes `k0(Activity, int, Function0)` when launching guest applications. Instead of initiating splash ad mediation or network stalls, `k0` immediately invokes `onNext.invoke()`, guaranteeing instant clone launch without ad gating (`REMOVED_DELAY`).
     - **Main Screen Splash Gate (`i1.v1.n0`)**: `zoi77gy37zzof.I0()` initializes the main fragment workspace via `v1.n0(Context, Function0, Function0, Function0)`. Hooked to immediately invoke `onNext.invoke()`, directly triggering `zoi77gy37zzof.J0 -> H0()` (`REMOVED_DELAY`).
     - **Global Ad Gate Short-Circuit (`DataStore.O1()Z`)**: Forces `canRequestAds` to return `false` (`replaceWithReturnBoolean(false)`). Bypasses ad loading in `AdManagerOfSplash.e0()` (which immediately calls `g0(onNext, activity)`) and suppresses ad prefetching across `i1.k`, `i1.v1`, `i1.c4`, and `qj` (`SKIPPED`).
     - **Rating & Review Prompt Suppression (`j9.o(Activity)Z`)**: Suppresses 5-star rating dialog builder (`dialog_grade`) triggered from home screen interactions by returning `false` (`SKIPPED`).
     - **Native Promo Banner Elimination (`NativeBanner`)**: Embedded in `zoi77gy37zzof` (`R.id.nativeBanner`). Method `m(Z)V` forced to set `View.GONE` (`0x8`). Data ingestion methods `l(List)V` and `z(List)V`, along with auto-switch routines `s()V`, `t()V`, and `u()V`, are stubbed with `return-void` (`SKIPPED`).
     - **First-Run Privacy Consent Wizard Bypass (`tg.d`)**: `com.excelliance.kxqp.util.tg.d(Context, tg$a)` displays `dialog_privacy_413`. Hooked to immediately invoke `callback.b()`, replicating the state where privacy consent was previously accepted (`SKIPPED`).
     - **Transitively Dead Surfaces**:
       - `com.verve.ng.sdk.interstitial.activity.MraidInterstitialActivity` & `VastInterstitialActivity`: Disabled in manifest by telemetry patch (`BLOCKED_ACTIVITIES`), 0 first-party invocations (`TRANSITIVELY_DEAD`).
       - `com.excelliance.kxqp.ads.topon.TopOnAdSplashActivity` & `ApiAdSplashActivity`: Disabled in manifest by telemetry patch (`BLOCKED_ACTIVITIES`), and invocation routes via `AdManagerOfSplash` neutralized (`TRANSITIVELY_DEAD`).

### G. Background Wakeup Elimination Layer (`resourcePatch` + `bytecodePatch`)

1. **Multiaccounts Debloat (Setup)**:
   - **Objective**: Prevent background boot wakeups and periodic task scheduling triggered by third-party ad SDK WorkManager services and receivers in `AndroidManifest.xml`.
   - **How**:
     - **Permissions**: Strips `android.permission.RECEIVE_BOOT_COMPLETED` (line 89) to eliminate device reboot wakeups.
     - **Services (3 disabled)**: Disables `androidx.work.impl.background.systemalarm.SystemAlarmService`, `androidx.work.impl.background.systemjob.SystemJobService`, and `androidx.work.impl.foreground.SystemForegroundService` (lines 6010-6025).
     - **Receivers (8 disabled)**: Disables `androidx.work.impl.utils.ForceStopRunnable$BroadcastReceiver`, `androidx.work.impl.background.systemalarm.ConstraintProxy$BatteryChargingProxy`, `ConstraintProxy$BatteryNotLowProxy`, `ConstraintProxy$StorageNotLowProxy`, `ConstraintProxy$NetworkStateProxy`, `RescheduleReceiver`, `ConstraintProxyUpdateReceiver`, and `DiagnosticsReceiver` (lines 6026-6097).
     - **Startup Initializer**: Removes `androidx.work.WorkManagerInitializer` `<meta-data>` entry from `androidx.startup.InitializationProvider` (line 5916).
     - **Preservation**: Strictly preserves `android.permission.WAKE_LOCK` (line 13), `com.excelliance.kxqp.platform.TrampolineService` (and `:lbcore` isolated process companions `EmptyService`, `JobProxyService`, `NotificationListenerService`, `TrampolineReceiver`, `TrampolineProvider`), `com.excelliance.kxqp.SmtServService` (foregroundServiceType `0x40000000`, lines 625-628), and all guest proxy services/receivers (`com.excelliance.kxqp.platform.proxy.*`) across all plugin slots (P00-P09).

2. **Stop Background Wakeups**:
   - **Objective**: Neutralize WorkManager runtime initialization in Dalvik bytecode, ensuring that any surviving startup code returns immediately without initializing schedulers or background databases.
   - **How**:
     - **Bytecode Stubs**: Stubs `Landroidx/work/WorkManagerInitializer;->create(Landroid/content/Context;)Landroidx/work/WorkManager;` (method@2955) and its synthetic bridge `create(Landroid/content/Context;)Ljava/lang/Object;` with `replaceWithReturnNull()`, returning `null` immediately.
     - **Eliminated Ad-SDK Workers**: Neutralizes Google Ads offline buffering workers (`com.google.android.gms.ads.internal.offline.buffering.OfflineNotificationPoster`, `OfflinePingSender`) and Unity Ads background job (`com.unity3d.ads.core.domain.work.UniversalRequestJob`), which are the only WorkManager workers registered in the APK.

---

## 4. Behavioral Hazards Warning

1. **Dual-Space and Process Isolation**:
   Multiple Accounts operates as a dual-space and process-level clone container where secondary accounts run in isolated guest processes. Patching bytecode or manifest components must preserve guest container virtualization lifecycles:
   - Must not break cloned-app launch flows or process forks.
   - Must not disrupt push notification dispatch mechanisms within cloned app containers.
   - Must keep in-app billing paths functional where required.

2. **Push Notification Infrastructure and FirebaseInitProvider Preservation**:
   `com.google.firebase.provider.FirebaseInitProvider`, `com.google.firebase.messaging.FirebaseMessagingService`, and `com.google.firebase.iid.FirebaseInstanceIdReceiver` must remain enabled. Multiple Accounts relies on Firebase initialization for dual-space notification multiplexing and synchronous app-state services. Telemetry suppression is achieved through manifest component disabling of analytics services, removal of `AnalyticsConnectorRegistrar`, declarative opt-out flags, and Dalvik bytecode stubs without altering core Firebase runtime initialization.

3. **Certificate Pinning & User CA Scope in Guest Containers**:
   Enabling user CA trust and neutralizing `CertificatePinner` allows full HTTPS inspection of the Multiple Accounts host process. However, secondary applications cloned inside the container run with their own sandboxed bytecode and network configurations; guest application pinning is not altered by host-level patches.

4. **First-Party Behavioral Privacy & Guest Process Isolation**:
   Neutralizing clipboard snooping in `LinkUtils` stops attribution harvesting by the host application on launch. Cloned guest applications still possess direct platform clipboard access within their respective sandboxes according to standard Android permissions.

5. **Re-Signing Tolerance & Host Signature Spoofing**:
   `BaseGameUtil.h(Context)` (`g4.h`) is pinned to return the official ZhuoMeng release signature string. Cloned guest applications execute inside isolated sandbox containers and query their own package signatures through the container proxy, not `g4.h`. This hook affects only Multiple Accounts' own client self-verification and prevents runtime crashes when the host APK is signed with test keys.

6. **Sideload Update Suppression vs Cloned App Store Navigation**:
   Neutralizing `jm.H(Activity)` and `jm.t(Activity, boolean)` skips Play Core in-app update checks for Multiple Accounts itself. Conversely, `MultiAppStatusUtil.f` (`nd.f`) is intentionally preserved: if a user uninstalls a cloned app from the host Android system, Multiple Accounts prompts the user and allows opening the store to re-download the target app without breaking user-initiated recovery workflows.

7. **Asynchronous Callback Invocation Invariant in Ad Gates**:
   Methods accepting completion callbacks (specifically `v1.k0(Activity, int, Function0)`, `v1.n0(Context, Function0, Function0, Function0)`, and `tg.d(Context, tg$a)`) MUST NEVER be stubbed with `return-void`. Doing so permanently hangs UI transitions, halts cold-start loading, or prevents cloned applications from ever launching. The declutter implementation strictly invokes the completion callback (`invoke-interface`) before returning `void`.

8. **Container Virtualization & Background Service Preservation**:
   Multiple Accounts relies on foreground services (`SmtServService`, `foregroundServiceType 0x40000000`) and the `:lbcore` isolated process (`TrampolineService`, `JobProxyService`, `NotificationListenerService`) for clone process lifecycles, keep-alive, IPC proxying, and push notification routing to cloned applications. These components must never be stripped or disabled. WorkManager is exclusively leveraged by ad networks (Google Ads offline buffering, Unity Ads UniversalRequestJob) and is safely neutralized without affecting the virtualization container.

---

## 5. Canonical Universal Patches

Generic resource slimming and device optimization patches are provided by universal patches in the canonical collection [kveld-morphe-patches](https://github.com/kveld9/kveld-morphe-patches). Comprehensive status of universal patches for Multiple Accounts (v5.8.2):

| Universal Patch / Debloat Axis | Classification | Status & Concrete Technical Rationale |
| :--- | :--- | :--- |
| **Remove Extra Languages** (`LocaleResourceSlimmerPatch`) | `UNIVERSAL_OK` | Standard Android resource tables with 11 language qualifiers (`th`, `hi`, `vi`, `in`, `ko`, `ar`, `fr`, `tr`, `es`, `pt`, `ru` + default English) decoded to standard `res/values-*/` and localized drawable directories; cleanly stripped by universal patch. |
| **Remove Unused Screen Sizes** (`DpiResourceSlimmerPatch`) | `UNIVERSAL_OK` | Standalone nodpi APK with standard density buckets (`ldpi`, `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`); launcher icons `@mipmap/ic_launcher` and `nodpi`/`anydpi` assets are safely protected by universal patch. |
| **ApkJunkCleaner** (`ApkJunkCleanerPatch`) | `UNIVERSAL_OK` | `DebugProbesKt.bin` at APK root is pruned by universal patch; `kotlin/*.kotlin_builtins` (9 files) are load-bearing runtime compiler descriptors used by `kotlin-reflect` (`lg.a.q:(Lwf/c;)Ljava/lang/String;` in classes8.dex) and are strictly preserved (`PRESERVED_FUNCTIONAL`). |
| **Stop Background Wakeups** (`BackgroundSyncPurgePatch`) | `DEDICATED_NEEDED` | Universal `BackgroundSyncPurgePatch` does not prune `androidx.work.WorkManagerInitializer` from `androidx.startup.InitializationProvider` in `AndroidManifest.xml` (line 5916) and does not stub `WorkManagerInitializer.create` in Dalvik bytecode. Multiple Accounts also requires strict preservation of clone container services (`TrampolineService`, `SmtServService`) and `WAKE_LOCK`. Implemented via dedicated patch `Stop Background Wakeups`. |
| **Shrink PNG Images** (`PngOptimizerPatch`) | `UNIVERSAL_OK` | Standard PNG raster drawables in `res/` and `assets/`; chunk CRC validation and 9-patch preservation handled by universal patch. |
| **Shrink WebP Images** (`UniversalWebpOptimizerPatch`) | `UNIVERSAL_OK` | Standard WebP images (10 files); EXIF/XMP/ICCP metadata stripping and VP8X flag synchronization handled by universal patch. |
| **Remove Tracking Code** (native `.so` trimmer) | `NOT_APPLICABLE` | All 13 `.so` files in `lib/arm64-v8a/` have >=1 load-bearing reference or dynamic loader dependency across fused base DEX; zeroing is forbidden to prevent `UnsatisfiedLinkError` boot crashes (`PRESERVED_FUNCTIONAL`). |
| **Tutorial Videos / Editor Stickers** | `NOT_APPLICABLE` | No media assets present (`res/raw` is absent; `assets/supplierconfig.json` is 136 bytes negligible). No patch authored. |
