package app.template.patches.intervaltimer.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

// Interval Timer caches its ad-free state in two R8-renamed JSON models (AppInstallation and
// User). Both serialize the server's "config_ads" (adbuster) flag next to the rewarded-ad window
// ("local_ads_time", "local_ads_off"). The JSON annotation type is renamed too, so only the
// annotations' string values are compared.
private val AD_STATE_FIELDS = setOf("config_ads", "local_ads_time", "local_ads_off")

private fun ClassDef.jsonFieldNames(): Set<String> =
    fields.flatMap { field ->
        field.annotations.flatMap { annotation ->
            annotation.elements.mapNotNull { (it.value as? StringEncodedValue)?.value }
        }
    }.toSet()

// The ad-state getter: an instance ()I method that returns 3 (ad-free), 2 (rewarded window
// active), 1 (ads) and, in User, 0 (unknown). Every ad placement and the "Remove ads" toolbar
// item read it.
internal open class AdStateFingerprint(marker: String) : Fingerprint(
    returnType = "I",
    parameters = listOf(),
    filters = listOf(literal(3), literal(2), literal(1)),
    custom = { method, classDef ->
        !AccessFlags.STATIC.isSet(method.accessFlags) &&
            classDef.jsonFieldNames().containsAll(AD_STATE_FIELDS + marker)
    },
)

// AppInstallation (keyed by "device"): the per-install copy, synced from GET me/device/{sid}/config.
internal object InstallationAdStateFingerprint : AdStateFingerprint("device")

// User (keyed by "token"): the signed-in account's copy, synced from GET me/config. The app asks
// it first and falls back to the installation unless it answers 3 (or 2).
internal object UserAdStateFingerprint : AdStateFingerprint("token")
