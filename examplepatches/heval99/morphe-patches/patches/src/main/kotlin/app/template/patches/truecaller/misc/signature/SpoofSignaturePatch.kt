package app.template.patches.truecaller.misc.signature

import app.template.patches.shared.Constants.COMPATIBILITY_TRUECALLER
import app.template.patches.shared.misc.signature.spoofSignaturePatch

// The original signing certificate (CN=truecaller, SHA-1
// 0ac1169ae6cead75264c725febd8e8d941f25e31), DER as base64, read from the signing block of the
// APKMirror 26.39.6 bundle. Firebase (Installations, so FCM) and Google sign-in send or check
// this certificate's SHA-1; re-signed, push and Google login fail. It does not affect the
// server-side Play Integrity attestation used for phone verification.
internal const val TRUECALLER_CERTIFICATE =
    "MIIBoTCCAQqgAwIBAgIESqliPDANBgkqhkiG9w0BAQUFADAVMRMwEQYDVQQDEwp0cnVlY2FsbGVyMB4XDTA5MDkxMDIwMzE1NloXDTM0MDkwNDIwMzE1NlowFTETMBEGA1UEAxMKdHJ1ZWNhbGxlcjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAlRaLpna+8av6sUvduPmMe4ipy9ZQX6pVSQZNoPicKTi5r7bZeyJtzfQjXPytRlQsODeY7rjnn367/QqUW8xNrz7+8CjdQDEbCxEOm9QbWsmC94ZvJ/qdnPZ0QXNznj3r6WyqyxAw91KngSiKWvkJ4InoPzvQmtuRo9JIu4b7W3UCAwEAATANBgkqhkiG9w0BAQUFAAOBgQBMcKAGSOT0tk4gvX4dD5zraItg3+vUEH6OwAtgPp/LfU5a7mLMefZfayIzR3YE2BMivNbLTR1NLAyKfW8NRSfusK4XChYKLn9GnFD61ZhqoeGJLBPj3aWM9Az9mZkr2IfJb38XGgOysgAzenHSZjp7mowoK5Q0c7sVuxMZNKgMKw=="

@Suppress("unused")
val spoofSignaturePatch = spoofSignaturePatch(
    fromPackageName = "com.truecaller",
    certificateBase64 = TRUECALLER_CERTIFICATE,
    compatibility = COMPATIBILITY_TRUECALLER,
)
