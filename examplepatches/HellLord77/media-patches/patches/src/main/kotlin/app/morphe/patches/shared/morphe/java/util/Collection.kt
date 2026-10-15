package app.morphe.patches.shared.morphe.java.util

import kotlin.collections.Collection

object Collection {
    override fun toString(): String = Collection::class.java.descriptorString()
}
