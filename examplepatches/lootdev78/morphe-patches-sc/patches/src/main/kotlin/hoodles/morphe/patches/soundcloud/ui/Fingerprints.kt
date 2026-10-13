/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.ui

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

object TitleBarUploadFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/creators/uploadv2/pub/titlebar/TitleBarUploadController;",
    returnType = "V",
    filters = listOf(
        methodCall(smali = "Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;")
    )
)

object TitleBarInboxFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/messages/inbox/titlebar/TitleBarInboxController;",
    returnType = "V",
    filters = listOf(
        methodCall(smali = "Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;")
    )
)

object TitleBarActivityFeedFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/activity/feed/titlebar/TitleBarActivityFeedController;",
    returnType = "V",
    filters = listOf(
        methodCall(smali = "Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;")
    )
)

object CastMenuItemFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/cast/ui/DefaultCastButtonInstaller;",
    filters = listOf(
        methodCall(smali = "Landroid/view/MenuItem;->setVisible(Z)Landroid/view/MenuItem;")
    )
)

object CastButtonFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/cast/ui/DefaultCastButtonInstaller;",
    returnType = "V",
    filters = listOf(
        methodCall(
            name = "setDialogFactory",
            definingClass = "Landroidx/mediarouter/app/MediaRouteButton;"
        )
    )
)
