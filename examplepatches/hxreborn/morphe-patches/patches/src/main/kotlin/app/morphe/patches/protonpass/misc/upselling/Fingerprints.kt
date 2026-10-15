/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.protonpass.misc.upselling

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterWithin
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val UPGRADE_AVAILABLE_FIELD = "isUpgradeAvailable"
private const val HOME_NAVIGATION_CLASS = "Lproton/android/pass/features/home/HomeNavigation"
private const val ONBOARDING_ROUTE = "$HOME_NAVIGATION_CLASS\$OnBoarding;"
private const val UPSELL_ONBOARDING_ROUTE = "$HOME_NAVIGATION_CLASS\$UpsellV2AndOnboarding;"
internal const val DARK_WEB_MONITORING_CLASS =
    "Lproton/android/pass/features/security/center/home/presentation/SecurityCenterHomeDarkWebMonitoring"

internal object UpgradeInfoConstructorFingerprint : Fingerprint(
    definingClass = "Lproton/android/pass/data/api/usecases/UpgradeInfo;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    filters = listOf(
        fieldAccess(name = UPGRADE_AVAILABLE_FIELD, type = "Z", opcode = Opcode.IPUT_BOOLEAN),
    ),
)

internal object PlanLimitReachedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("L", "I"),
    filters = listOf(
        fieldAccess(name = UPGRADE_AVAILABLE_FIELD, type = "Z", opcode = Opcode.IGET_BOOLEAN),
    ),
)

internal object OnboardingRouteFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(type = UPSELL_ONBOARDING_ROUTE, opcode = Opcode.SGET_OBJECT),
        fieldAccess(type = ONBOARDING_ROUTE, opcode = Opcode.SGET_OBJECT, location = MatchAfterWithin(3)),
    ),
)

internal object UpgradeButtonFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "V",
    custom = { method, _ -> method.name.substringBefore('-') == "UpgradeButton" },
)

internal object AttachmentsStateConstructorFingerprint : Fingerprint(
    definingClass = "Lproton/android/pass/commonuimodels/api/attachments/AttachmentsState;",
    name = "<init>",
    parameters = listOf(
        "Ljava/util/List;",
        "Ljava/util/List;",
        "Ljava/util/Set;",
        "Lproton/android/pass/common/api/Option;",
        "Z",
    ),
)

internal object DarkWebMonitoringSectionFingerprint : Fingerprint(
    name = "SecurityCenterHomeDarkWebMonitoringSection",
    returnType = "V",
    parameters = listOf(
        "Landroidx/compose/ui/Modifier;",
        "Lkotlin/jvm/functions/Function1;",
        "$DARK_WEB_MONITORING_CLASS;",
        "Landroidx/compose/runtime/Composer;",
        "I",
    ),
)

internal object SentinelUpsellRowFingerprint : Fingerprint(
    filters = listOf(
        fieldAccess(name = "isSentinelPaidFeature", type = "Z", opcode = Opcode.IGET_BOOLEAN),
        methodCall(definingClass = "Lproton/android/pass/composecomponents/impl/row/CounterRowKt;"),
    ),
)
