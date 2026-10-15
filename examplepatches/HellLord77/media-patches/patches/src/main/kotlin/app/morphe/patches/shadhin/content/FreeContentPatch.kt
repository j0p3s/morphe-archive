package app.morphe.patches.shadhin.content

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shadhin.shared.Constants.COMPATIBILITY_SHADHIN
import app.morphe.patches.shadhin.shared.morphe.com.gm.shadhin.MainContentModel

@Suppress("unused")
val freeContentPatch =
    bytecodePatch(
        name = "Free content",
        description = "Marks content as free.",
        default = true,
    ) {
        compatibleWith(COMPATIBILITY_SHADHIN)

        dependsOn(MainContentModel.isPaidPatch)
    }
