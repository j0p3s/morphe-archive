package app.morphe.patches.shared.morphe.kotlin.collections

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import app.morphe.patches.shared.morphe.java.util.ArrayList
import app.morphe.patches.shared.morphe.java.util.List
import com.android.tools.smali.dexlib2.AccessFlags

object Collections {
    override fun toString() = "Lkotlin/collections/CollectionsKt__CollectionsKt;"

    object ArrayListOfFingerprint : Fingerprint(
        definingClass = Collections.toString(),
        name = "arrayListOf",
        accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.VARARGS),
        returnType = ArrayList.toString(),
        parameters = listOf(Type.OBJECT_ARRAY),
    )

    object ListOfFingerprint : Fingerprint(
        definingClass = Collections.toString(),
        name = "listOf",
        accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.VARARGS),
        returnType = List.toString(),
        parameters = listOf(Type.OBJECT_ARRAY),
    )
}