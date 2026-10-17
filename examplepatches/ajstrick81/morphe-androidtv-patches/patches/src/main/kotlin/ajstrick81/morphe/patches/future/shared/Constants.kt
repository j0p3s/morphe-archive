package ajstrick81.morphe.patches.future.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY = Compatibility(
        name = "Fawesome Movies & TV (Android TV)",
        packageName = "com.future.moviesByFawesomeAndroidTV",
        appIconColor = 0xFFB300,
        targets = listOf(AppTarget("19.7"))
    )
}
