package app.template.patches.sofascore.misc.gms

import app.morphe.patcher.Fingerprint

// The launch activity. Its class is declared in the manifest, so R8 keeps the name; onCreate(Bundle)
// is a framework override.
internal object StartActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/sofascore/results/main/start/StartActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
