package app.morphe.patches.hoichoi.content

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOI
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOITV
import app.morphe.util.returnEarly

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_HOICHOI, COMPATIBILITY_HOICHOITV)

        execute {
            GetMonetizationModelFingerprint.matchAll().forEach {
                it.method.returnEarly("FREE")
            }

            IsFreeContentFingerprint.matchAll().forEach {
                it.method.returnEarly(true)
            }

            IsLoginRequiredFingerprint.matchAll().forEach {
                it.method.returnEarly(false)
            }
        }
    }
