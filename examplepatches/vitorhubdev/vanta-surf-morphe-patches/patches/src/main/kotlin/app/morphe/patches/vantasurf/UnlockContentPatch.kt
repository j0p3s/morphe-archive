package app.morphe.patches.vantasurf

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch

/**
 * Unlocks the episodes that are gated behind an ad.
 *
 * The app has two different ad gates:
 *
 * * 5.0.2 and newer show a "watch an ad to keep watching" popup. The popup flow
 *   takes over the playback and the player refuses to resume while it is pending.
 * * 5.0.0 and 5.0.1 show an "ad gate" dialog with the scenarios "watch an ad to
 *   start this video" and "unlock this episode". The dialog is built from an ad
 *   item, and the player lets the video play when that ad item is missing.
 *
 * Both gates are disabled, so the episodes play straight away.
 */
@Suppress("unused")
val unlockContentPatch = bytecodePatch(
    name = "Unlock episodes",
    description = "Plays episodes without waiting for the watch an ad gate.",
    default = true,
) {
    compatibleWith(VantaSurfCompatibility)

    execute {
        var patched = 0

        // 5.0.2 and newer: the free ad popup no longer takes over the playback.
        FreeAdPopupFingerprint.matchOrNull()?.let { match ->
            match.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
            patched++
        }

        // 5.0.2 and newer: playback is never blocked by a pending popup.
        ResumeBlockedFingerprint.matchOrNull()?.let { match ->
            match.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
            patched++
        }

        // 5.0.0 and 5.0.1: without an ad item the gate completes itself.
        AdGateItemFingerprint.matchOrNull()?.let { match ->
            match.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return-object v0
                """,
            )
            patched++
        }

        if (patched == 0) {
            throw PatchException("Could not find the ad gate of Vanta Surf")
        }
    }
}
