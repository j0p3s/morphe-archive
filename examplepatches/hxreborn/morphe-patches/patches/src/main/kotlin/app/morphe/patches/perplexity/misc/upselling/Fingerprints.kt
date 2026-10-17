/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.perplexity.misc.upselling

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal object SignInFunnelUserDataToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf("UserData(isNewUser=", ", forcePaywallAfterLogin="),
)

internal fun skipPaywallAfterSignInFingerprint(userDataType: String) = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(userDataType, "Z"),
)

internal object HomeUpsellFetchFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    strings = listOf("Cannot map announcement upsell ", "`setup_checklist` is missing for upsell "),
)

internal object UpgradePlanBannerToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf("UpgradePlanBanner(logoFallback="),
)

internal object HomeStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    strings = listOf("HomeState(userAvatarUrl="),
)

internal object UpgradePlanBannerFlagFingerprint : Fingerprint(
    classFingerprint = HomeStateToStringFingerprint,
    name = "<init>",
    filters = listOf(
        methodCall(definingClass = "Ljava/util/Set;", name = "contains"),
        fieldAccess(opcode = Opcode.IPUT_BOOLEAN, definingClass = "this", location = MatchAfterWithin(8)),
    ),
)

internal object ThreadUpsellMapperFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("RemoteUpsellInformation `title` is missing"),
)

internal object UpsellTypeEnumFingerprint : Fingerprint(
    name = "<clinit>",
    strings = listOf("UPGRADE_TO_PRO", "OPEN_SUBSCRIPTION_PROMO", "RUN_QUERY_IN_COMPUTER", "SHARE_LOCATION"),
)
