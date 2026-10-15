package app.template.patches.scores365.misc.gms

import app.morphe.patcher.Fingerprint

// The launch activity: every launcher alias (AppIconAlias*) targets it. Its class is declared in
// the manifest, so R8 keeps the name; onCreate(Bundle) is a framework override.
internal object SplashOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/scores365/ui/Splash;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
