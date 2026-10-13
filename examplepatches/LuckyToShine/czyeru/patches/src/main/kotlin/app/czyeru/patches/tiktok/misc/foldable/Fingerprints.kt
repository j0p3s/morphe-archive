package app.czyeru.patches.tiktok.misc.foldable

import app.morphe.patcher.Fingerprint

// Real dex class is X.0os0 in TikTok 47.2.41.
internal object CommentSplitLiveCheckFingerprint : Fingerprint(
    definingClass = "LX/0os0;",
    name = "LIZ",
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "Landroid/content/res/Configuration;"),
    strings = listOf("isOptCommentSplit"),
)

internal object CommentSplitContainerCheckFingerprint : Fingerprint(
    definingClass = "LX/0os0;",
    name = "LIZJ",
    returnType = "Z",
    parameters = listOf(),
    strings = listOf("isOptSplitContainer"),
)
