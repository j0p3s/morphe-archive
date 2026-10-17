package app.template.patches.luckyclick.misc.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal const val AD_FREE_PERMANENT = "AD_FREE_PERMANENT"

// The Play purchase restore, run from MainActivity.onResume (queryPurchasesAsync). It is the
// app's PurchasesResponseListener: it acknowledges or consumes each purchase, then stores
// "any ad-free purchase found" into the in-memory ad-free state (Boolean.valueOf + setValue) and
// "permanent purchase found" as AD_FREE_PERMANENT. With no purchase it resets the state to false.
// The listener method keeps its library name; the class is R8-renamed (i9.o in 3.28).
internal object PurchasesRestoreFingerprint : Fingerprint(
    name = "onQueryPurchasesResponse",
    returnType = "V",
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;", "Ljava/util/List;"),
    filters = listOf(
        methodCall(definingClass = "Ljava/lang/Boolean;", name = "valueOf"),
        string(AD_FREE_PERMANENT),
        methodCall(definingClass = "Landroid/content/SharedPreferences\$Editor;", name = "putBoolean"),
    ),
    custom = { _, classDef -> !classDef.type.startsWith("Lcom/android/billingclient/") },
)
