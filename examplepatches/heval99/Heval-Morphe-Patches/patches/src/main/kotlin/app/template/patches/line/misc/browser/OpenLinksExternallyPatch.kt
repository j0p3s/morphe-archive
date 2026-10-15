// Based on andrewliang25/morphe-patches (GPL-3.0), patches/line/externalbrowser: OpenUriActivity's
// intent builder gets the EXTERNAL_WITHOUT_CUSTOMTABS mode. The builder is now found by its intent
// extra keys and the mode parameter by its enum constant, instead of a pinned parameter list (one
// R8-renamed parameter type changed in 26.15.1). Always on when selected (no settings toggle).

package app.template.patches.line.misc.browser

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_LINE
import com.android.tools.smali.dexlib2.AccessFlags

private const val EXTERNAL_MODE = "EXTERNAL_WITHOUT_CUSTOMTABS"

// The single intent builder for opening a URI in com.linecorp.browser.OpenUriActivity. For
// http/https URIs it switches on the browser-mode enum parameter to choose Custom Tabs, the in-app
// browser or an external app; other schemes (LIFF, line://, tel:) skip the switch.
internal object OpenUriIntentBuilderFingerprint : Fingerprint(
    returnType = "Landroid/content/Intent;",
    strings = listOf("CUSTOM_TABS_MODE", "SHOULD_USE_IAB", "IS_NEW_TAB_IN_EXT"),
    custom = { _, classDef -> classDef.type.startsWith("Lcom/linecorp/browser/OpenUriActivity") },
)

@Suppress("unused")
val openLinksExternallyPatch = bytecodePatch(
    name = "Open links in external browser",
    description = "Opens web links in your default browser instead of LINE's in-app browser. " +
        "LINE links and LIFF mini apps stay in LINE.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_LINE)

    execute {
        OpenUriIntentBuilderFingerprint.method.apply {
            // The mode parameter: the one whose type is an enum with EXTERNAL_WITHOUT_CUSTOMTABS.
            val parameters = parameterTypes.map(CharSequence::toString)
            val modeIndex = parameters.indices.singleOrNull { index ->
                classDefByOrNull(parameters[index])?.staticFields?.any { it.name == EXTERNAL_MODE } == true
            } ?: throw PatchException("No browser-mode parameter on $definingClass->$name")
            val modeType = parameters[modeIndex]

            // p-register of that parameter: p0 is `this` unless static; J and D take two.
            val firstParameter = if (AccessFlags.STATIC.isSet(accessFlags)) 0 else 1
            val register = firstParameter + parameters.take(modeIndex).sumOf { if (it == "J" || it == "D") 2 else 1 }

            // Every web URI now takes LINE's own external-browser branch, which has its own
            // fallback when no browser is installed. Only a null check and the mode switch read
            // the parameter.
            addInstructions(0, "sget-object p$register, $modeType->$EXTERNAL_MODE:$modeType")
        }
    }
}
