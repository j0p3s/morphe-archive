package app.template.patches.yahoosports.betting

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// BettingImageBannerCtrl's data method (12.10.3: m2(Object)), the only caller of
// SportsbookPromotionHelper.shouldShowBettingImageBanner(String). Both class names and the helper
// method name are kept. When the helper says no, the method falls back to the server's
// bettingPromo banner, so the whole method is the gate, not the helper.
internal object BettingImageBannerBindFingerprint : Fingerprint(
    definingClass = "Lcom/yahoo/mobile/ysports/ui/card/bettingimagebanner/control/BettingImageBannerCtrl;",
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/yahoo/mobile/ysports/sportsbook/promotions/SportsbookPromotionHelper;",
            name = "shouldShowBettingImageBanner",
        ),
    ),
)
