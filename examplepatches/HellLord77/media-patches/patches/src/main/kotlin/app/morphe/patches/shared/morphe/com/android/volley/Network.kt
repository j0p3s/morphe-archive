package app.morphe.patches.shared.morphe.com.android.volley

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object Network {
    override fun toString() = "Lcom/android/volley/Network;"

    object PerformRequestFingerPrint :
        Fingerprint(
            definingClass = Network.toString(),
            name = "performRequest",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.ABSTRACT),
            returnType = NetworkResponse.toString(),
            parameters = listOf(Request.toString()),
        )
}
