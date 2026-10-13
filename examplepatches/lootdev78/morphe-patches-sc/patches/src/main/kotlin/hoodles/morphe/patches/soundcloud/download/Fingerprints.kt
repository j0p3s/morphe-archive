/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.download

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.instanceOf
import app.morphe.patcher.methodCall

object OfflineSettingsFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/settings/main/SettingsScreenKt;",
    returnType = "V",
    filters = listOf(
        instanceOf(type = "Lcom/soundcloud/android/settings/main/OfflineSyncSettingState\$Visible;")
    )
)

object TrackRememberFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/trackpage/TrackDetailsPagePresenter\$fetchData\$2;",
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        fieldAccess(
            definingClass = "this",
            type = "Lcom/soundcloud/android/foundation/domain/TrackUrn;"
        ),
        methodCall(
            smali = "Lcom/soundcloud/android/foundation/domain/tracks/TrackItem;->getPermalinkUrl()Ljava/lang/String;"
        )
    )
)

object PlaylistRememberFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/features/bottomsheet/playlist/PlaylistBottomSheetViewModel\$playlistLoader\$1\$3;",
    filters = listOf(
        methodCall(
            name = "getPlaylistUrn",
            definingClass = "Lcom/soundcloud/android/foundation/domain/playlists/PlaylistItem;"
        ),
        methodCall(
            smali = "Lcom/soundcloud/android/foundation/domain/playlists/PlaylistItem;->getPermalinkUrl()Ljava/lang/String;"
        )
    )
)

object TrackClickFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/trackpage/TrackDetailsPagePresenter\$subscribeForDownloadClick\$1;",
    returnType = "V",
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/soundcloud/android/trackpage/TrackPageView\$DownloadClick;",
            type = "Lcom/soundcloud/android/foundation/domain/TrackUrn;"
        ),
        methodCall(
            smali = "Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;"
        )
    )
)

object TrackStateFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/trackpage/TrackDetailsPagePresenter\$toPageResultObservable\$1\$7\$1;",
    filters = listOf(
        methodCall(
            smali = "Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$ViewState;-><init>(Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$State;)V"
        )
    )
)

object PlaylistStateFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/playlist/view/renderers/PlaylistEngagementsPlayableRenderer;",
    returnType = "V",
    filters = listOf(
        methodCall(
            smali = "Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$ViewState;-><init>(Lcom/soundcloud/android/ui/components/buttons/DownloadActionButton\$State;)V"
        )
    )
)

object TrackMenuSwitchFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/features/bottomsheet/track/TrackMenuItem\$RemoveFromDownload;",
    name = "<init>",
    returnType = "V",
    filters = listOf(
        fieldAccess(
            smali = "Lcom/soundcloud/android/ui/components/R\$drawable;->ic_actions_downloaded:I"
        )
    )
)

object PlaylistMenuSwitchFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/features/bottomsheet/playlist/PlaylistMenuItem\$Downloaded;",
    name = "<init>",
    returnType = "V",
    filters = listOf(
        fieldAccess(
            smali = "Lcom/soundcloud/android/ui/components/R\$drawable;->ic_actions_downloaded:I"
        )
    )
)

object LibraryDownloadsFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/features/library/LibraryLinksViewHolder;",
    name = "bindItem",
    returnType = "V",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        fieldAccess(
            definingClass = "this",
            type = "Lcom/soundcloud/android/ui/components/actionlists/ActionListItem;"
        ),
        fieldAccess(
            definingClass = "this",
            type = "Lcom/soundcloud/android/ui/components/actionlists/ActionListItem;"
        )
    )
)

object PlaylistBottomSheetClicksFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            name = "getPlaylistUrn",
            definingClass = "Lcom/soundcloud/android/foundation/actions/models/PlaylistMenuParams;"
        ),
        methodCall(
            smali = "Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;"
        ),
        methodCall(
            name = "getPlaylistUrn",
            definingClass = "Lcom/soundcloud/android/foundation/actions/models/PlaylistMenuParams;"
        ),
        methodCall(
            smali = "Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;"
        )
    )
)

object TrackBottomSheetClicksFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/soundcloud/android/features/bottomsheet/track/TrackBottomSheetViewModel;",
            type = "Ljava/lang/String;"
        ),
        methodCall(
            smali = "Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;"
        ),
        fieldAccess(
            definingClass = "Lcom/soundcloud/android/features/bottomsheet/track/TrackBottomSheetViewModel;",
            type = "Ljava/lang/String;"
        ),
        methodCall(
            smali = "Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;"
        )
    )
)

object PlaylistDetailsClickFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    filters = listOf(
        methodCall(
            smali = "Lcom/soundcloud/android/foundation/domain/playlists/PlaylistItem;->getPermalinkUrl()Ljava/lang/String;"
        ),
        methodCall(smali = "Landroid/view/View;->getContext()Landroid/content/Context;"),
        fieldAccess(
            definingClass = "Lcom/soundcloud/android/playlists/PlaylistDetailsMetadata;",
            type = "Lcom/soundcloud/android/playlists/PlaylistDetailsMetadata\$OfflineOptions;"
        )
    )
)
