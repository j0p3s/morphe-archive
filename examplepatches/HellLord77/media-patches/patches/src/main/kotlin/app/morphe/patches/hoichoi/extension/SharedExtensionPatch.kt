package app.morphe.patches.hoichoi.extension

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.misc.extension.SHARED_UTILS_EXTENSION_CLASS
import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.patches.hoichoi.extension.hooks.hoichoiActivityOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoiApplicationOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoitvActivityOnCreateHook
import app.morphe.patches.hoichoi.extension.hooks.hoichoitvApplicationOnCreateHook
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOI
import app.morphe.patches.hoichoi.shared.Constants.COMPATIBILITY_HOICHOITV
import app.morphe.patches.shared.extension.app.morphe.patcher.patch.compatibleWith

val sharedExtensionPatch = bytecodePatch {
    dependsOn(sharedExtensionPatch(listOf("hoichoi")))

    finalize {
        var versionCode = packageMetadata.versionCode.toInt()
        if (COMPATIBILITY_HOICHOI.compatibleWith(versionCode)) {
            hoichoiActivityOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
            hoichoiApplicationOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
        } else if (COMPATIBILITY_HOICHOITV.compatibleWith(versionCode)) {
            hoichoitvActivityOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
            hoichoitvApplicationOnCreateHook(SHARED_UTILS_EXTENSION_CLASS)
        }
    }
}
