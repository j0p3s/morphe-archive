package app.morphe.patches.shared.morphe.kotlin

import kotlin.String

object String {
    override fun toString(): String = String::class.java.descriptorString()
}