package app.template.patches.fotmob.misc.signature

import app.template.patches.shared.Constants.COMPATIBILITY_FOTMOB
import app.template.patches.shared.misc.signature.spoofSignaturePatch

// The original signing certificate (CN=Christer Nordvik, OU=Score Service, O=Score Service, SHA-1
// 5546e6c4b0c1f31b9e5a953a89a8bb6f25a2ad6f), DER as base64, read from the signing block of the
// APKMirror 238.17794.20261012 bundle's base APK (the merged APK's block no longer verifies).
// Firebase (Installations, so FCM, plus Auth, which Google sign-in ends in) sends this
// certificate's SHA-1 as X-Android-Cert (issue #64). The Facebook SDK in this build only hashes the
// Facebook apps' signatures, not FotMob's, and the Facebook app reads the caller's real
// certificate itself, so this patch does not change Facebook login.
internal const val FOTMOB_CERTIFICATE =
    "MIICYDCCAckCBEjwvccwDQYJKoZIhvcNAQEEBQAwdzELMAkGA1UEBhMCTk8xDDAKBgNVBAgTA04vQTEPMA0GA1UEBxMGQmVyZ2VuMRYwFAYDVQQKEw1TY29yZSBTZXJ2aWNlMRYwFAYDVQQLEw1TY29yZSBTZXJ2aWNlMRkwFwYDVQQDExBDaHJpc3RlciBOb3JkdmlrMB4XDTA4MTAxMTE0NTI1NVoXDTM2MDIyNzE0NTI1NVowdzELMAkGA1UEBhMCTk8xDDAKBgNVBAgTA04vQTEPMA0GA1UEBxMGQmVyZ2VuMRYwFAYDVQQKEw1TY29yZSBTZXJ2aWNlMRYwFAYDVQQLEw1TY29yZSBTZXJ2aWNlMRkwFwYDVQQDExBDaHJpc3RlciBOb3JkdmlrMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC7d1X4zVO0nlmMfjo7YSQ9zLLe2jHjffwlYi4MOZvlKSUy9vxrJI/WbLUcWdev9qEv+tAQcFTaGqqaszSXRHtkMM5X28JdWF4wgdMa4VS1KGQAPamKd8xdVarTfiDQZD89HxX0mmDpZxpBkaiv4aoNQ78m0jotAfGfC8v7x8fK+QIDAQABMA0GCSqGSIb3DQEBBAUAA4GBABpq9Ur3ZXa7ov8t5H3vB0+7f3ghVc2M8XU7pGXLQau5UiZ9uMMrZ+AfINRNepckCMJAfECWjxNwAiqJbROWN88GVDM+Zkf2g6CTeu7HpJQ/KQYY1y9uvUdZyH9lgVh6VEd6nDiA4IxzLYGTI1p1yM8oBSAZ424FxnK6mfmgnhZI"

@Suppress("unused")
val spoofSignaturePatch = spoofSignaturePatch(
    fromPackageName = "com.mobilefootie.wc2010",
    certificateBase64 = FOTMOB_CERTIFICATE,
    compatibility = COMPATIBILITY_FOTMOB,
)
