package app.morphe.patches.shared.extension.kotlin.collections

inline fun <T> MutableSet<T>.replaceAll(operator: (T) -> T) {
    val elements = map(operator)
    clear()
    addAll(elements)
}