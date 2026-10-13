package app.aeong.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {
    val COMPATIBILITY_DISCORD = Compatibility(
        name = "Discord",
        packageName = "com.discord",
        apkFileType = ApkFileType.APKM,
        appIconColor = 0x5865F2,
        targets = listOf(
            AppTarget(
                version = "347.5 - Alpha"
            ),
            AppTarget(
                version = "344.5 - Alpha"
            )
        )
    )
}
