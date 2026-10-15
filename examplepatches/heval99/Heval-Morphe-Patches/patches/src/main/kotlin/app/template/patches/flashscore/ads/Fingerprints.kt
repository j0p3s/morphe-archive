package app.template.patches.flashscore.ads

import app.morphe.patcher.Fingerprint

internal const val ADVERT_ZONE = "Leu/livesport/core/ui/adverts/AdvertZone;"

// AdvertZone is the app's ad slot view (kept: it is inflated from layouts). Its single load entry
// point picks the provider for the slot (Google, the "revive" bookmaker WebView banners, ...) and
// is the only method that throws "Unsupported ad provider: ".
internal object AdvertZoneLoadFingerprint : Fingerprint(
    definingClass = ADVERT_ZONE,
    returnType = "V",
    parameters = listOf(),
    strings = listOf("Unsupported ad provider: "),
)

// The video player's media item (R8-renamed, com.zentity.ottplayer.b in 26.10.2): stream URLs,
// DRM data and the VMAP ad response. Found through its data-class toString().
internal object VideoMediaToStringFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf(", vmapResponse="),
)

// Its constructor (streams, drm, vmapResponse). The player only creates the IMA ads loader when
// vmapResponse is non-null, so a null VMAP means no pre-roll or mid-roll is ever requested.
internal object VideoMediaConstructorFingerprint : Fingerprint(
    classFingerprint = VideoMediaToStringFingerprint,
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/util/ArrayList;",
        "Lcom/zentity/ottplayer/MediaProvider\$DrmData;",
        "Ljava/lang/String;",
    ),
)
