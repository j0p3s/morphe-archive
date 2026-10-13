package app.czyeru.patches.irisgallery

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Click handler behind the selection menu's "Move to Locked" action
 * (Iris Gallery 0.8.0, obfuscated as `tj1`).
 *
 * `tj1` is a synthetic Function1 shared by several handlers and switches on its field `i`.
 * `i == 2` is the vault handler: it receives the selected media (a List of `np1`, the
 * "MediaImage" model) and launches the coroutine `if0`, which moves them into the hidden vault.
 */
object MoveToLockedHandlerFingerprint : Fingerprint(
    definingClass = "Ltj1;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        // The vault coroutine, created in the i == 2 branch.
        methodCall(
            definingClass = "Lif0;",
            name = "<init>"
        )
    )
)
