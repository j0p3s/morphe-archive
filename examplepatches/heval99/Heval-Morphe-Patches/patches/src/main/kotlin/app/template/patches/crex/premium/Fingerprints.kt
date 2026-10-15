package app.template.patches.crex.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal const val MY_APPLICATION =
    "Lin/cricketexchange/app/cricketexchange/utils/application/MyApplication;"
internal const val HOME_ACTIVITY = "Lin/cricketexchange/app/cricketexchange/activities/HomeActivity;"

// The subscription is stored as the "expiry_date" string ("yyyy-MM-dd") in the "payment"
// SharedPreferences, written from the server's premium info on login/sync and after a purchase.
// Two client-side checks compare it with today's date, and both are true when the plan has
// EXPIRED (default "0000-00-00"), i.e. they mean "show ads / not premium".

// MyApplication's "ads eligible" getter: today.after(expiry). The only no-arg boolean on the
// Application (name kept) that reads "expiry_date"; ~160 call sites (ad managers, app-open,
// floating score, every screen's adsVisibility flag) go through it.
internal object AdsEligibleFingerprint : Fingerprint(
    definingClass = MY_APPLICATION,
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        string("expiry_date"),
        string("0000-00-00"),
        methodCall(definingClass = "Ljava/util/Date;", name = "after"),
    ),
)

// HomeActivity's copy of the same check: it caches today.after(expiry) in a static boolean
// (default true in <clinit>) that the home tabs, matches and series screens read directly.
internal object HomeExpiryCheckFingerprint : Fingerprint(
    definingClass = HOME_ACTIVITY,
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        string("payment"),
        string("expiry_date"),
        methodCall(definingClass = "Ljava/util/Date;", name = "after"),
        opcode(Opcode.SPUT_BOOLEAN),
    ),
)
