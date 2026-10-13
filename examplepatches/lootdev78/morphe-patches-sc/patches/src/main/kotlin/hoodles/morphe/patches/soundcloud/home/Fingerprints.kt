/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.home

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess

object SDUIToHomeResultsFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/sdui/domain/SDUIRepository\$toHomeResults\$1;",
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/soundcloud/android/sdui/data/apidata/ApiSDUIData;",
            type = "Ljava/util/List;"
        )
    )
)
