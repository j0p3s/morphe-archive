package app.template.patches.shared

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.SupportedAbi

object Constants {

    val BATTERYGURU_COMPATIBILITY = Compatibility(
        name = "Battery Guru",
        packageName = "com.paget96.batteryguru",
        appIconColor = 0x146478,
        targets = listOf(
            AppTarget(version = "2.5.0.8", versionCode = 732),
        )
    )

    val PRIVACYKIT_COMPATIBILITY = Compatibility(
        name = "Privacy Kit",
        packageName = "com.sal.privacykit",
        appIconColor = 0x1A73E8,
        targets = listOf(
            AppTarget(version = "2.0.5"),
            AppTarget(version = "2.2"),
            AppTarget(version = "2.3.1"),
            AppTarget(version = "3.2"),
            AppTarget(version = "3.4"),
            AppTarget(version = "3.5"),
            AppTarget(version = "3.5.1"),
            AppTarget(version = "3.6"),
        )
    )

    val PROTONVPN_COMPATIBILITY = Compatibility(
        name = "Proton VPN",
        packageName = "ch.protonvpn.android",
        appIconColor = 0x6D4AFF,
        targets = listOf(
            AppTarget(version = "5.20.57.0", versionCode = 605205700),
        )
    )

    val PROTONVPN_REPRODUCIBLE_COMPATIBILITY = Compatibility(
        name = "Proton VPN Reproducible",
        packageName = "ch.protonvpn.android.reproducible",
        appIconColor = 0x6D4AFF,
        targets = listOf(
            AppTarget(version = "5.20.57.0", versionCode = 605205700),
        )
    )
}