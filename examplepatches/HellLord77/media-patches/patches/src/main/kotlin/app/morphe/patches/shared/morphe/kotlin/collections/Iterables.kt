package app.morphe.patches.shared.morphe.kotlin.collections

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import app.morphe.patches.shared.morphe.java.util.Collection
import com.android.tools.smali.dexlib2.AccessFlags

object Iterables {
    override fun toString() = "Lkotlin/collections/CollectionsKt__IterablesKt;"

    object CollectionSizeOrDefault :
        Fingerprint(
            definingClass = Iterables.toString(),
            name = "collectionSizeOrDefault",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
            returnType = Type.int,
            parameters = listOf(Collection.toString()),
        )
}
