package app.template.patches.transfermarkt.misc.telemetry

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// cordova-plugin-idfa's IdfaPlugin.getInfo(CallbackContext): reads the Google advertising ID and
// hands {aaid, trackingLimited} to the web app, which passes it to its ad and analytics tags.
// The AdvertisingIdClient call and the two JSON keys pin it.
internal object IdfaGetInfoFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Lorg/apache/cordova/CallbackContext;"),
    strings = listOf("aaid", "trackingLimited"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/gms/ads/identifier/AdvertisingIdClient;",
            name = "getAdvertisingIdInfo",
        ),
    ),
)
