package app.template.patches.shazam.misc.spotify

import app.morphe.patcher.Fingerprint

// Spotify Auth SDK 1.2.3 is bundled un-renamed (public library API). startAuthActivity()
// sends the request to the installed Spotify app (com.spotify.sso.action.START_AUTH_FLOW);
// the intent extras it fills in are inlined as const-strings in this method.
object SpotifyNativeStartAuthActivityFingerprint : Fingerprint(
    definingClass = "Lcom/spotify/sdk/android/auth/app/SpotifyNativeAuthUtil;",
    name = "startAuthActivity",
    returnType = "Z",
    parameters = listOf(),
    strings = listOf("CLIENT_ID", "REDIRECT_URI"),
)
