package app.czyeru.patches.tiktok.misc.commentsort

import app.morphe.patcher.Fingerprint

internal object CommentSortOptionStyleFingerprint : Fingerprint(
    definingClass = "Lkotlin/jvm/internal/AFwS250S0000000_20;",
    name = "invoke\$182",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lkotlin/jvm/internal/AFwS250S0000000_20;"),
    strings = listOf("comment_sort_opt_style"),
)

internal object CommentSortEligibilityFingerprint : Fingerprint(
    definingClass = "LX/0ls3;",
    name = "LIZ",
    returnType = "Z",
    parameters = listOf("Lcom/ss/android/ugc/aweme/feed/model/Aweme;"),
)
