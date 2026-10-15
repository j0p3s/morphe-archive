package app.template.patches.truecaller.misc.gms

import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import app.template.patches.shared.misc.gms.gmsCoreSupportPatch
import app.template.patches.truecaller.misc.signature.spoofSignaturePatch

// Truecaller's Google login (social_login.google, through androidx.credentials' Play services
// provider) and its push messages (FCM) go through Google Play services, which rejects the
// re-signed app, so they need MicroG RE. Off by default: everything else works without it, and
// the extension asks the user to install MicroG RE on every launch until it is installed. Pulls
// in "Spoof signature", because Firebase checks the app's certificate too.
// Phone number verification is attested server-side with Play Integrity; neither patch changes
// that, and nothing here targets Play Integrity.
@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportPatch(
    fromPackageName = "com.truecaller",
    // SHA-1 of the original signing certificate (CN=truecaller), read from the APKMirror 26.39.6
    // bundle's signing block.
    spoofedPackageSignature = "0ac1169ae6cead75264c725febd8e8d941f25e31",
    mainActivityOnCreateFingerprint = TruecallerInitOnCreateFingerprint,
    compatibility = COMPATIBILITY_TRUECALLER,
    default = false,
    dependencies = setOf(spoofSignaturePatch),
)
