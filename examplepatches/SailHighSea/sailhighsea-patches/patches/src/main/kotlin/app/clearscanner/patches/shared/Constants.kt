package app.clearscanner.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_CLEAR_SCANNER = Compatibility(
        name = "Clear Scanner",
        packageName = "com.indymobileapp.document.scanner",
        // The file I developed against is an XAPK (base APK + config splits).
        // Make sure this matches the file type offered on APKMirror/UpToDown.
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0x1E88E5,
        targets = listOf(
            AppTarget(version = "10.2.18")
        )
    )
}
