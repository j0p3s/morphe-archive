/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.poe.misc.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.generated.replaceMethod
import app.morphe.patches.shared.misc.pairip.removePairipVirtualizationPatch
import app.morphe.util.inputStreamFromBundledResource
import app.morphe.util.matchSingle

private const val REGISTERS = 10

val restoreDownloadReceiverPatch = bytecodePatch {
    compatibleWith(AppCompatibilities.POE)

    dependsOn(removePairipVirtualizationPatch)

    execute {
        val smali = inputStreamFromBundledResource("poe", "downloadCompleteReceiver.onReceive.smali")
            ?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Missing poe/downloadCompleteReceiver.onReceive.smali")

        mutableClassDefBy(DownloadCompleteReceiverFingerprint.matchSingle().classDef)
            .replaceMethod("onReceive", REGISTERS, smali)
    }
}
