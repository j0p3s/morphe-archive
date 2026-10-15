// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/fixpushnotifications: the Firebase
// Installations request builder anchor and the constant original-certificate header. Unchanged
// except that it is always on (no settings toggle).

package app.template.patches.line.misc.push

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction

// SHA-1 of LINE's original signing certificate (CN=tsutomu horiyashiki, O=NaverJapan), the
// lineage root of the APK Signature Scheme v3.1 rotation. The deprecated GET_SIGNATURES read that
// fills the header returns this root for a rotated app. The rotated key (CN=LINE Android, used on
// SDK 33+) is 6A2927D945AEA6571E1DA5566802F25045D367BD. Both checked against the 26.15.1 bundle.
private const val LINE_ORIGINAL_CERT_SHA1 = "89396DC419292473972813922867E6973D6F5C50"

// The bundled Firebase Installations SDK's request builder (URL, String) -> HttpURLConnection. It
// reports the app's signing-certificate SHA-1 in X-Android-Cert; Google checks that against the
// API key's Android app restriction, so a re-signed build gets "BAD CONFIG" and no FCM token.
// The return type and parameters exclude the Remote Config client, which sets the same header
// with setRequestProperty.
internal object FisRequestBuilderFingerprint : Fingerprint(
    returnType = "Ljava/net/HttpURLConnection;",
    parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
    filters = listOf(
        string("X-Android-Cert"),
        methodCall(definingClass = "Ljava/net/URLConnection;", name = "addRequestProperty"),
    ),
)

@Suppress("unused")
val fixPushNotificationsPatch = bytecodePatch(
    name = "Fix push notifications",
    description = "Makes push notifications work on a re-signed build while LINE is closed, by " +
        "reporting LINE's original signing certificate to Firebase.",
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        // Overwrite the header value register right before addRequestProperty(key, value). Only
        // this call site changes; the shared certificate helpers stay intact.
        val addHeader = FisRequestBuilderFingerprint.instructionMatches[1]
        val valueRegister = (addHeader.instruction as FiveRegisterInstruction).registerE
        FisRequestBuilderFingerprint.method.addInstructions(
            addHeader.index,
            "const-string v$valueRegister, \"$LINE_ORIGINAL_CERT_SHA1\"",
        )
    }
}
