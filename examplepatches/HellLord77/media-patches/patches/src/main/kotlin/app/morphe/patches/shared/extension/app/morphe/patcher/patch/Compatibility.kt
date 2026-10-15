package app.morphe.patches.shared.extension.app.morphe.patcher.patch

import app.morphe.patcher.patch.Compatibility

fun Compatibility.compatibleWith(version: String): Boolean {
    return targets.any { target -> target.version == version }
}

fun Compatibility.compatibleWith(versionCode: Int): Boolean {
    return targets.any { target -> target.versionCodes?.containsValue(versionCode) ?: false }
}
