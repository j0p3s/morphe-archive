package app.morphe.patches.iscreen.content

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.iscreen.shared.Constants.COMPATIBILITY_ISCREEN
import app.morphe.util.returnEarly

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = false,
    ) {
        compatibleWith(COMPATIBILITY_ISCREEN)

        execute {
            GetPremiumFingerprint.matchAll().forEach {
                it.method.returnEarly(false)
            }

            GetTvodFingerprint.matchAll().forEach {
                it.method.returnEarly(false)
            }
        }
    }
