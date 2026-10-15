package app.morphe.patches.platform8tv.content

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.platform8tv.shared.Constants.COMPATIBILITY_PLATFORM8TV
import app.morphe.util.returnEarly

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_PLATFORM8TV)

        execute {
            GetContentAvailabilityFingerprint.matchAll().forEach {
                it.method.returnEarly("FREE")
            }
        }
    }
