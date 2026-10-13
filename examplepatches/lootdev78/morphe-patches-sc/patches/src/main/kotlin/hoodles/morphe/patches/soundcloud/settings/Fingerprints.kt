/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.settings

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

object SettingsScreenFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/settings/main/SettingsScreenKt;",
    returnType = "V",
    filters = listOf(
        methodCall(
            smali = "Landroidx/compose/runtime/Updater;->d(Landroidx/compose/runtime/Composer;Ljava/lang/Object;Lkotlin/jvm/functions/Function2;)V"
        )
    )
)
