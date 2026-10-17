package app.template.patches.myfitnesspal.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

internal const val ENTITLEMENTS = "Lcom/myfitnesspal/queryenvoy/domain/model/subscriptions/entitlements/"

/**
 * The static `(String) -> Enum` parser on [enumType]'s companion (a nested class of the enum),
 * found by shape plus its `Unknown` fallback rather than by the R8 names of the companion
 * (`$a`) and the method (`a`).
 */
private fun companionParser(enumType: String) = Fingerprint(
    returnType = enumType,
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(fieldAccess(definingClass = enumType, name = "Unknown", opcode = Opcode.SGET_OBJECT)),
    custom = { method, classDef ->
        AccessFlags.STATIC.isSet(method.accessFlags) &&
            classDef.type.startsWith(enumType.removeSuffix(";") + "\$")
    },
)

/**
 * 26.37.0 removed the old local `SubscriptionPreferences.getPremiumPlusEnabled` gate: premium
 * state now comes from the server as queryenvoy `FeatureState` objects (feature, entitlement
 * and tier), deserialized through the enum companions. The two parsers below are the single
 * conversion point for the server strings, so overriding them marks every feature as entitled
 * at the highest tier.
 */
internal val EntitlementParserFingerprint = companionParser("${ENTITLEMENTS}Entitlement;")

internal val TierParserFingerprint = companionParser("${ENTITLEMENTS}Tier;")
