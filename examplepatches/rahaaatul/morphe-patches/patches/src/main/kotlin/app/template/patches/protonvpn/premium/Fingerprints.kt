package app.template.patches.protonvpn.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import app.template.patches.shared.Constants.PROTONVPN_COMPATIBILITY

val RepositoryFilterServersFingerprint = Fingerprint(
    definingClass = "Lfj/l;",
    name = "r",
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = emptyList(),
)

val RepositoryVpnCountriesFingerprint = Fingerprint(
    definingClass = "Lfj/l\$b;",
    name = "i",
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

val ALL_FINGERPRINTS = listOf(
    RepositoryFilterServersFingerprint,
    RepositoryVpnCountriesFingerprint,
)