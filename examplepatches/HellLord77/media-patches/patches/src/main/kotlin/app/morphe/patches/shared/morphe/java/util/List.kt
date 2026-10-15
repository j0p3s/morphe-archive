@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package app.morphe.patches.shared.morphe.java.util

import java.util.List

object List {
    override fun toString(): String = List::class.java.descriptorString()
}
