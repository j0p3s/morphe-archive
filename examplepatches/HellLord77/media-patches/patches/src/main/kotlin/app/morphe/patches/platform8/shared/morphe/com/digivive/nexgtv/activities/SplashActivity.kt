package app.morphe.patches.platform8.shared.morphe.com.digivive.nexgtv.activities

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import com.android.tools.smali.dexlib2.AccessFlags

object SplashActivity {
    override fun toString() = "Lcom/digivive/nexgtv/activities/SplashActivity;"

    object GoToMainActivityFingerprint :
        Fingerprint(
            definingClass = SplashActivity.toString(),
            name = "goToMainActivity",
            accessFlags = listOf(AccessFlags.PRIVATE),
            returnType = Type.void,
            parameters = emptyList(),
        )

    object CallNextActivityFingerprint :
        Fingerprint(
            definingClass = SplashActivity.toString(),
            name = "callNextActivity",
            accessFlags = listOf(AccessFlags.PRIVATE),
            returnType = Type.void,
            parameters = listOf(Type.int),
        )
}
