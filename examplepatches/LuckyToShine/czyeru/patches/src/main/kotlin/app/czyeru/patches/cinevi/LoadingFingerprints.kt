package app.czyeru.patches.cinevi

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

private const val BASE_VIEW = "Lchuangyuan/ycj/videolibrary/widget/BaseView;"

/**
 * `BaseView.A(int visibility)` of the bundled player library (not obfuscated) shows or hides the
 * full-screen "first load" layer. Visible: it starts the frame animation and hides the controls.
 * Otherwise: it stops the animation. Either way it ends by setting the layer's visibility.
 */
object LoadFirstVisibilityFingerprint : Fingerprint(
    definingClass = BASE_VIEW,
    name = "A",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("I"),
    filters = listOf(
        methodCall(definingClass = BASE_VIEW, name = "H"),
        methodCall(definingClass = BASE_VIEW, name = "J")
    )
)
