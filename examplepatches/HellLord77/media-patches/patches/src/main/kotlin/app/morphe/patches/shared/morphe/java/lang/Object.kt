package app.morphe.patches.shared.morphe.java.lang

object Object {
    override fun toString(): String = Any::class.java.descriptorString()
}
