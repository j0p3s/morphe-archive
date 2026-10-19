/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.imo.misc.analytics

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

private const val APPSFLYER_CLASS = "Lcom/appsflyer/AppsFlyerLib;"

internal object AppsFlyerStartFingerprint : Fingerprint(
    definingClass = "Lcom/imo/android/",
    filters = listOf(methodCall(definingClass = APPSFLYER_CLASS, name = "start")),
)

internal object AppsFlyerEventFingerprint : Fingerprint(
    definingClass = "Lcom/imo/android/",
    filters = listOf(methodCall(definingClass = APPSFLYER_CLASS, name = "logEvent")),
)

internal object FirebaseCollectionSettingFingerprint : Fingerprint(
    definingClass = "Lcom/imo/android/imoim/setting/BootAlwaysSettingsDelegate;",
    name = "getAnalyticsCollectionenabled",
)

internal object MonitorEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/util/HashMap;", "L"),
    strings = listOf("monitor", "log_event"),
)

internal object StatEventFingerprint : Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("send_media_im", "bundle_stat", "msg_type"),
)

internal object AdvertisingIdCallbackFingerprint : Fingerprint(
    parameters = listOf("Lcom/google/android/gms/ads/identifier/AdvertisingIdClient\$Info;"),
)

internal object AdvertisingIdUploadFingerprint : Fingerprint(
    classFingerprint = AdvertisingIdCallbackFingerprint,
    returnType = "V",
    filters = listOf(methodCall(definingClass = "Landroid/os/AsyncTask;", name = "executeOnExecutor")),
)
