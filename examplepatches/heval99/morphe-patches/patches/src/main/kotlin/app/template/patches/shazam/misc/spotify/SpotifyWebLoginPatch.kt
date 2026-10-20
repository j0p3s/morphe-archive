package app.template.patches.shazam.misc.spotify

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly

/**
 * Part of Shazam's "Spoof signature". The bundled Spotify Auth SDK hands the login to the
 * installed Spotify app, which checks the calling app's real signing certificate in its own
 * process, so a re-signed Shazam always gets "You could not be connected to Spotify". Reporting
 * the Spotify app as unavailable makes AuthorizationClient fall through to its next handler,
 * WebViewAuthHandler: the accounts.spotify.com login page in a dialog, whose redirect is caught
 * in-process. No certificate is involved there.
 */
internal val spotifyWebLoginPatch = bytecodePatch {
    execute {
        SpotifyNativeStartAuthActivityFingerprint.method.returnEarly(false)
    }
}
