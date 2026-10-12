package app.morphe.patches.shadhin.content

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import com.android.tools.smali.dexlib2.AccessFlags

object FetchStreamingUrlFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.ABSTRACT),
    returnType = Type.OBJECT,
    parameters = listOf(Type.STRING, Type.STRING, Type.STRING, "L"),
    custom = { method, _ ->
        method.annotations.flatMap { it.elements }
            .any { it.value.toString().endsWith("/streamings/url\"") }
    }
)