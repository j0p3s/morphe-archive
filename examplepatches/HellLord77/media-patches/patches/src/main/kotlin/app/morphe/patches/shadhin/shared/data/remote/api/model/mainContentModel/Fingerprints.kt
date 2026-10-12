package app.morphe.patches.shadhin.shared.data.remote.api.model.mainContentModel

import app.morphe.patcher.Fingerprint
import app.morphe.patches.shared.Type
import com.android.tools.smali.dexlib2.AccessFlags

object IsPaidFingerprint : Fingerprint(
    definingClass = "Lcom/gm/shadhin/data/remote/api/model/MainContentModel;",
    name = "isPaid",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = Type.boolean,
    parameters = emptyList(),
)