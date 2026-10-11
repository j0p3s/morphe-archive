# Instagram Patches Guide

Technical documentation and patch catalog for Instagram on Android.

---

## 1. Target & Compatibility

| Property | Value |
| :--- | :--- |
| **Target App** | Instagram |
| **Package Name** | `com.instagram.android` |
| **Target Version** | `447.0.0.55.81` |
| **Target Package Format** | APKM (Split APK Bundle) |
| **Authoritative Source** | [APKMirror](https://www.apkmirror.com/apk/instagram/instagram-instagram/instagram-447-0-0-55-81-release/) |

---

## 2. Applied Patches Catalog

| Patch Name | Typology | Default State | Dependencies | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Block Telemetry & Trackers** | `bytecodePatch` | `true` (Enabled) | `Instagram Telemetry Manifest Purge` | Neutralizes Facebook Analytics2, DataTransport, FDID/PhoneId providers, and strips AD_ID permissions. |
| **Instagram Telemetry Manifest Purge** | `resourcePatch` | `false` (Opt-in) | None | Strips tracking/advertising permissions and disables analytics services, providers, and receivers in AndroidManifest.xml. |
| **MLKit Vision Slimmer** | `resourcePatch` | `false` (Opt-in) | None | Disable MLKit component discovery and registrars. WARNING: this breaks in-app QR and barcode scanning. |
| **Block Crash & Diagnostics** | `bytecodePatch` | `false` (Opt-in) | `Instagram Crash & Diagnostics Manifest Purge` | Neutralizes crash-dump uploaders, Lacrima detector broadcasts, debug diagnostics, carrier preload trackers, and lead-ads entrypoints. |
| **AR Effects Slimmer** | `rawResourcePatch` | `false` (Opt-in) | None | Strips ARCore native binaries (libarcore_sdk_c.so, libarcore_sdk_jni.so) and AR effect/face tracking asset models. WARNING: Disables camera AR filters, 3D face effects, and body/face tracking. |
| **Breakpad Crash Reporter Slimmer** | `rawResourcePatch` | `false` (Opt-in) | None | Zeroes Google Breakpad native crash reporter binaries (libbreakpad.so, libbreakpad_cpp_helper.so) in-situ. WARNING: Native crash reporting is silenced. If the host application loads these libraries unconditionally without exception guards, startup crashes could occur. |

---

## 3. Deep Technical Breakdown

### A. Layer 1: Manifest Purge Layer (`resourcePatch`)

The resource patch executes declarative AST transformations on `AndroidManifest.xml` before DEX assembly:

1. **Permission Stripping**:
   - `com.google.android.gms.permission.AD_ID` (Google Play Services Advertising ID)
   - `android.permission.ACCESS_ADSERVICES_ATTRIBUTION` (Privacy Sandbox Attribution API)
   - `android.permission.ACCESS_ADSERVICES_AD_ID` (Android Privacy Sandbox Ad ID)
   - `android.permission.ACCESS_ADSERVICES_CUSTOM_AUDIENCE` (Privacy Sandbox Custom Audiences)
   - `android.permission.ACCESS_ADSERVICES_TOPICS` (Privacy Sandbox Topics API)
   - `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE` (Play Install Referrer)

2. **Component Disabling**:
   Disables analytics, tracking, measurement, and advertising components by setting `android:enabled="false"` and `android:exported="false"`:
   - **Providers**:
     - `com.instagram.common.analytics.fdidlite.AsyncInstagramFDIDLiteProvider`
     - `com.instagram.common.analytics.phoneid.AsyncInstagramPhoneIdProvider`
     - `com.instagram.contentprovider.InstallReferrerProvider`
   - **Services**:
     - `com.facebook.analytics2.fabric.onefabric.FFAlarmUploadJobService`
     - `com.facebook.analytics2.logger.GooglePlayUploadService`
     - `com.facebook.analytics2.logger.legacy.uploader.AlarmBasedUploadService`
     - `com.facebook.analytics2.logger.legacy.uploader.Analytics2UploadService`
     - `com.facebook.analytics2.logger.legacy.uploader.LollipopUploadService`
     - `com.facebook.analytics2.logger.service.LollipopUploadSafeService`
     - `com.google.android.datatransport.runtime.backends.TransportBackendDiscovery`
     - `com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService`
     - `com.google.android.gms.analytics.AnalyticsJobService`
     - `com.google.android.gms.analytics.AnalyticsService`
     - `com.meta.mfa.service.MfaCrossAppServiceImpl`
     - `com.meta.trusteddevice.service.TrustedDeviceFoundationServiceImpl`
     - `com.meta.wearable.acdc.sdk.service.ACDCRegistrationService`
   - **Receivers**:
     - `com.facebook.analytics2.fabric.onefabric.OneFabricUploadAlarmReceiver`
     - `com.facebook.analytics2.logger.legacy.uploader.HighPriUploadRetryReceiver`
     - `com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver`
     - `com.google.android.gms.analytics.AnalyticsReceiver`
     - `com.instagram.analytics.uploadscheduler.AnalyticsUploadAlarmReceiver`
     - `com.instagram.common.analytics.phoneid.InstagramPhoneIdRequestReceiver`
   - **Activities**:
     - `com.google.android.play.core.common.PlayCoreDialogWrapperActivity`

3. **Component Discovery Registrar Pruning**:
   Purges component discovery registrars declared under `<application>` matching:
   - `dynamicloading.DynamicLoadingRegistrar`
   - `mlkit`

### B. Layer 2: Bytecode Dispatcher Neutralization Layer (`bytecodePatch`)

The Dalvik bytecode layer neutralizes Facebook Analytics2 event dispatchers at execution time:

1. **Targeted Entrypoint Hooks**:
   - `Lcom/instagram/analytics/analytics2/IgAnalytics2TaskBasedUploader;->HZG(LX/KpT;LX/ArQ;LX/Av0;)V`
   - `Lcom/instagram/analytics/analytics2/IGAnalytics2SimpleUploader;->HZG(LX/KpT;LX/ArQ;LX/Av0;)V`
   - `Lcom/facebook/analytics2/logger/legacy/uploader/PrivacyControlledUploader;->HZG(LX/KpT;LX/ArQ;LX/Av0;)V`

2. **Transformation Strategy**:
   - Injects immediate `return-void` at instruction index 0.
   - Prevents background payload formatting, compression, and network transmission across both simple and task-based Facebook Analytics2 upload pipelines.

3. **Telemetry Standard**:
   - Emits structured diagnostic messages prefixed with `[Instagram Telemetry]`.
   - Reports exact count and names of neutralized dispatch methods.

### C. Layer 3: MLKit Vision Slimmer Layer (`resourcePatch`)

Strips Google MLKit discovery services and component metadata from `AndroidManifest.xml`:

1. **Targeted Discovery Service**:
   - `com.google.mlkit.common.internal.MlKitComponentDiscoveryService` disabled via `android:enabled="false"` and `android:exported="false"`.

2. **Registrar Removal**:
   - Traverses `<meta-data>` children inside the discovery service and removes all elements with `android:name` prefixed with `com.google.firebase.components:`.

3. **Telemetry Standard**:
   - Emits structured diagnostic messages prefixed with `[Instagram MLKit Slimmer]`.

### D. Layer 4: Crash & Diagnostics Layer (`bytecodePatch` / `resourcePatch`)

Chains `Instagram Crash & Diagnostics Manifest Purge` (`resourcePatch`) under `Block Crash & Diagnostics` (`bytecodePatch`):

1. **Purged Manifest Components (12 Targets)**:
   - **Services (3 targets)**:
     - `com.facebook.common.errorreporting.memory.service.jobschedulercompat.igsvc.DumperUploadService`: Background memory dump crash uploader.
     - `com.facebook.oxygen.preloads.sdk.firstparty.managedappcache.IsManagedAppCacheService`: Carrier preload managed app cache service.
     - `com.facebook.oxygen.preloads.sdk.firstparty.managedappcache.IsManagedAppCacheJobService`: Carrier preload JobScheduler dispatcher.
   - **Receivers (5 targets)**:
     - `com.facebook.errorreporting.lacrima.detector.broadcast.SecureShutdownBootBroadcastReceiver`: Dirty exit and crash loop detector across device shutdown and reboot.
     - `com.facebook.errorreporting.lacrima.detector.broadcast.ProtectedLockScreenBroadcastReceiver`: Screen on/off state tracker for Lacrima crash reporting.
     - `com.facebook.errorreporting.lacrima.detector.broadcast.PublicLockScreenBroadcastReceiver`: Lock screen state transition detector for crash tracking.
     - `com.facebook.oxygen.preloads.sdk.firstparty.managedappcache.IsManagedAppFlag`: Broadcast flag receiver for carrier managed app state.
     - `com.facebook.oxygen.preloads.sdk.firstparty.managedappcache.IsManagedAppReceiver`: Broadcast receiver responding to carrier app manager updates.
   - **Activities (3 targets)**:
     - `com.facebook.flipper.android.diagnostics.FlipperDiagnosticActivity`: Internal debug inspection and diagnostics activity.
     - `com.facebook.oxygen.preloads.integration.install.ig4a.xavfullscreen.XavFbInstallVariantActivity`: Carrier preload install variant screen.
     - `com.instagram.leadads.activity.LeadAdsActivity`: Interactive lead-generation ad form host activity.
   - **Application Startup Flags (1 target)**:
     - `com.facebook.oxygen.preloads.integration.appstartup.flag.FirstAppOpenFlag`: Receiver tracking initial app open for preload installers.

2. **Preserved Push, Wearable, and Core-API Exclusions (Invariants)**:
   - Functional push messaging and token services are strictly preserved: `com.instagram.notifications.push.fcm.IgFirebaseMessagingService`, `com.instagram.notifications.push.fcm.FcmListenerService`, and `com.facebook.pushlite.tokenprovider.fcm.PushLite*`.
   - Media streaming infrastructure is preserved: `com.facebook.wearable.mediastream.service.MediaStreamService`.
   - Core API and GraphQL endpoints (`graph.facebook.com`, `graph.instagram.com`) remain untouched.

3. **DEX-Hook Residual Note**:
   - Lacrima and Oxygen Preloads classes exist in the DEX string pool but method names and parameter types are heavily obfuscated. Speculative Dalvik AST signatures were rejected under the Zero-Zombie rule. Manifest-level disabling neutralizes all IPC, JobScheduler, and Broadcast entrypoints deterministically without DEX stability hazards.

### E. Layer 5: AR Effects Layer (`rawResourcePatch`)

Strips Google ARCore native binaries and in-app augmented reality model assets:

1. **Target Inventory & Disposition**:
   - **Pruned Assets (`file.delete()`, 12 targets)**:
     - `assets/app_modules/contents/arservicesforbodytracking.json` (469 bytes)
     - `assets/app_modules/contents/arservicesforexpressionfitting.json` (489 bytes)
     - `assets/app_modules/contents/arservicesforfacewave.json` (453 bytes)
     - `assets/app_modules/contents/arservicesforgenericml.json` (457 bytes)
     - `assets/app_modules/contents/arservicesforhairsegmentation.json` (485 bytes)
     - `assets/app_modules/contents/arservicesforhandtracking.json` (469 bytes)
     - `assets/app_modules/contents/arservicesforpersonsegmentation.json` (493 bytes)
     - `assets/app_modules/contents/arservicesforruntimerigmapping.json` (489 bytes)
     - `assets/app_modules/contents/arservicesforunifiedtargettracking.json` (505 bytes)
     - `assets/app_modules/contents/arservicesforwolf.json` (437 bytes)
     - `assets/app_modules/contents/arclassBenchmarks.json` (437 bytes)
     - `assets/ReferenceFaceShapeConstants/v01_high_end_face_compressed.bin` (77,932 bytes)
   - **Zeroed Native Binaries (`raf.setLength(0L)`, 2 targets)**:
     - `lib/arm64-v8a/libarcore_sdk_c.so` (68,120 bytes, SHA-256 `bd61b8bd62ed3029ecc965c85fc57d06aed8239a8f7f7897773cab22b5f162ea`)
     - `lib/arm64-v8a/libarcore_sdk_jni.so` (117,272 bytes, SHA-256 `bdc27b78a5360c80c3ae1a4da07f9792f8414b96a438b608e19b01fd083589ce`)

2. **Cheapest-First Guard Protocol**:
   - File existence and regular file validation (`file.exists() && file.isFile`).
   - Exact byte length check matching target specification (`file.length() == target.expectedSize`).
   - Bounds check asserting size is at least standard 4-byte ELF header size (`target.expectedSize >= ELF_MAGIC.size`).
   - Header magic verification (`header.contentEquals(byteArrayOf(0x7F, 'E', 'L', 'F'))`).
   - Full SHA-256 digest match against authoritative baseline hash.
   - On any guard failure, logs descriptive skip reason without throwing unhandled exceptions.

3. **Byte Accounting & Savings**:
   - Yields up to 83,115 bytes from pruned asset models and 185,392 bytes from zeroed native libraries, for a total reduction of 268,507 bytes (~0.26 MB).

### F. Layer 6: Breakpad Native Crash Reporter Layer (`rawResourcePatch`)

Zeroes Google Breakpad native crash reporting binaries in-situ:

1. **Target Inventory & Disposition**:
   - **Zeroed Native Binaries (`raf.setLength(0L)`, 2 targets)**:
     - `lib/arm64-v8a/libbreakpad.so` (482,808 bytes, SHA-256 `79c09b76e10262b50ec646779308129abe850f79a7d7afcb737c20688787e19b`)
     - `lib/arm64-v8a/libbreakpad_cpp_helper.so` (6,168 bytes, SHA-256 `4b9fe38f4d1def6701ac53e26b0da054e9d578424f1dbb493aa9935947b1dfd0`)

2. **Guard Protocol**:
   - Executes identical cheapest-first validation pipeline: existence, exact file length, ELF header bounds, 4-byte ELF magic assertion, and SHA-256 digest verification.

3. **Byte Accounting & Savings**:
   - Yields 488,976 bytes (~0.47 MB) of binary payload reduction in the arm64-v8a native folder.

4. **Startup-Load Hazard**:
   - If host application initialization routines load `libbreakpad.so` or `libbreakpad_cpp_helper.so` unconditionally via `System.loadLibrary` or `dlopen` without exception guards, an `UnsatisfiedLinkError` or `SIGSEGV` may occur at startup. This patch is strictly opt-in (`default = false`).

---

## 4. Behavioral Hazards Warning

Users and packagers must observe the following risks before applying patches:

1. **Opt-in Status**:
   `MLKit Vision Slimmer` (`instagramMlKitSlimmerPatch`), `Block Crash & Diagnostics` (`instagramCrashDiagnosticsBlockPatch`), `AR Effects Slimmer` (`instagramArEffectsSlimmerPatch`), and `Breakpad Crash Reporter Slimmer` (`instagramBreakpadSlimmerPatch`) are opt-in and disabled by default (`default = false`). `Instagram Telemetry Manifest Purge` (`instagramTelemetryResourcePatch`) and `Instagram Crash & Diagnostics Manifest Purge` (`instagramCrashDiagnosticsResourcePatch`) are also opt-in (`default = false`), chained as automatic dependencies under their respective bytecode patches.

2. **MLKit QR & Barcode Scanning Breakage**:
   Applying `MLKit Vision Slimmer` disables Google MLKit vision component discovery. While reducing initialization overhead and background component loading, this breaks in-app QR code and barcode scanning functionality within Instagram.

3. **Mandatory Backup & Verification**:
   Users must retain a clean backup copy of the original Instagram APKM bundle and verify the patched installation before replacing daily installations.

4. **LeadAds Form Failure**:
   Applying `Block Crash & Diagnostics` disables `LeadAdsActivity`. Tapping interactive lead-generation advertisement cards in feeds or stories will fail to render the embedded lead form or may trigger an unhandled intent.

5. **Silent Crash Reporting**:
   Applying `Block Crash & Diagnostics` and `Breakpad Crash Reporter Slimmer` disables memory dump uploaders (`DumperUploadService`), Lacrima broadcast receivers, and native C/C++ crash dumping (`libbreakpad.so`). Crash logs, tombstone traces, and dirty shutdown diagnostics are silenced and will not reach Meta backends.

6. **AR Filters, Effects, and Face-Tracking Breakage**:
   Applying `AR Effects Slimmer` zeroes ARCore native libraries and strips face tracking models. In-app camera augmented reality filters, 3D face meshes, body tracking, and person/hair segmentation will not function.

7. **Carrier-Preload State Loss**:
   Disabling Oxygen preload components removes background synchronization with carrier app managers (such as carrier AppManager services), preventing carrier-level tracking and install variant selection.

8. **Push Notifications and Core App Intact**:
   Firebase Cloud Messaging (`PushLite`, `IgFirebaseMessagingService`, `FcmListenerService`), account authentication services, media streaming (`MediaStreamService`), and core GraphQL/REST API endpoints (`graph.facebook.com`, `graph.instagram.com`) are strictly preserved and remain fully functional.
