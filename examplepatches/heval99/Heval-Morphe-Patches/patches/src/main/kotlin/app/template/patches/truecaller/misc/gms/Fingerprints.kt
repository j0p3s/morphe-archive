package app.template.patches.truecaller.misc.gms

import app.morphe.patcher.Fingerprint

// The launch activity (TruecallerInitAlias points at it). Its class is declared in the manifest,
// so R8 keeps the name; onCreate(Bundle) is a framework override.
internal object TruecallerInitOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/truecaller/ui/TruecallerInit;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
