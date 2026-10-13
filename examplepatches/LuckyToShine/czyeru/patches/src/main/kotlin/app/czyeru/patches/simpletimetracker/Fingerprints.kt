package app.czyeru.patches.simpletimetracker

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Simple Time Tracker 1.60 keeps its class and method names, so these fingerprints match by name.
 */

/**
 * `IconImageRepo.getImages(arrayId)` builds the map of icon name to drawable id for one group
 * of the icon picker (`R.array.icon_*`).
 */
object GetImagesFingerprint : Fingerprint(
    definingClass = "Lcom/example/util/simpletimetracker/core/repo/IconImageRepo;",
    name = "getImages",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/Map;",
    parameters = listOf("I")
)

/**
 * `IconView.setTextIcon(text)` shows an icon that is a text (an emoji). Icon names that do not
 * start with `ic_` end up here, which includes the custom `xml:` names.
 */
object SetTextIconFingerprint : Fingerprint(
    definingClass = "Lcom/example/util/simpletimetracker/feature_views/IconView;",
    name = "setTextIcon",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;")
)

/**
 * Binds one cell of the icon picker: card color, then the drawable, then the click listener.
 * It is the `invoke` of the second lambda in `createIconSelectionAdapterDelegate`.
 */
object BindIconSelectionItemFingerprint : Fingerprint(
    definingClass = "Lcom/example/util/simpletimetracker/feature_icon_selection/adapter/" +
        "IconSelectionAdapterDelegateKt\$createIconSelectionAdapterDelegate\$2;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(
        "Lcom/example/util/simpletimetracker/feature_icon_selection/databinding/ItemIconSelectionLayoutBinding;",
        "Lcom/example/util/simpletimetracker/feature_base_adapter/ViewHolderType;",
        "Ljava/util/List;"
    )
)
