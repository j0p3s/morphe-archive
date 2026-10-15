package app.morphe.patches.shared.morphe.kotlin.collections

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.morphe.java.util.List
import com.android.tools.smali.dexlib2.AccessFlags

object Collections {
    override fun toString() = "Lkotlin/collections/CollectionsKt__CollectionsKt;"

    object EmptyListFingerprint :
        Fingerprint(
            definingClass = Collections.toString(),
            name = "emptyList",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
            returnType = List.toString(),
            parameters = emptyList(),
        )
}
