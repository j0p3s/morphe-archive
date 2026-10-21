package app.morphe.patches.multiaccounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.shared.Constants.COMPATIBILITY_MULTIACCOUNTS
import app.morphe.patches.shared.clearTryBlocks
import app.morphe.patches.shared.disableComponentsWhere
import app.morphe.patches.shared.removeComponentDiscoveryRegistrarsWhere
import app.morphe.patches.shared.replaceWithReturnNull
import app.morphe.patches.shared.replaceWithReturnVoid
import app.morphe.patches.shared.setApplicationMetaData
import app.morphe.patches.shared.stripPermissionsWhere
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.w3c.dom.Element

// --- Resource Patch Contracts ---

private val BLOCKED_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "com.google.android.gms.permission.AD_ID_NOTIFICATION",
    "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE",
    "BIND_GET_INSTALL_REFERRER_SERVICE",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
)

// Load-bearing first-party static-data provider (authority <package>.staticslioprovider) queried at app startup; disabling it kills the process (observed: logcat "Failed to find provider info for ...staticslioprovider" then process death + downstream UnsatisfiedLinkError in com.excelliance.kxqp.util natives whose payload this provider serves).
private val BLOCKED_PROVIDERS = setOf(
    "com.applovin.sdk.AppLovinInitProvider",
    "com.yandex.mobile.ads.core.initializer.MobileAdsInitializeProvider",
    "com.yandex.mobile.ads.features.debugpanel.data.local.DebugPanelFileProvider",
    "io.appmetrica.analytics.internal.PreloadInfoContentProvider",
    "com.google.android.gms.ads.MobileAdsInitProvider",
    "com.vungle.ads.VungleProvider",
    "com.ironsource.lifecycle.IronsourceLifecycleProvider",
    "com.ironsource.lifecycle.LevelPlayActivityLifecycleProvider",
    "com.mbridge.msdk.config.component.status.MBComponentLifecycleProvider",
    "com.secmtp.sdk.core.api.ATInitializationProvider",
    "com.smartdigimkt.sdk.api.SDMInitializationProvider",
)

private val BLOCKED_SERVICES = setOf(
    "com.applovin.impl.adview.activity.FullscreenAdService",
    "com.applovin.impl.adview.activity.AppRestartDuringAdDetectionService",
    "com.google.android.gms.measurement.AppMeasurementService",
    "com.google.android.gms.measurement.AppMeasurementJobService",
    "io.appmetrica.analytics.internal.AppMetricaService",
    "com.google.android.gms.ads.AdService",
    "com.google.android.datatransport.runtime.backends.TransportBackendDiscovery",
    "com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService",
)

private val BLOCKED_RECEIVERS = setOf(
    "com.google.android.gms.measurement.AppMeasurementReceiver",
    "com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver",
    "com.bytedance.applog.collector.Collector",
    "com.mbridge.msdk.foundation.same.broadcast.NetWorkChangeReceiver",
)

private val BLOCKED_ACTIVITIES = setOf(
    // AppLovin MAX
    "com.applovin.adview.AppLovinFullscreenActivity",
    "com.applovin.adview.AppLovinFullscreenImmersiveActivity",
    "com.applovin.sdk.AppLovinWebViewActivity",
    "com.applovin.mediation.MaxDebuggerActivity",
    "com.applovin.mediation.MaxDebuggerDetailActivity",
    "com.applovin.mediation.MaxDebuggerMultiAdActivity",
    "com.applovin.mediation.MaxDebuggerAdUnitsListActivity",
    "com.applovin.mediation.MaxDebuggerAdUnitWaterfallsListActivity",
    "com.applovin.mediation.MaxDebuggerAdUnitDetailActivity",
    "com.applovin.mediation.MaxDebuggerCmpNetworksListActivity",
    "com.applovin.mediation.MaxDebuggerTcfConsentStatusesListActivity",
    "com.applovin.mediation.MaxDebuggerTcfInfoListActivity",
    "com.applovin.mediation.MaxDebuggerTcfStringActivity",
    "com.applovin.mediation.MaxDebuggerTestLiveNetworkActivity",
    "com.applovin.mediation.MaxDebuggerTestModeNetworkActivity",
    "com.applovin.mediation.MaxDebuggerUnifiedFlowActivity",
    "com.applovin.mediation.MaxDebuggerWaterfallSegmentsActivity",
    "com.applovin.mediation.MaxDebuggerAxonEventsListActivity",
    "com.applovin.creative.MaxCreativeDebuggerActivity",
    "com.applovin.creative.MaxCreativeDebuggerDisplayedAdActivity",
    // Yandex
    "com.yandex.mobile.ads.common.AdActivity",
    "com.yandex.mobile.ads.features.debugpanel.ui.IntegrationInspectorActivity",
    // Unity / IronSource
    "com.unity3d.services.ads.adunit.AdUnitActivity",
    "com.unity3d.services.ads.adunit.AdUnitTransparentActivity",
    "com.unity3d.services.ads.adunit.AdUnitTransparentSoftwareActivity",
    "com.unity3d.services.ads.adunit.AdUnitSoftwareActivity",
    "com.unity3d.ads.adplayer.FullScreenWebViewDisplay",
    "com.ironsource.sdk.controller.ControllerActivity",
    "com.ironsource.sdk.controller.InterstitialActivity",
    "com.ironsource.sdk.controller.OpenUrlActivity",
    "com.unity3d.ironsourceads.internal.services.InlineStoreActivity",
    "com.ironsource.mediationsdk.testSuite.TestSuiteActivity",
    // Verve / HyprMX
    "com.verve.ng.sdk.consent.UserConsentActivity",
    "com.verve.ng.sdk.interstitial.activity.MraidInterstitialActivity",
    "com.verve.ng.sdk.interstitial.activity.VastInterstitialActivity",
    "com.verve.ng.sdk.rewarded.activity.MraidRewardedActivity",
    "com.verve.ng.sdk.rewarded.activity.VastRewardedActivity",
    "com.verve.ng.sdk.contentinfo.AdFeedbackActivity",
    "com.verve.ng.sdk.utils.browser.BrowserActivity",
    // Google Ads
    "com.google.android.gms.ads.AdActivity",
    "com.google.android.gms.ads.OutOfContextTestingActivity",
    "com.google.android.gms.ads.NotificationHandlerActivity",
    // ByteDance / Pangle
    "com.bytedance.sdk.openadsdk.activity.single.TTCeilingLandingPageActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTLandingPageActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTPlayableLandingPageActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTVideoLandingPageLink2Activity",
    "com.bytedance.sdk.openadsdk.activity.single.TTHistoryLandingPageActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTHistoryActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTDelegateActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTWebsiteActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTAppOpenAdActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTAppOpenAdTransActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTRewardVideoActivity",
    "com.bytedance.sdk.openadsdk.activity.TTRewardWebActivity",
    "com.bytedance.sdk.openadsdk.activity.TTFullWebActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTRewardExpressVideoActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTFullScreenVideoActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTFullScreenExpressVideoActivity",
    "com.bytedance.sdk.openadsdk.activity.single.TTAdActivity",
    "com.bytedance.applog.migrate.MigrateDetectorActivity",
    // Vungle
    "com.vungle.ads.internal.ui.VungleActivity",
    // Mintegral
    "com.mbridge.msdk.config.activity.MBRewardVideoActivity",
    "com.mbridge.msdk.activity.MBCommonActivity",
    "com.mbridge.msdk.out.LoadingActivity",
    "com.mbridge.msdk.reward.player.MBRewardVideoActivity",
    // Secmtp / SmartDigiMkt
    "com.secmtp.sdk.core.activity.ATGdprAuthActivity",
    "com.secmtp.sdk.core.common.inner.ui.ATLandscapeActivity",
    "com.secmtp.sdk.core.common.inner.ui.ATPortraitActivity",
    "com.secmtp.sdk.core.common.inner.ui.ATLandscapeTranslucentActivity",
    "com.secmtp.sdk.core.common.inner.ui.ATPortraitTranslucentActivity",
    "com.secmtp.sdk.core.common.inner.ui.activity.ATMixSplashActivity",
    "com.secmtp.sdk.core.basead.ui.web.WebLandPageActivity",
    "com.smartdigimkt.expressad.reward.player.ATRewardVideoActivity",
    "com.smartdigimkt.sdk.basead.ui.ATLandscapeActivity",
    "com.smartdigimkt.sdk.basead.ui.ATPortraitActivity",
    "com.smartdigimkt.sdk.basead.ui.ATLandscapeTranslucentActivity",
    "com.smartdigimkt.sdk.basead.ui.ATPortraitTranslucentActivity",
    "com.smartdigimkt.sdk.basead.ui.web.WebLandPageActivity",
    "com.smartdigimkt.sdk.basead.ui.RewardExitConfirmDialogActivity",
    // App-internal ad splash activities
    "com.excelliance.kxqp.ads.topon.TopOnAdSplashActivity",
    "com.excelliance.kxqp.ads.api.ApiAdSplashActivity",
)

private val OPT_OUT_METADATA = listOf(
    "firebase_analytics_collection_enabled" to "false",
    "firebase_analytics_collection_deactivated" to "true",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
    "firebase_performance_collection_deactivated" to "true",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
    "google_analytics_automatic_screen_reporting_enabled" to "false",
    "com.yandex.mobile.ads.AUTOMATIC_SDK_INITIALIZATION" to "false",
    "com.my.target.autoInitMode" to "0",
    "com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION" to "false",
)

@Suppress("unused")
val multiaccountsTelemetryManifestPatch = resourcePatch(
    name = "Multiaccounts Telemetry Manifest Purge",
    description = "Strips advertising permissions, disables ad network providers, activities, services, and receivers, prunes telemetry registrars, and injects opt-out metadata flags in AndroidManifest.xml.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)

    execute {
        val manifestFile = get("AndroidManifest.xml")
        if (!manifestFile.exists()) {
            println("[Multiaccounts Telemetry] Skipped: AndroidManifest.xml not found.")
            return@execute
        }

        var removedPermissions = 0
        var disabledProviders = 0
        var disabledServices = 0
        var disabledReceivers = 0
        var disabledActivities = 0
        var removedRegistrars = 0
        var injectedMetadata = 0

        document(manifestFile.absolutePath).use { doc ->
            val root = doc.documentElement
            val application = root.getElementsByTagName("application").item(0) as? Element

            removedPermissions = root.stripPermissionsWhere { it in BLOCKED_PERMISSIONS }.size

            if (application != null) {
                disabledProviders = application.disableComponentsWhere("provider") { it in BLOCKED_PROVIDERS }
                disabledServices = application.disableComponentsWhere("service") { it in BLOCKED_SERVICES }
                disabledReceivers = application.disableComponentsWhere("receiver") { it in BLOCKED_RECEIVERS }
                disabledActivities = application.disableComponentsWhere("activity") { it in BLOCKED_ACTIVITIES }

                removedRegistrars = application.removeComponentDiscoveryRegistrarsWhere { name ->
                    (name.contains("analytics", ignoreCase = true) ||
                        name.contains("measurement", ignoreCase = true) ||
                        name.contains("crashlytics", ignoreCase = true)) &&
                        !name.contains("mlkit", ignoreCase = true)
                }

                OPT_OUT_METADATA.forEach { (name, value) ->
                    application.setApplicationMetaData(name, value)
                    injectedMetadata++
                }
            }
        }

        println("[Multiaccounts Telemetry] Stripped $removedPermissions permission(s), disabled $disabledProviders provider(s), $disabledServices service(s), $disabledReceivers receiver(s), $disabledActivities activity/activities, removed $removedRegistrars discovery registrar(s), and injected $injectedMetadata opt-out flag(s).")
    }
}

// --- Bytecode Patch Contracts ---

private data class TargetMethod(
    val name: String,
    val parameters: List<String>,
)

private data class SdkHookFamily(
    val definingClass: String,
    val label: String,
    val methods: List<TargetMethod>,
)

private val SDK_HOOK_FAMILIES = listOf(
    SdkHookFamily(
        definingClass = "Lcom/android/staticslio/StatisticsManager;",
        label = "In-House StatisticsManager",
        methods = listOf(
            TargetMethod("uploadData", listOf("Ljava/lang/String;")),
            TargetMethod("uploadBeanImmediateInternal", listOf("Lcom/android/staticslio/beans/BaseBean;")),
            TargetMethod("startImmediateUploadData", emptyList()),
            TargetMethod("startNextUploadDataTask", emptyList()),
            TargetMethod("uploadAllData", emptyList()),
            TargetMethod("uploadBean", listOf("Landroid/content/ContentValues;", "Z")),
            TargetMethod("uploadBean", listOf("Lcom/android/staticslio/beans/BaseBean;")),
            TargetMethod("uploadBeanImmediate", listOf("Lcom/android/staticslio/beans/BaseBean;")),
            TargetMethod("uploadStaticData", listOf("I", "I", "Ljava/lang/String;")),
            TargetMethod("uploadStaticData", listOf("I", "I", "Ljava/lang/String;", "Lcom/android/staticslio/e;")),
            TargetMethod("uploadStaticDataForOptions", listOf("I", "I", "Ljava/lang/String;", "Lcom/android/staticslio/e;", "[Lcom/android/staticslio/beans/OptionBean;")),
            TargetMethod("upLoadBasicInfoStaticData", listOf("Ljava/lang/String;", "Ljava/lang/String;", "I", "Z", "Ljava/lang/String;", "Z")),
            TargetMethod("upLoadBasicInfoStaticData", listOf("Ljava/lang/String;", "Ljava/lang/String;", "Z", "Ljava/lang/String;", "Z")),
            TargetMethod("upLoadBasicInfoStaticData", listOf("Ljava/lang/String;", "Ljava/lang/String;", "Z", "Z")),
            TargetMethod("upLoadBasicInfoStaticData", listOf("Ljava/lang/String;", "Z", "Ljava/lang/String;", "Z")),
            TargetMethod("upLoadBasicInfoStaticData", listOf("Ljava/lang/String;", "Z", "Z")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/bytedance/applog/AppLog;",
        label = "ByteDance AppLog",
        methods = listOf(
            TargetMethod("onEventV3", listOf("Ljava/lang/String;")),
            TargetMethod("onEventV3", listOf("Ljava/lang/String;", "Landroid/os/Bundle;")),
            TargetMethod("onEventV3", listOf("Ljava/lang/String;", "Landroid/os/Bundle;", "I")),
            TargetMethod("onEventV3", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
            TargetMethod("onEventV3", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;", "I")),
            TargetMethod("onMiscEvent", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
            TargetMethod("trackClick", listOf("Landroid/view/View;")),
            TargetMethod("trackClick", listOf("Landroid/view/View;", "Lorg/json/JSONObject;")),
            TargetMethod("trackPage", listOf("Landroid/app/Activity;")),
            TargetMethod("trackPage", listOf("Landroid/app/Activity;", "Lorg/json/JSONObject;")),
            TargetMethod("trackPage", listOf("Ljava/lang/Object;")),
            TargetMethod("trackPage", listOf("Ljava/lang/Object;", "Lorg/json/JSONObject;")),
            TargetMethod("startDurationEvent", listOf("Ljava/lang/String;")),
            TargetMethod("stopDurationEvent", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
            TargetMethod("pauseDurationEvent", listOf("Ljava/lang/String;")),
            TargetMethod("resumeDurationEvent", listOf("Ljava/lang/String;")),
            TargetMethod("profileSet", listOf("Lorg/json/JSONObject;")),
            TargetMethod("profileSetOnce", listOf("Lorg/json/JSONObject;")),
            TargetMethod("profileIncrement", listOf("Lorg/json/JSONObject;")),
            TargetMethod("profileAppend", listOf("Lorg/json/JSONObject;")),
            TargetMethod("flush", emptyList()),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/applovin/impl/sdk/EventServiceImpl;",
        label = "AppLovin EventServiceImpl",
        methods = listOf(
            TargetMethod("trackEvent", listOf("Ljava/lang/String;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;", "Ljava/util/Map;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;", "Ljava/util/Map;", "Ljava/util/Map;")),
            TargetMethod("trackEventSynchronously", listOf("Ljava/lang/String;")),
            TargetMethod("trackCheckout", listOf("Ljava/lang/String;", "Ljava/util/Map;")),
            TargetMethod("trackInAppPurchase", listOf("Landroid/content/Intent;", "Ljava/util/Map;")),
            TargetMethod("maybeTrackAppOpenEvent", emptyList()),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/applovin/sdk/AppLovinSdk;",
        label = "AppLovinSdk Debuggers",
        methods = listOf(
            TargetMethod("showCreativeDebugger", emptyList()),
            TargetMethod("showMediationDebugger", emptyList()),
            TargetMethod("showMediationDebugger", listOf("Ljava/util/Map;")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/google/android/gms/measurement/AppMeasurement;",
        label = "Google AppMeasurement",
        methods = listOf(
            TargetMethod("logEventInternal", listOf("Ljava/lang/String;", "Ljava/lang/String;", "Landroid/os/Bundle;")),
            TargetMethod("logEventInternalNoInterceptor", listOf("Ljava/lang/String;", "Ljava/lang/String;", "Landroid/os/Bundle;", "J")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lio/appmetrica/analytics/AppMetrica;",
        label = "Yandex AppMetrica",
        methods = listOf(
            TargetMethod("reportEvent", listOf("Ljava/lang/String;")),
            TargetMethod("reportEvent", listOf("Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("reportEvent", listOf("Ljava/lang/String;", "Ljava/util/Map;")),
            TargetMethod("reportError", listOf("Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("reportError", listOf("Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/Throwable;")),
            TargetMethod("reportError", listOf("Ljava/lang/String;", "Ljava/lang/Throwable;")),
            TargetMethod("reportUnhandledException", listOf("Ljava/lang/Throwable;")),
            TargetMethod("reportAppOpen", listOf("Landroid/app/Activity;")),
            TargetMethod("reportAppOpen", listOf("Landroid/content/Intent;")),
            TargetMethod("reportAppOpen", listOf("Ljava/lang/String;")),
            TargetMethod("reportRevenue", listOf("Lio/appmetrica/analytics/Revenue;")),
            TargetMethod("reportAdRevenue", listOf("Lio/appmetrica/analytics/AdRevenue;")),
            TargetMethod("reportExternalAdRevenue", listOf("[Ljava/lang/Object;")),
            TargetMethod("reportExternalAttribution", listOf("Lio/appmetrica/analytics/ExternalAttribution;")),
            TargetMethod("reportUserProfile", listOf("Lio/appmetrica/analytics/profile/UserProfile;")),
            TargetMethod("sendEventsBuffer", emptyList()),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/tenjin/android/TenjinSDK;",
        label = "Tenjin Attribution SDK",
        methods = listOf(
            TargetMethod("connect", emptyList()),
            TargetMethod("connect", listOf("Ljava/lang/String;")),
            TargetMethod("connect", listOf("Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("eventWithName", listOf("Ljava/lang/String;")),
            TargetMethod("eventWithNameAndValue", listOf("Ljava/lang/String;", "I")),
            TargetMethod("eventWithNameAndValue", listOf("Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("eventAdImpression", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionAdMob", listOf("Ljava/lang/Object;", "Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionAdMob", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionAdMob", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionAppLovin", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionAppLovin", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionAppLovin", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionCAS", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionCAS", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionCAS", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionHyperBid", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionHyperBid", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionHyperBid", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionIronSource", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionIronSource", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionIronSource", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionTopOn", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionTopOn", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionTopOn", listOf("Lorg/json/JSONObject;")),
            TargetMethod("eventAdImpressionTradPlus", listOf("Ljava/lang/Object;")),
            TargetMethod("eventAdImpressionTradPlus", listOf("Ljava/lang/String;")),
            TargetMethod("eventAdImpressionTradPlus", listOf("Lorg/json/JSONObject;")),
            TargetMethod("transaction", listOf("Ljava/lang/String;", "Ljava/lang/String;", "I", "D")),
            TargetMethod("transaction", listOf("Ljava/lang/String;", "Ljava/lang/String;", "I", "D", "Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("transactionAmazon", listOf("Lcom/tenjin/android/model/Transaction;")),
            TargetMethod("transactionAmazon", listOf("Ljava/lang/String;", "Ljava/lang/String;", "I", "D", "Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/yandex/mobile/ads/common/MobileAds;",
        label = "Yandex MobileAds",
        methods = listOf(
            TargetMethod("enableLogging", listOf("Z")),
            TargetMethod("showDebugPanel", listOf("Landroid/content/Context;")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/ironsource/mediationsdk/IronSource;",
        label = "IronSource Mediation",
        methods = listOf(
            TargetMethod("setAdRevenueData", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/bytedance/sdk/openadsdk/api/init/PAGSdk;",
        label = "Pangle PAGSdk",
        methods = listOf(
            TargetMethod("setAdRevenue", listOf("Lorg/json/JSONObject;")),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/tiktok/TikTokBusinessSdk;",
        label = "TikTok Business SDK",
        methods = listOf(
            TargetMethod("initializeSdk", listOf("Lcom/tiktok/TikTokBusinessSdk\$TTConfig;")),
            TargetMethod("startTrack", emptyList()),
            TargetMethod("trackTTEvent", listOf("Lcom/tiktok/appevents/base/TTBaseEvent;")),
            TargetMethod("trackTTEvent", listOf("Lcom/tiktok/appevents/base/EventName;")),
            TargetMethod("trackTTEvent", listOf("Lcom/tiktok/appevents/base/EventName;", "Ljava/lang/String;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;", "Ljava/lang/String;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;")),
            TargetMethod("trackEvent", listOf("Ljava/lang/String;", "Lorg/json/JSONObject;", "Ljava/lang/String;")),
            TargetMethod("trackGooglePlayPurchase", listOf("Lcom/tiktok/appevents/TTPurchaseInfo;")),
            TargetMethod("trackGooglePlayPurchase", listOf("Ljava/util/List;")),
            TargetMethod("flush", emptyList()),
        ),
    ),
    SdkHookFamily(
        definingClass = "Lcom/tiktok/appevents/TTRequest;",
        label = "TikTok TTRequest",
        methods = listOf(
            TargetMethod("notifyChange", emptyList()),
        ),
    ),
)

// String-anchored fingerprints for host rewrites in obfuscated OkHttp DoH and analytics classes
private val dohDnsFingerprint = Fingerprint(
    strings = listOf("log.multispaces.net", "ad.multispaces.net"),
)

private val statisCtrlFingerprint = Fingerprint(
    strings = listOf("https://statis.multispaces.net/multiadctrol.php"),
)

@Suppress("unused")
val multiaccountsTelemetryPatch = bytecodePatch(
    name = "Block Telemetry & Trackers",
    description = "Neutralizes in-house statistics, ByteDance AppLog, AppLovin MAX, Google Measurement, AppMetrica, Tenjin, IronSource, and Pangle ad/telemetry dispatchers, redirects isolated tracking host literals to 0.0.0.0, disables ad and tracking manifest components, and injects opt-out metadata flags.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)
    dependsOn(multiaccountsTelemetryManifestPatch)

    execute {
        var patched = 0
        var touchedClasses = 0
        var rewrittenHosts = 0
        val hookCounts = mutableMapOf<String, Int>()

        for (family in SDK_HOOK_FAMILIES) {
            val classDef = classDefByOrNull(family.definingClass)
            if (classDef == null) {
                println("[Multiaccounts Telemetry] Skipped: ${family.definingClass} absent.")
                continue
            }

            val mutableClass = mutableClassDefBy(classDef)
            var familyHooked = 0

            for (target in family.methods) {
                val method = mutableClass.methods.firstOrNull { m ->
                    m.name == target.name &&
                        m.returnType == "V" &&
                        m.parameters.map { it.type } == target.parameters
                } ?: continue

                if (method.implementation == null) continue

                method.replaceWithReturnVoid()
                patched++
                familyHooked++
            }

            if (familyHooked > 0) {
                hookCounts[family.label] = familyHooked
                touchedClasses++
            }
        }

        // TikTok Business SDK Callback Neutralization:
        // initializeSdk(TTConfig, TTInitCallback) is called by com.excelliance.kxqp.util.ql.
        // Replace body to safely invoke callback.success() if non-null without performing
        // network init, config fetch, or background tracking loop setup.
        classDefByOrNull("Lcom/tiktok/TikTokBusinessSdk;")?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            val initCallbackMethod = mutableClass.methods.firstOrNull { m ->
                m.name == "initializeSdk" &&
                    m.parameters.map { it.type } == listOf(
                        "Lcom/tiktok/TikTokBusinessSdk\$TTConfig;",
                        "Lcom/tiktok/TikTokBusinessSdk\$TTInitCallback;",
                    )
            }
            if (initCallbackMethod != null && initCallbackMethod.implementation != null) {
                initCallbackMethod.clearTryBlocks()
                initCallbackMethod.implementation?.let { it.removeInstructions(0, it.instructions.count()) }
                initCallbackMethod.addInstructions(
                    0,
                    """
                        if-eqz p1, :done
                        invoke-interface {p1}, Lcom/tiktok/TikTokBusinessSdk${'$'}TTInitCallback;->success()V
                        :done
                        return-void
                    """.trimIndent(),
                )
                patched++
                hookCounts["TikTok Business SDK Callback"] = 1
            }
        }

        // TikTok TTRequest transport cut:
        // Null out remote config and event batch dispatchers returning JSONObject or List
        classDefByOrNull("Lcom/tiktok/appevents/TTRequest;")?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            var ttRequestCut = 0
            for (m in mutableClass.methods) {
                if (m.name in setOf("getBusinessSDKConfig", "getDebugModeConfig", "reportAppEvent", "fetchDeferredDeeplinkWithCompletion", "reportMonitorEvent") &&
                    m.implementation != null
                ) {
                    m.replaceWithReturnNull()
                    patched++
                    ttRequestCut++
                }
            }
            if (ttRequestCut > 0) {
                hookCounts["TikTok Transport (TTRequest)"] = (hookCounts["TikTok Transport (TTRequest)"] ?: 0) + ttRequestCut
            }
        }

        // TikTok HttpRequestUtil transport cut (SDK-internal HTTP client):
        classDefByOrNull("Lcom/tiktok/util/HttpRequestUtil;")?.let { classDef ->
            val mutableClass = mutableClassDefBy(classDef)
            var httpCut = 0
            for (m in mutableClass.methods) {
                if ((m.name == "doPost" || m.name == "doGet") && m.implementation != null) {
                    m.replaceWithReturnNull()
                    patched++
                    httpCut++
                }
            }
            if (httpCut > 0) {
                hookCounts["TikTok Transport (HttpRequestUtil)"] = httpCut
                touchedClasses++
            }
        }

        // Host rewrite 1: Obfuscated DNS-over-HTTPS / pinned client in Lt2/w;
        val dohMethod = dohDnsFingerprint.method
        val dohInstructions = dohMethod.instructionsOrNull?.toList() ?: emptyList()
        for ((index, instruction) in dohInstructions.withIndex().reversed()) {
            val opcode = instruction.opcode
            if (opcode != Opcode.CONST_STRING && opcode != Opcode.CONST_STRING_JUMBO) continue
            val ref = (instruction as? ReferenceInstruction)?.reference as? StringReference ?: continue
            if (ref.string == "log.multispaces.net" || ref.string == "ad.multispaces.net") {
                val register = (instruction as? OneRegisterInstruction)?.registerA ?: continue
                val op = if (register > 255) "const-string/jumbo" else "const-string"
                dohMethod.replaceInstruction(index, "$op v$register, \"0.0.0.0\"")
                rewrittenHosts++
            }
        }

        // Host rewrite 2: In-house telemetry endpoint constructor in Lv2/f;
        val statisMethod = statisCtrlFingerprint.method
        val statisInstructions = statisMethod.instructionsOrNull?.toList() ?: emptyList()
        for ((index, instruction) in statisInstructions.withIndex().reversed()) {
            val opcode = instruction.opcode
            if (opcode != Opcode.CONST_STRING && opcode != Opcode.CONST_STRING_JUMBO) continue
            val ref = (instruction as? ReferenceInstruction)?.reference as? StringReference ?: continue
            if (ref.string.contains("statis.multispaces.net")) {
                val register = (instruction as? OneRegisterInstruction)?.registerA ?: continue
                val op = if (register > 255) "const-string/jumbo" else "const-string"
                val replaced = ref.string.replace("statis.multispaces.net", "0.0.0.0")
                statisMethod.replaceInstruction(index, "$op v$register, \"$replaced\"")
                rewrittenHosts++
            }
        }

        if (rewrittenHosts > 0) {
            touchedClasses += 2
        }

        if (patched == 0 && rewrittenHosts == 0) {
            error("[Multiaccounts Telemetry] No telemetry dispatch methods or host literals found - failing closed.")
        }

        val summary = hookCounts.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        println("[Multiaccounts Telemetry] Neutralized $patched telemetry dispatch methods and rewrote $rewrittenHosts host literals across $touchedClasses classes -> 0.0.0.0 ($summary).")
    }
}
