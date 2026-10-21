package app.daniele.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    /**
     * Cinemaniac (it.papalillo.moviestowatch). The bytecode patches validate
     * exact bytes, so only the analyzed release is targeted.
     */
    val COMPATIBILITY_CINEMANIAC = Compatibility(
        name = "Cinemaniac", // App name as it appears in the Android launcher.
        packageName = "it.papalillo.moviestowatch",
        // File type of the analyzed APK (universal APK, versionCode 172).
        apkFileType = ApkFileType.APK,
        // Icon color in Morphe Manager (0xRRGGBB).
        appIconColor = 0xD32F2F,
        // SHA-256 of the signing certificate (apksigner verify --print-certs).
        signatures = setOf(
            "ca4bfb695d731fd138299522b35b185feeecc3f91e384d7c4822cef13231a2e0",
        ),
        targets = listOf(
            // Exact release the patches were developed against.
            AppTarget(version = "5.0.7"),
        ),
    )
}
