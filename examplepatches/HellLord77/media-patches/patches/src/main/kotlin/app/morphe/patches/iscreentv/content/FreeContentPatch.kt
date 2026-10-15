package app.morphe.patches.iscreentv.content

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.iscreentv.shared.Constants.COMPATIBILITY_ISCREENTV
import app.morphe.util.returnEarly

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = false,
    ) {
        compatibleWith(COMPATIBILITY_ISCREENTV)

        execute {
            GetPremiumFingerprint.matchAll().forEach {
                it.method.returnEarly(false)
            }

            GetTvodFingerprint.matchAll().forEach {
                it.method.returnEarly(false)
            }
        }
    }
