// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/removeads/Fingerprints.kt and
// patches/line/hideadviews/Fingerprints.kt: the Smart Channel banner receive lambdas and the LINE
// Ads view classes. The receive lambdas are now found by their Thrift error strings instead of the
// R8-renamed org.apache.thrift class, and the Smart Channel host by the SmartChannelViewLayout cast.

package app.template.patches.line.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.checkCast
import app.morphe.patcher.string

internal const val SMART_CHANNEL_VIEW_LAYOUT = "Lcom/linecorp/line/admolin/smartch/v2/view/SmartChannelViewLayout;"

// The Smart Channel getBanners receive branch. R8 merged it into a shared packed-switch lambda,
// so only the case after the op-name string may change.
internal object GetBannersReceiveFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("getBanners"),
        string("getBanners failed: unknown result"),
    ),
)

// Same for getPrefetchableBanners (banners cached ahead of display).
internal object GetPrefetchableBannersReceiveFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("getPrefetchableBanners"),
        string("getPrefetchableBanners failed: unknown result"),
    ),
)

// The chat list's Smart Channel controller: its constructor takes the host FrameLayout, inflates
// the banner layout into it and casts the result to SmartChannelViewLayout.
internal object SmartChannelControllerFingerprint : Fingerprint(
    name = "<init>",
    returnType = "V",
    parameters = listOf("Landroid/widget/FrameLayout;", "L", "L", "L", "L"),
    filters = listOf(checkCast(SMART_CHANNEL_VIEW_LAYOUT)),
)

internal object LadAdViewAttachFingerprint : Fingerprint(
    definingClass = "Lcom/linecorp/line/ladsdk/ui/common/view/lifecycle/LadAdView;",
    name = "onAttachedToWindow",
    returnType = "V",
    parameters = listOf(),
)

internal object LyadAdViewAttachFingerprint : Fingerprint(
    definingClass = "Lcom/linecorp/line/ladsdk/ui/v2/common/lifecycle/LyadAdView;",
    name = "onAttachedToWindow",
    returnType = "V",
    parameters = listOf(),
)
