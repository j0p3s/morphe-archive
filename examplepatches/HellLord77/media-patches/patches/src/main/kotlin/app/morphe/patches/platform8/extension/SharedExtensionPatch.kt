package app.morphe.patches.platform8.extension

import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.patches.platform8.extension.hooks.platform8ActivityOnCreateHook
import app.morphe.patches.platform8.extension.hooks.platform8ApplicationOnCreateHook

val sharedExtensionPatch =
    sharedExtensionPatch(
        listOf("platform8"),
        platform8ActivityOnCreateHook,
        platform8ApplicationOnCreateHook,
    )
