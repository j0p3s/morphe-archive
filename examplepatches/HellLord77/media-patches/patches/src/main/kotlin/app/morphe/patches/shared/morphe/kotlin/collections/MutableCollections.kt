package app.morphe.patches.shared.morphe.kotlin.collections

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import app.morphe.patches.shared.morphe.java.lang.Iterable
import app.morphe.patches.shared.morphe.java.util.Collection
import com.android.tools.smali.dexlib2.AccessFlags

object MutableCollections {
    override fun toString() = "Lkotlin/collections/CollectionsKt__MutableCollectionsKt;"

    object AddAllFingerprint :
        Fingerprint(
            definingClass = MutableCollections.toString(),
            name = "addAll",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
            returnType = Type.boolean,
            parameters = listOf(Collection.toString(), Iterable.toString()),
        )
}
