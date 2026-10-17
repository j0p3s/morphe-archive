// Based on o0b/morphe-patches (GPL-3.0), patches/src/main/kotlin/app/template/patches/businesscalendar2/pro/
// Fingerprints.kt: the two SettingsHelper$ProStatus preference getters.
package app.template.patches.bizcal.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

private const val PRO_STATUS = "Lcom/appgenix/bizcal/data/settings/SettingsHelper\$ProStatus;"

// Pro state is a feature-set bitmask (ProUtil.sFeatureSet, full package = 7). Both of ProUtil's
// computations (getCurrentFeatureSet and getCurrentFeatureSetAndCheckForProApps) read these two
// getters before the trial, legacy key-app and Play Billing paths, and billing's computeProStatus
// maps force-pro straight to 7 before any downgrade. Every isFeatureEnabled /
// isAnyProFeatureEnabled / isFullProEnabled gate, the watch trial and the Go Pro drawer item derive
// from them. The app code is not obfuscated; the preference keys pin the bodies.

// getCompleteProStatus(Context): the sticky "full pro purchased" flag, boolean pref "result".
object CompleteProStatusFingerprint : Fingerprint(
    definingClass = PRO_STATUS,
    name = "getCompleteProStatus",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("result"),
        methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
    ),
)

// getForceProStatus(Context): the developer force-pro override, boolean pref "status".
object ForceProStatusFingerprint : Fingerprint(
    definingClass = PRO_STATUS,
    name = "getForceProStatus",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("status"),
        methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
    ),
)
