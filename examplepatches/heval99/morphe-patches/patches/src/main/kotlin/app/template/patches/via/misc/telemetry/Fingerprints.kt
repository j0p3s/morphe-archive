package app.template.patches.via.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

// The class name is kept; R8 renames logEvent itself (7.3.3: a(String, Bundle)), so calls to it
// are matched by owner and shape only. It is the class's only (String, Bundle)V method.
internal const val FIREBASE_ANALYTICS = "Lcom/google/firebase/analytics/FirebaseAnalytics;"

// The app's Firebase tracker (7.3.3: t8/b, behind the t8/f facade and the t8/a hub) sends every
// event through this sink: (String name, Map params)V turns the map into a Bundle and calls
// FirebaseAnalytics.logEvent. It is the app's only caller of logEvent.
internal object FirebaseTrackerLogEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = FIREBASE_ANALYTICS,
            parameters = listOf("Ljava/lang/String;", "Landroid/os/Bundle;"),
            returnType = "V",
        ),
    ),
    custom = { method, _ -> method.implementation != null },
)
