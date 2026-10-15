package app.template.patches.flashscore.misc.rating

import app.morphe.patcher.Fingerprint

// The app settings store's boolean getter (R8-renamed, lhk.a(ehk) in 26.10.2). It takes the
// settings-key enum and is the only method that logs " using getBool!". Every read of
// CAN_SHOW_RATING_BANNER and CAN_SHOW_INAPP_RATING goes through it.
internal object SettingsGetBoolFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("L"),
    strings = listOf("Undefined default value for ", " using getBool!"),
)
