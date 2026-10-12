package app.morphe.patches.hoichoi.user

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOI
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOITV
import app.morphe.util.returnEarly

@Suppress("unused")
val anonymousUserPatch = bytecodePatch(
    name = "Anonymous user",
    description = "Log in as anonymous subscribed user.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOICHOI, COMPATIBILITY_HOICHOITV)

    execute {
        IsLoggedInFingerprint.matchAll().forEach {
            it.method.returnEarly(true)
        }

        IsSubscribedFingerprint.matchAll().forEach {
            it.method.returnEarly(true)
        }
    }
}
