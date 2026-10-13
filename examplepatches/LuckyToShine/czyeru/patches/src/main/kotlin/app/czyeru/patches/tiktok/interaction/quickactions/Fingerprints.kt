/*
 * Copyright 2026 icysymmetra/tiktok-patches-for-morphe contributors
 * https://github.com/icysymmetra/tiktok-patches-for-morphe
 */
package app.czyeru.patches.tiktok.interaction.quickactions

import app.morphe.patcher.Fingerprint

/**
 * Decides whether the exposed quick emoji row is shown for a comment input
 * (`BaseSlotComponentTrigger.Jp(CommentContextSource, state)`, TikTok 47.2.41).
 */
internal object QuickCommentReactionGateFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/comment/keyboard/keyboardv2/refactor/ExposedEmojiPanelTrigger;",
    name = "Jp",
    returnType = "Z",
    parameters = listOf("Lcom/ss/android/ugc/aweme/comment/model/CommentContextSource;", "L"),
)

internal object LongPressQuickShareGateFingerprint : Fingerprint(
    returnType = "I",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "LX/09fy;" &&
            method.name == "LIZ"
    },
)

internal object LongPressRepostGateFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/assem/digg/VideoDiggAssem;",
    returnType = "Z",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf(
        "Long press detected on digg button for aweme: ",
        "long_press_like_panel",
    ),
)
