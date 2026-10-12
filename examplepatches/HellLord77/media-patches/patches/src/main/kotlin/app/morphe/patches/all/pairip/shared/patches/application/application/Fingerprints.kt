package app.morphe.patches.all.pairip.shared.patches.application.application

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import com.android.tools.smali.dexlib2.AccessFlags

internal object AttachBaseContextFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/application/Application;",
    name = "attachBaseContext",
    accessFlags = listOf(AccessFlags.PROTECTED),
    returnType = Type.void,
    parameters = listOf(Type.CONTEXT),
)
