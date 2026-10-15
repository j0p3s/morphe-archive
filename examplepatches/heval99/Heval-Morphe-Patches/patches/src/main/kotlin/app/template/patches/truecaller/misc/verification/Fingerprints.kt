package app.template.patches.truecaller.misc.verification

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val OTP_SMS_API = "Lcom/truecaller/wizard/verification/otp/sms/OtpSmsApi;"

// The verification wizard picks how it reads the code (OtpSmsApi.GOOGLE = SMS Retriever,
// OtpSmsApi.SMS = the SMS inbox): the remote-config value, falling back to SMS when it is GOOGLE
// and Google Play services cannot be used. R8 merged that lambda with 28 unrelated ones into one
// Function0.invoke() behind a switch, so the method is found by its GOOGLE-then-SMS reads and the
// patch only edits the branch that compares against GOOGLE.
internal object OtpSmsApiSelectionFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = OTP_SMS_API, name = "GOOGLE"),
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = OTP_SMS_API, name = "SMS"),
    ),
    custom = { method, _ ->
        AccessFlags.PUBLIC.isSet(method.accessFlags) && AccessFlags.FINAL.isSet(method.accessFlags)
    },
)
