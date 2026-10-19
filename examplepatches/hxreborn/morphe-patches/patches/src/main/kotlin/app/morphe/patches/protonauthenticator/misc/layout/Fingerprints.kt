/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.protonauthenticator.misc.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patches.shared.misc.proton.COMPOSER_TYPE
import com.android.tools.smali.dexlib2.Opcode

internal object DiscoverSectionVisibilityFingerprint : Fingerprint(
    name = "getShouldShowDiscoverSection\$master_release",
    returnType = "Z",
    parameters = emptyList(),
)

internal object PassBannerFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(fieldAccess(name = "settings_pass_banner_description", type = "I", opcode = Opcode.SGET)),
)

internal object SpreadTheWordRowsFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        fieldAccess(name = "settings_spread_the_word_section", type = "I", opcode = Opcode.SGET),
        methodCall(name = "listOf", opcode = Opcode.INVOKE_STATIC, location = MatchAfterWithin(30)),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
    ),
)

internal object SettingsSectionFingerprint : Fingerprint(
    name = "SettingsSection",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/List;", COMPOSER_TYPE, "I"),
)
