package app.morphe.patches.platform8tv.extension

import app.morphe.patches.all.misc.extension.sharedExtensionPatch
import app.morphe.patches.platform8tv.extension.hooks.platform8tvActivityOnCreateHook
import app.morphe.patches.platform8tv.extension.hooks.platform8tvApplicationOnCreateHook

val sharedExtensionPatch =
    sharedExtensionPatch(
        listOf("platform8"),
        platform8tvActivityOnCreateHook,
        platform8tvApplicationOnCreateHook,
    )
