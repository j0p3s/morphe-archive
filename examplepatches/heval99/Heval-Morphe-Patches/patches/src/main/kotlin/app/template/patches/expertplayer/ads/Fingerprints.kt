package app.template.patches.expertplayer.ads

import app.morphe.patcher.Fingerprint

// PairIP's license check, run from com.pairip.application.Application.attachBaseContext. This app
// has the license layer only (no VMRunner, SignatureCheck or StartupLauncher). Without Play's
// license the check kills the process. The same no-op is what pairip-disabler applies to this
// layer. The class and method names are kept by PairIP's R8 rules, so they are stable.
object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
