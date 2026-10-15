package app.template.patches.kakaotalk.shared

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.value.MethodEncodedValue
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

// KakaoTalk keeps Kotlin's @DebugMetadata on coroutine classes (c = original class name, m =
// original suspend function name) and the EnclosingMethod annotation that points back at the
// R8-renamed method that created them. Together they name an obfuscated method by its source name.

private const val DEBUG_METADATA = "Lkotlin/coroutines/jvm/internal/DebugMetadata;"
private const val ENCLOSING_METHOD = "Ldalvik/annotation/EnclosingMethod;"

/**
 * The method enclosing the coroutine class whose @DebugMetadata has `c = [className]` (and
 * `m = [methodName]` when given), or null when no such class exists.
 */
internal fun BytecodePatchContext.methodEnclosingCoroutine(className: String, methodName: String? = null): MutableMethod? {
    val coroutine = classDefByOrNull { classDef ->
        classDef.annotations.any { annotation ->
            annotation.type == DEBUG_METADATA && annotation.elements.let { elements ->
                fun value(name: String) = (elements.firstOrNull { it.name == name }?.value as? StringEncodedValue)?.value
                value("c") == className && (methodName == null || value("m") == methodName)
            }
        }
    } ?: return null

    val enclosing = coroutine.annotations.firstOrNull { it.type == ENCLOSING_METHOD }
        ?.elements?.firstOrNull { it.name == "value" }?.value as? MethodEncodedValue
        ?: throw PatchException("${coroutine.type} ($className) has no EnclosingMethod")
    return mutableClassDefBy(enclosing.value.definingClass).methods.firstOrNull { it.matches(enclosing.value) }
        ?: throw PatchException("Enclosing method ${enclosing.value} of $className not found")
}

internal fun MutableMethod.matches(reference: MethodReference) =
    name == reference.name && returnType == reference.returnType &&
        parameterTypes.map(CharSequence::toString) == reference.parameterTypes.map(CharSequence::toString)

/**
 * Resolves a method by its coroutine metadata, cross-checked against [fallback] (a shape-based
 * lookup). Either one is enough; when both resolve they must agree.
 */
internal fun BytecodePatchContext.resolveByCoroutine(
    label: String,
    className: String,
    methodName: String? = null,
    fallback: () -> MutableMethod?,
): MutableMethod {
    val byMetadata = methodEnclosingCoroutine(className, methodName)
    val byShape = fallback()
    if (byMetadata != null && byShape != null &&
        (byMetadata.definingClass != byShape.definingClass || !byMetadata.matches(byShape))
    ) {
        throw PatchException("$label: metadata anchor ${byMetadata.definingClass}->${byMetadata.name} " +
            "and shape anchor ${byShape.definingClass}->${byShape.name} disagree")
    }
    return byMetadata ?: byShape ?: throw PatchException("$label: neither anchor matched")
}
