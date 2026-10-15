package app.template.patches.truecaller.misc.telemetry

import app.morphe.patcher.Fingerprint

// Kept CleverTap wrapper interface; the app's implementation of it is R8-renamed, so the patch
// finds it by this interface.
internal const val CLEVERTAP_MANAGER = "Lcom/truecaller/clevertap/CleverTapManager;"

// AppsFlyer's public API class. It only declares abstract methods; the implementation is an
// SDK-internal subclass whose name AppsFlyer itself rotates (AFa1ySDK here), so the patch finds
// it by its superclass.
internal const val APPSFLYER_LIB = "Lcom/appsflyer/AppsFlyerLib;"

// Application.onCreate calls this to switch the app start tracker on; while it stays off every
// onActivityCreate/onServiceCreate/onBroadcastReceive log returns early.
internal object AppStartTrackerEnableTrackingFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/analytics/technical/AppStartTracker;",
    name = "enableTracking",
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)
