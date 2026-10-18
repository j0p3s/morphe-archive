package app.template.patches.digisport.misc.update

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// MainActivityBase.checkUpdate(): asks Play's AppUpdateManager for an update and starts the
// update flow. MainActivity.onCreate is its only caller, and it is the app's only
// AppUpdateManagerFactory.create call.
internal object CheckUpdateFingerprint : Fingerprint(
    definingClass = "Lro/rcsrds/digisport/ui/main/MainActivityBase;",
    name = "checkUpdate",
    accessFlags = listOf(AccessFlags.PROTECTED, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/play/core/appupdate/AppUpdateManagerFactory;",
            name = "create",
        ),
    ),
    custom = { method, _ -> method.implementation != null },
)
