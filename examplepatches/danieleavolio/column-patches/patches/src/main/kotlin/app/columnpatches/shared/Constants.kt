package app.columnpatches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {

    /**
     * Column (com.columnapp.column), developed by Column srl.
     *
     * Confirmed target: versionName 1.3.7 / versionCode 327, downloaded as an XAPK
     * (base apk + config splits for abi/density/locale).
     *
     * Everything below was verified by decompiling the base APK:
     *  - The application is a Capacitor (WebView) shell. The native layer is only
     *    MainActivity + com.columnapp.column.ColumnWebView + a MetaPixel stub.
     *  - The web app lives in the assets/public directory (JS bundle) and talks to api.column.news.
     *  - Entitlements (premium / freemium) are issued by the backend and validated
     *    through RevenueCat (@revenuecat/purchases-capacitor). There is no client
     *    side premium flag to toggle.
     *  - The only premium-exclusive perk in the app's own constants is "Zero
     *    pubblicità" (no ads). AI summaries and podcasts are part of the free tier.
     */
    val COLUMN = Compatibility(
        name = "Column",
        packageName = "com.columnapp.column",
        // Column is distributed as an XAPK (base apk + abi/density/locale splits).
        // It is not listed on APKMirror or Uptodown; the XAPK used to develop these
        // patches is version 1.3.7 from APKPure.
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x0F172A,
        targets = listOf(
            AppTarget(version = "1.3.7"),
            AppTarget(version = null, isExperimental = true),
        )
    )
}
