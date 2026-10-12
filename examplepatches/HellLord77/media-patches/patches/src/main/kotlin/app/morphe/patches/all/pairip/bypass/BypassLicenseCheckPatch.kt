package app.morphe.patches.all.pairip.bypass

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.all.pairip.shared.patches.licensecheck.licenseClient.checkLicensePatch

@Suppress("unused")
val bypassLicenseCheckPatch = bytecodePatch(
    name = "Bypass PairIP license check",
    description = "Bypasses Play Integrity API (pairip) client-side license check.",
    default = false,
) {
    dependsOn(checkLicensePatch)
}