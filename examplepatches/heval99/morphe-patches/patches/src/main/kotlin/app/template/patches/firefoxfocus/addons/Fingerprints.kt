package app.template.patches.firefoxfocus.addons

import app.morphe.patcher.Fingerprint

internal const val EXTENSION_CLASS_DESCRIPTOR = "Lapp/template/extension/firefoxfocus/FocusAddons;"

internal const val WEB_EXTENSION_CONTROLLER = "Lorg/mozilla/geckoview/WebExtensionController;"
internal const val PROMPT_DELEGATE = "Lorg/mozilla/geckoview/WebExtensionController\$PromptDelegate;"
internal const val WEB_EXTENSION_ACTION = "Lorg/mozilla/geckoview/WebExtension\$Action;"
internal const val GECKO_RESULT = "Lorg/mozilla/geckoview/GeckoResult;"
internal const val FOCUS_APPLICATION = "Lorg/mozilla/focus/FocusApplication;"
internal const val MENU_BUILDER = "Lmozilla/components/browser/menu/WebExtensionBrowserMenuBuilder;"
internal const val FUNCTION0 = "Lkotlin/jvm/functions/Function0;"

internal const val SHOW_ADDONS_IN_MENU = "showAddonsInMenu"
internal const val ON_ADDONS_MANAGER_TAPPED = "onAddonsManagerTapped"

// Focus is built with -dontobfuscate and GeckoView's consumer rules keep org.mozilla.geckoview.**
// with their names, so these class and method names are stable anchors (verified in 158.0).

// WebExtensionController.setPromptDelegate(PromptDelegate): Android Components' GeckoEngine
// registers its prompt delegate here from FocusApplication.onCreate (WebExtensionSupport.initialize).
object SetPromptDelegateFingerprint : Fingerprint(
    definingClass = WEB_EXTENSION_CONTROLLER,
    name = "setPromptDelegate",
    returnType = "V",
    parameters = listOf(PROMPT_DELEGATE),
)

object FocusApplicationOnCreateFingerprint : Fingerprint(
    definingClass = FOCUS_APPLICATION,
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
)

// WebExtension$Action.openPopup(GeckoResult<GeckoSession>, String popupUri): both popup paths end
// here (the action click's onTogglePopup and the controller's OpenPopup message).
object ActionOpenPopupFingerprint : Fingerprint(
    definingClass = WEB_EXTENSION_ACTION,
    name = "openPopup",
    returnType = "V",
    parameters = listOf(GECKO_RESULT, "Ljava/lang/String;"),
)
