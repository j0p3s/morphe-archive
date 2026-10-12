package app.morphe.patches.shared.morphe.java.util

import kotlin.collections.ArrayList

object ArrayList {
    override fun toString(): String = ArrayList::class.java.descriptorString()
}