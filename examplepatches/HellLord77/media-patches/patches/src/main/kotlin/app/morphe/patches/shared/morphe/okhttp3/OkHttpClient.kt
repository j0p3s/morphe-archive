package app.morphe.patches.shared.morphe.okhttp3

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object OkHttpClient {
    override fun toString() = "Lokhttp3/OkHttpClient;"

    object Builder {
        override fun toString() = $$"Lokhttp3/OkHttpClient$Builder;"

        object AddInterceptorFingerprint :
            Fingerprint(
                definingClass = Builder.toString(),
                name = "addInterceptor",
                accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
                returnType = Builder.toString(),
                parameters = listOf(Interceptor.toString()),
            )

        object BuildFingerprint :
            Fingerprint(
                definingClass = Builder.toString(),
                name = "build",
                accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
                returnType = OkHttpClient.toString(),
                parameters = emptyList(),
            )
    }
}
