package app.tower.patches.tower

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal const val TOWER_PACKAGE = "com.TechTreeGames.TheTower"
internal const val TOWER_CLONE_PACKAGE = "com.TechTreeGames.LocTower"

/** libil2cpp.so of v29.0.6 (build 1237), arm64-v8a. Patches refuse to run on any other build. */
internal const val IL2CPP_PATH = "lib/arm64-v8a/libil2cpp.so"
internal const val IL2CPP_SHA256 = "db7b25b94beafbde80bcfbfedd7e0a53f8df5862d5cdd342d22f48de4c73b115"

internal val COMPATIBILITY_TOWER = Compatibility(
    name = "The Tower",
    packageName = TOWER_PACKAGE,
    apkFileType = ApkFileType.APKM,
    appIconColor = 0x3A2F8C,
    targets = listOf(
        AppTarget(version = "29.0.6")
    )
)
