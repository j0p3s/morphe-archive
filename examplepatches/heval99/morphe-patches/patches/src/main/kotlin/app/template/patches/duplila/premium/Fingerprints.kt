package app.template.patches.duplila.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// App classes are not obfuscated (Kotlin source names, matching @DebugMetadata).
internal const val BILLING_MANAGER = "Lcom/duplila/screenshare/billing/BillingManager;"

// BillingManager.userPaid(): the suspend premium check behind every gate (MainActivity's
// isUserPaid / banner / consent branch, SelectionScreenState.enabledPremium, the preferences
// screen, CheckoutActivity and ScreenMirrorActivity's watermark, video-size, recording and key
// checks). It switches to Dispatchers.IO via withContext and runs hasActivePurchase (Play
// queryPurchasesAsync on-device) for both subscriptions under a 10 s withTimeoutOrNull. There
// is no backend.
internal object UserPaidFingerprint : Fingerprint(
    definingClass = BILLING_MANAGER,
    name = "userPaid",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lkotlin/coroutines/Continuation;"),
    filters = listOf(
        methodCall(definingClass = "Lkotlinx/coroutines/BuildersKt;", name = "withContext"),
    ),
)
