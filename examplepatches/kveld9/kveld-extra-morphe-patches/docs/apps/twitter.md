# X (Twitter) Patches Guide

Technical documentation and patch catalog for X (formerly Twitter) on Android.

---

## 1. Target & Compatibility

| Property | Value |
| :--- | :--- |
| **Target App** | X (Twitter) |
| **Package Name** | `com.twitter.android` |
| **Target Version** | `12.33.1-prod.01` |
| **Target Package Format** | APKM (Split APK Bundle) |
| **Primary Architecture** | `arm64-v8a` |
| **Authoritative Source** | [APKMirror](https://www.apkmirror.com/apk/x-corp/twitter/x-12-33-1-prod-01-release/) |

---

## 2. Applied Patches Catalog

| Patch Name | Typology | Default State | Dependencies | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Block Telemetry & Trackers** | `bytecodePatch` | `true` (Enabled) | `X Telemetry Manifest Purge` | Neutralizes Google AppMeasurement and Firebase Performance Trace dispatchers, and strips advertising identifiers. |
| **X Telemetry Manifest Purge** | `resourcePatch` | `false` (Opt-in) | None | Strips tracking and advertising permissions, disables measurement services and receivers, and injects opt-out metadata in `AndroidManifest.xml`. |
| **Remove Premium Upsell** | `bytecodePatch` | `true` (Enabled) | None | Removes premium upsell surfaces. |
| **Remove Ads** | `bytecodePatch` | `true` (Enabled) | None | Removes promoted posts, trends and ads from timeline. |
| **Timeline UI** | `bytecodePatch` | `false` (Opt-in) | None | Timeline surfaces: posts, threads, banners, recommendations and video entry points with 14 patch-time boolean options. |
| **No Shortened URL** | `bytecodePatch` | `false` (Opt-in) | None | Expands t.co shortened URLs, removing the click-tracking intermediary. |
| **Pause Search Suggestions** | `bytecodePatch` | `false` (Opt-in) | None | Stops persisting search suggestions locally without breaking search. |
| **Control Video Auto Scroll** | `bytecodePatch` | `false` (Opt-in) | None | Prevents automatic scrolling to the next video in immersive view. |
| **Share Links** | `bytecodePatch` | `false` (Opt-in) | None | Custom sharing domain, handle custom links and legacy share links. |
| **Post & Profile UI** | `bytecodePatch` | `false` (Opt-in) | None | Post actions, profile header and Grok surfaces with 3 patch-time boolean options. |
| **Navigation UI** | `bytecodePatch` | `false` (Opt-in) | None | Bottom navigation, FAB, tabs and bars with 7 patch-time boolean options. |
| **Show Sensitive Media** | `bytecodePatch` | `false` (Opt-in) | None | Displays sensitive media directly without requiring tap to reveal. |
| **DM Debloat** | `bytecodePatch` | `false` (Opt-in) | None | Hide call buttons in direct message conversations with 2 patch-time boolean options. |
| **Block Update Screen** | `bytecodePatch` | `true` (Enabled) | None | Blocks the 'This app is out of date' screen and suppresses in-app update prompts on launch. |
| **X MLKit Vision Slimmer** | `resourcePatch` | Opt-in | No | Disables MLKit discovery service + init provider and strips MLKit registrars. WARNING: breaks in-app QR/barcode scanning. |

---

## 3. Deep Technical Breakdown

### A. Layer 1: Manifest Purge Layer (`resourcePatch`)

The resource patch executes declarative AST transformations on `AndroidManifest.xml` before DEX assembly:

1. **Permission Stripping**:
   - `com.google.android.gms.permission.AD_ID` (Google Play Services Advertising ID)
   - `android.permission.ACCESS_ADSERVICES_AD_ID` (Android Privacy Sandbox Ad ID)
   - `android.permission.ACCESS_ADSERVICES_ATTRIBUTION` (Privacy Sandbox Attribution API)
   - `android.permission.ACCESS_ADSERVICES_TOPICS` (Privacy Sandbox Topics API)
   - `com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE` (Play Install Referrer)

2. **Component Disabling**:
   Disables measurement and background transport components by setting `android:enabled="false"` and `android:exported="false"`:
   - `com.google.android.gms.measurement.AppMeasurementService`
   - `com.google.android.gms.measurement.AppMeasurementJobService`
   - `com.google.android.datatransport.runtime.backends.TransportBackendDiscovery`
   - `com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService`
   - `com.google.android.gms.measurement.AppMeasurementReceiver`
   - `com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver`
   - `com.twitter.analytics.tracking.InstallationReferrer$OemIntentReceiver`

3. **Component Discovery Registrar Pruning**:
   Purges dependency injection registrars declared under `com.google.firebase.components.ComponentDiscoveryService` matching tracking keywords (`analytics`, `measurement`, `perf`, `remoteconfig`, `sessions`, `abt`), while strictly preserving ML Kit, Firebase Cloud Messaging, and Crashlytics registrars. Crashlytics registrars are preserved because the app requires the component at startup (collection suppressed via `firebase_crashlytics_collection_enabled=false`):
   - `FirebasePerfKtxRegistrar` / `FirebasePerfRegistrar`
   - `AnalyticsConnectorRegistrar`
   - `FirebaseRemoteConfigKtxRegistrar` / `RemoteConfigRegistrar`
   - `AbtRegistrar`
   - `FirebaseSessionsRegistrar`

4. **Opt-Out Metadata Injection**:
   Injects declarative configuration tags under `<application>` to disable SDK telemetry collection across initialization sequences:
   - `firebase_analytics_collection_enabled` = `false`: Disables automated Firebase Analytics event recording.
   - `firebase_analytics_collection_deactivated` = `true`: Permanently deactivates Analytics collection runtime triggers.
   - `firebase_crashlytics_collection_enabled` = `false`: Suppresses Firebase Crashlytics crash report collection and scheduled uploads.
   - `firebase_performance_collection_enabled` = `false`: Disables Firebase Performance Monitoring runtime metrics capture.
   - `firebase_performance_collection_deactivated` = `true`: Permanently deactivates Firebase Performance collection.
   - `google_analytics_adid_collection_enabled` = `false`: Blocks Google Analytics advertising ID association.
   - `google_analytics_default_allow_ad_personalization_signals` = `false`: Disables personalization signals.

5. **Application Invariants & Non-Interference**:
   The manifest purge explicitly preserves:
   - Core network access permissions (`android.permission.INTERNET`, `android.permission.ACCESS_NETWORK_STATE`).
   - Push notifications via `com.twitter.notification.service.firebase.TwitterFirebaseMessagingService`, `com.google.firebase.messaging.FirebaseMessagingService`, and `com.google.firebase.provider.FirebaseInitProvider`.
   - Google Play Billing via `com.android.billingclient.api.ProxyBillingActivity` and `ProxyBillingActivityV2`.
   - Deep-link scheme filters (`twitter://`, `x://`, `https://x.com`, `https://twitter.com`).
   - User-facing internal WebView and analytics screens (`TweetAnalyticsWebViewActivity`, `AccountAnalyticsActivity`, `RoomHostAnalyticsWebViewActivity`).

### B. Layer 2: Bytecode Dispatcher Neutralization Layer (`bytecodePatch`)

The Dalvik bytecode layer neutralizes SDK event submission pipelines at execution time:

1. **Targeted Entrypoint Hooks**:
   - `Lcom/google/android/gms/measurement/AppMeasurement;->logEventInternal(Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;)V`: Drops measurement event payloads and suppresses metric buffering.
   - `Lcom/google/firebase/perf/metrics/Trace;->start()V`: Neutralizes performance trace session attachment and timer initialization.
   - `Lcom/google/firebase/perf/metrics/Trace;->stop()V`: Suppresses trace completion validation and background metric payload submission to transport.
   - `Lcom/google/firebase/perf/metrics/Trace;->putAttribute(Ljava/lang/String;Ljava/lang/String;)V`: Prevents custom dimension attribute recording on trace instances.
   - `Lcom/google/firebase/perf/metrics/Trace;->removeAttribute(Ljava/lang/String;)V`: Neutralizes trace attribute removal mutations.
   - `Lcom/google/firebase/perf/metrics/Trace;->putMetric(Ljava/lang/String;J)V`: Suppresses custom counter metric recording on trace instances.
   - `Lcom/google/firebase/perf/metrics/Trace;->incrementMetric(Ljava/lang/String;J)V`: Suppresses custom counter increment mutations on trace instances.

2. **Transformation Strategy**:
   - Injects immediate `return-void` at instruction index 0.
   - Prevents allocation of event payloads, dispatch thread creation, and background telemetry buffering.
   - Restricts hooks to stable, unobfuscated third-party SDK signatures, eliminating fragility across target app updates.
   - Removes the bare `Object.getClass()` null check that R8 leaves after app-side calls to the obfuscated Firebase Performance `getInstance()` (static, no-arg method in `com/google/firebase/perf/` returning its own class). Purging `FirebasePerfRegistrar` makes `getInstance()` return `null`, which otherwise crashes the app when opening a post from a DM or a quoted post. The scan skips `com/google/firebase/` SDK classes, replaces only an unconsumed `getClass()` on the `move-result-object` register within 8 instructions, and fails the patch if no call site matches.

3. **Telemetry & Zero-Zombie Standard**:
   - Emits structured diagnostic messages prefixed with `[X Telemetry]`.
   - Reports dynamic hook mutation counts without loop spam.
   - Follows zero-zombie verification to ensure all hooks cleanly resolve or prune on target updates.

### C. Layer 3: Premium Upsell Neutralization Layer (`bytecodePatch`)

The bytecode patch removes client-side premium upsell surfaces in navigation and composer/upload flows:

1. **Targeted Entrypoint Hooks**:
   - `subscriptions_upsells_premium_home_nav_enabled`: gates premium upsell surfaces in media and composer upload flows (`com.x.composer.upload.v2.ui`).
   - `subscriptions_upsells_premium_home_nav_offer_enabled`: gates premium upsell tab and promotional offer surfaces in home navigation tabbed layouts (`com.x.home.tabbed`).

2. **Transformation Strategy**:
   - Locates feature switch check call sites via indexed string literal references.
   - Neutralizes boolean evaluation results dynamically (forces return register to `0` / `false` or boxed `Boolean.FALSE`), short-circuiting upsell gating branches at client level.
   - Preserves standard navigation tabs and upload pipelines without server communication interference.

3. **Telemetry & Zero-Zombie Standard**:
   - Emits structured diagnostic messages prefixed with `[Remove Premium Upsell]`.
   - Reports dynamic hook mutation counts without loop spam (`Applied $patched hooks -> premium upsell surfaces suppressed.`).
   - Guarantees zero zombie mismatches via strict runtime validation on flag evaluation resolution.

### D. Layer 4: Timeline Ads Suppression Layer (`bytecodePatch`)

The bytecode patch removes promoted content from the timeline on the 12.33 native URT stack (`com.x.urt.items`, replacing the removed `JsonTimeline*` mapper layer):

1. **Targeted Entrypoint Hooks**:
   - `Lcom/x/urt/b;->a(...)`: presenter dispatch for timeline items; filters promoted posts (`k1` with `ef` promoted metadata), promoted trends (`z1` with `sf` metadata), and entry IDs containing `promoted`.
   - Google SSP init (`googlessp/init`): short-circuits ad SDK initialization to `Boolean.FALSE`.
   - `Lcom/x/urt/items/post/quickpromote/b;->a(...)`: suppresses the QuickPromote booster surface.

2. **Transformation Strategy**:
   - Injects an item-type gate at presenter dispatch returning the empty `b0` presenter for promoted items.
   - Forces ad-init result to false client-side; no network payloads touched.

3. **Telemetry & Zero-Zombie Standard**:
   - Emits structured diagnostic messages prefixed with `[Remove Ads]`.
   - Reports dynamic hook mutation counts without loop spam (`Applied $patched hooks -> promoted content and timeline ads suppressed.`).
   - Guarantees zero zombie mismatches via strict runtime validation on fingerprint resolution.

### E. Layer 5: Served-Content Debloat (`bytecodePatch`)

The bytecode layer suppresses intrusive server-driven prompts, banners, and served content overlays at the client presentation boundary:

1. **Strategy & Telemetry Standards**:
   - Anchors obfuscated instruction models dynamically via unique string literals without APK-wide method scanning.
   - Neutralizes presentation state models at instantiation time to short-circuit downstream Jetpack Compose rendering trees cleanly.
   - Structured telemetry prefixed with `[<Patch Name>]` reports exact mutation counts under a zero-zombie policy (`error()` on unresolved targets).

2. **Suppressed Surfaces**:
   - **Timeline UI**:
     - Consolidates timeline suppression hooks under 14 configurable boolean options (`hideBanner`, `hidePromoteButton`, `hideNudgeButton`, `hideRecommendationItems`, `hideDiscoverMore`, `hideRecommendedUsers`, `hideLiveThreads`, `hideCommunityNotes`, `hidePostMetrics`, `hideViewCount`, `inlineActionBar`, `timelineAddTab`, `timelineTopBar`, `hideLiveBanner`).
     - **Hide Banner**: Neutralizes `UrtShowInstructionsState` constructor instructions (`UrtShowInstructionsState(showInstructions=`) by replacing `showInstructions` with `Collections.emptyList()` and zeroing all eligibility/visibility flags (`isEligibleToShowPill`, `isReadyToShow`, `isPillCurrentlyVisible`).
     - **Hide Promote Button**: Forces post presentation quick promote eligibility evaluator (`x_lite_quick_promote_enabled`, `x_lite_quick_promote_premium_paywall_enabled`) return to `false` (`0`), suppressing self-post promote button rendering.
     - **Hide Nudge Button**: Neutralizes `FocalPostState` constructor (`FocalPostState(timelinePostState=`) instructions by zeroing `followButtonState` (`null`), `isSubscribeEligible` (`false`), and stateful follow flags before field assignment, suppressing post follow/subscribe/follow back button rendering.
     - **Hide Recommendation Items**: Intercepts timeline presenter dispatch (`Lcom/x/urt/b;->a`) for item models whose `entryId` contains recommendation identifiers (`who-to-follow`, `who_to_follow`, `who-to-subscribe`, `stories`, `eventsummary`, `toptabsrpusermodule`, `community-to-join`) and returns the empty presenter (`Lcom/x/urt/b0;->a`).
     - **Hide Discover More**: Intercepts timeline presenter dispatch (`Lcom/x/urt/b;->a` and `Lcom/x/postdetail/c0;->r`) for item models whose `entryId` contains `discover` or `Discover` and returns the empty presenter (`Lcom/x/urt/b0;->a`) in tweet reply threads.
     - **Hide Recommended Users**: Neutralizes `ProfileFollowRecommendationResponse` constructors (`ProfileFollowRecommendationResponse(style=`) by zeroing `recommendedUsers` (`null`) before field assignment, preventing follow recommendation models from entering presenter state and suppressing recommended users popup rendering.
     - **Hide Live Threads**: Neutralizes `FleetlineApiResponse` constructor (`FleetlineApiResponse(threads=`) by replacing `threads` with `Collections.emptyList()` before field assignment, suppressing live thread and space bar rendering at the top of the timeline.
     - **Hide Live Banner**: Neutralizes feature switch `x_lite_livestream_pill_enabled` to `false` (`0`), suppressing the top-of-timeline livestream and broadcast pill docked beneath home tabs.
     - **Community Notes & Inline Actions**: Neutralizes feature switches `birdwatch_consumption_enabled` and `android_ntab_display_options_inline_actions_enabled` to `false` (`0`).
     - **Post Metrics & View Count**: Rewrites `Lcom/x/inlineactionbar/d;->invoke` to substitute `CountedPill` with `Countless` and injects filtering around view count invoke instructions.
     - **Timeline Add Tab & Top Bar**: Zeroes conditional branch registers in `Lcom/x/home/tabbed/a;` (`f`, `i`, `j`) to hide the home timeline add tab button and follow button in top bars.
   - **No Shortened URL**:
     - Target: `ApiTimelineUrl` constructor (`com.x.android.core.fragment`), resolved dynamically via indexed string `ApiTimelineUrl(url=`, and `GraphqlUrlsEntity` constructor, resolved dynamically via indexed string `GraphqlUrlsEntity(display_url=`.
     - Transformation: Intercepts constructor field assignments and substitutes shortened `t.co` URLs with `expanded_url` when non-null (`if-eqz`), replacing the click-tracking redirect intermediary at parse time across timeline rich text and entity sets.
   - **Pause Search Suggestions**:
     - Target: `RecentSearchRepo` DataStore save method (`com.x.repositories.search.q`), resolved dynamically via indexed string `search_recent_v3`.
     - Transformation: Injects an immediate `sget-object ... Lkotlin/Unit;` + `return-object` early-return hook at method entry (index 0) with dynamic field name resolution, neutralizing persistent writes to `search_recent_v3` Preferences DataStore while preserving search query execution, typeahead, and result presentation.

    - **Control Video Auto Scroll**:
      - Target: `PersistentVideoSettings` constructor and state model (`com.x.media.playback.settings.persistent`), resolved dynamically via indexed strings `PersistentVideoSettings(hasLockedPlaybackSpeed=` and `, isAutoAdvanceEnabled=`.
      - Transformation: Dynamically resolves the `isAutoAdvanceEnabled` field name from `toString` and zeroes the boolean parameter register (`const/4 vN, 0x0`) immediately before field assignment in `<init>`, permanently preventing automatic video progression across immersive playback sessions.
    - **Share Links**:
      - Target: Post share intent builder (`com.x.share.impl.x.c`), resolved dynamically via indexed strings `https://x.com/i/trending/`, `https://x.com/i/lists/`, `https://x.com/i/status/`, `image/*`, and canonical post URL model (`com.x.models.u5.getUrl`), resolved via `https://x.com/unavailable/status/`, `https://x.com/`, `/status/`.
      - Transformation: When `customSharingDomain` is enabled, substitutes `https://x.com/i/status/` and `https://x.com/` with `fxtwitter.com` (`https://fxtwitter.com/i/status/` and `https://fxtwitter.com/`) at string load sites. `handleCustomLinks` and `legacyShareLinks` boolean options are provided with runtime skips documenting the absence of stock custom-domain routing and post-11.4x upstream deprecation of author handles in share URLs.
    - **Post & Profile UI**:
      - Target: Client-side Grok post and profile action switches and profile connect tabs switch (`grok_android_analyze_timelines_enabled`, `grok_android_imagine_cta_profile_enabled`, `x_lite_profile_connect_tabs_enabled`) across `com.x.main.landing.q` and `com.x.main.y`.
      - Transformation: Neutralizes evaluation results for verified local feature switches to `false` (`0`). Provides 3 patch-time boolean options (`hideGrokPostAction`, `hideProfileGrokButton`, `profileTabs`).
    - **Navigation UI**:
      - Target: Client-side bottom navigation, Grok tab, rich explore/search tabs, sidebar, badges, and compose FAB expansion switches across `com.x.main.landing.d0`, `com.x.main.landing.q`, `com.x.explore.f`, and `com.x.notifications.tab.j`.
      - Transformation: Neutralizes evaluation results for verified local navigation feature switches (`android_x_lite_persistent_bottom_nav_enabled`, `android_webview_grok_tab_enabled`, `android_rich_tabs_explore_tab_enabled`, `android_rich_tabs_search_tab_enabled`, `android_grok_bot_sidebar_enabled`, `grok_android_tab_badge_enabled`, `android_compose_fab_expansion_enabled`) to `false` (`0`) at call sites. Provides 7 patch-time boolean options (`hideBadges`, `hideFabMenu`, `navigationBar`, `grokTab`, `exploreTabs`, `searchTabItems`, `sideBarItems`).
    - **Show Sensitive Media**:
      - Target: `MediaVisibilityResults` constructors (`com.x.models.interstitial.h`), resolved dynamically via indexed string `MediaVisibilityResults(blurImageInterstitial=`.
      - Transformation: Injects an immediate `return-void` following the super constructor `<init>()V` at instruction index 1 across both constructors, permanently ensuring `blurImageInterstitial` remains `null`. This suppresses downstream sensitive media blur overlays and warning banners across post presentation and media visibility trees without modifying user settings or network payloads.
    - **Block Update Screen**:
      - Target: Startup version gate evaluator (`com.x.composer.work.l1`), resolved dynamically via indexed strings `min_android_xlite_version` and `x_lite_in_app_update_blocking_versions`, and Google Play in-app update controller (`com.x.android.appupdate.f`), resolved via indexed strings `x_lite_in_app_update_min_releases_behind` and `x_lite_in_app_update_cold_start_versions`.
      - Transformation: Injects an immediate `sget-object ... Lkotlin/Unit;` + `return-object` early-return hook at the `min_android_xlite_version` evaluation site, permanently bypassing version code thresholds, version blacklist matching, Decompose router `ForceUpdateArgs` stack substitution, and lifecycle DataStore persistence. Additionally injects an immediate `return-void` at instruction index 0 of Google Play in-app update check entrypoint, suppressing Play Store update bottom sheets and prompts on cold/warm startup.
    - **DM Debloat**:
      - Target: Client-side direct message calling and conversation surface gates across `com.x.calling.navigator.w` and `com.x.dm.root.o1`.
      - Transformation: Neutralizes evaluation results for verified local feature switch `xcall_xchat_enabled` to `false` (`0`) at call sites when either audio or video call toggle is enabled, hiding both header buttons together through the unified xchat pipeline. WARNING: neutralizing this flag disables DM outgoing-call entrypoints, not just the header icons. Provides 2 patch-time boolean options (`hideDmAudioCall`, `hideDmVideoCall`).

### F. Evaluated Candidates & Architectural Boundaries

During reverse engineering of target `12.33.0-prod.01`, six prospective patches were audited and classified as `NOT_REPRODUCED` / Out-of-Scope due to the absence of local bytecode gates or resource definitions:

1. **Disable Auto Timeline Scroll on Launch**:
   - Upstream Piko implemented timeline scroll restoration through a multi-component runtime extension (`TimelinePosition.java`) backed by SQLite/DataStore persistence across cold starts, rather than a standalone bytecode patch.
   - In `12.33.0-prod.01`, timeline state is governed by `com.x.urt.y` (`c()Lcom/x/urt/l0;`), where cold-launch timeline state is null and resets to `(0, 0)` in memory. No localized boolean switch or single-method gate exists to retain position without external runtime persistence. Classified as `NOT_REPRODUCED`.

2. **System Font (Chirp Font Bypass)**:
   - Upstream Piko relied on legacy feature-switch interceptors (`chirp_font_enabled`) in `FeatureSwitchesConfiguration`.
   - In `12.33.0-prod.01`, Twitter/X completely migrated typography to Jetpack Compose (`com.x.xds.core.e`), hardcoding Chirp OTF font resources (`0x7f090000..0x7f090004`) into static field `b` (`FontListFontFamily`) without string literals, and binding XML styles directly to assets. No local feature switch or Typeface gate exists. Classified as `NOT_REPRODUCED`.

3. **Dynamic Color (Material You Palette Bypass)**:
   - Upstream Piko implemented Dynamic Color as an XML resource patch rewriting legacy `res/values/colors.xml` brand entries (`twitter_blue` / `brand_blue`) to system accent references (`@android:color/system_accent1_*`).
   - In `12.33.0-prod.01`, Twitter/X completely migrated UI styling and theming to Jetpack Compose (XDS - `com.x.xds.core.*`). Canonical brand blue (`#1D9BF0`, `4280130544L` / `0xff1d9bf0L`) is hardcoded as an immutable primitive constant into static field `w` of `com.x.xds.core.c` and loaded at class initialization (`<clinit>`) into `XColors` (`com.x.xds.core.a`), exposed via `com.x.xds.core.b->a` (`CompositionLocal`). The APK resource table (`resources.arsc`) contains zero brand blue entries, and no local XML attribute, feature switch, or dynamic color resolver gate exists. Classified as `NOT_REPRODUCED`.

4. **Post Display**:
   - Upstream Piko grouped 6 post and profile display toggles (`roundOffNumbers`, `selectableText`, `postSourceLabel`, `moreProfileInfo`, `legacyFollowerLists`, `showChangelogs`).
   - In `12.33.0-prod.01`, exhaustive reverse engineering established that none of these surfaces possess a client-side bytecode feature switch or local gate:
     - `roundOffNumbers`: post engagement metrics, counts, and view counters are formatted and served directly via URT models; no local client feature flag or number format gate exists.
     - `selectableText`: user bio and profile header text are rendered entirely in Jetpack Compose (`com.x.profile.header.j6` / `ai.x.grok.bots.markdown.h`), with 0 `TextView` instances and 0 `setTextIsSelectable` calls across the APK.
     - `postSourceLabel`: post client source attribution (e.g. "Twitter for iPhone") was deprecated and completely removed upstream from the client and server API in late 2022.
     - `moreProfileInfo`: profile header details are rendered via Compose `UserProfileHeader` without client feature flags; GraphQL `include_profile_info` is strictly model metadata and `DebugProfileInfo` is scoped to debug/onboarding tasks.
     - `legacyFollowerLists`: follower and following lists are served via standard URT timeline queries; legacy activities and their feature switches were removed upstream.
     - `showChangelogs`: mod changelog presentation was implemented via companion runtime extension hooks; no stock client bytecode gate exists in the stock X APK.
   - Classified as `NOT_REPRODUCED`.

5. **Show Poll Results (Unvoted Polls)**:
   - Upstream Piko proposed displaying poll voting results without voting.
   - In `12.33.0-prod.01`, client poll state is governed by `com.x.cards.impl.poll.x` (`PollCardState`) and `com.x.cards.impl.poll.j` (`PollCardPresenter`). While the local client layout gates result bar rendering behind `selectedChoice != null || isAuthor`, vote counts and percentages (`choice{i}_count`) are not computed client-side; they are provided directly by Twitter's backend in card binding JSON payloads.
   - For active polls where the user has not voted and is not the author, Twitter's backend strips `choice{i}_count` from the card data to prevent voting bias. Forcing `isAuthor` or `selectedChoice` locally results in 0 vote counts (0%) across all options rather than genuine results. Accessing true results prior to poll expiration strictly requires a server-side vote mutation (`POST /v2/cards/create.json` / `VoteOnPoll`).
   - Because genuine poll results depend on server-side response payloads rather than a client-side gate, this candidate is classified as `NOT_REPRODUCED`.

6. **Remove Search Suggestions (Explore)**:
   - Upstream Piko proposed suppressing search suggestions in the Explore section.
   - In `12.33.0-prod.01`, Explore content (timelines, trends, topic modules) is entirely server-driven via URT GraphQL queries without local suggestion toggle flags.
   - The interactive search query surface (`com.x.search.query.n` / `w0`) manages two suggestion layers: local search history persistence (neutralized client-side via the standalone `Pause Search Suggestions` patch on `RecentSearchRepo` / `search_recent_v3`), and live typeahead suggestions fetched dynamically via GraphQL (`SearchTypeaheadQuery` / `Global_search_typeahead`).
   - Exhaustive DEX inspection across all APK classes (`classes*.dex`) confirmed that no client-side feature switch (`search_suggestions_enabled`, `typeahead_suggestions_enabled`) or local render gate exists to selectively suppress search suggestions in the Explore section without breaking global query execution. Classified as `NOT_REPRODUCED`.

