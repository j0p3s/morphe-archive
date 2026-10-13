package app.czyeru.patches.tiktok.misc.sharesheet

import app.morphe.patcher.Fingerprint

// Real dex class is X.0puG in TikTok 47.2.41 (builder: X.0puH).
internal object SharePanelSnapshotConstructorFingerprint : Fingerprint(
    definingClass = "LX/0puG;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("LX/0puH;"),
)
