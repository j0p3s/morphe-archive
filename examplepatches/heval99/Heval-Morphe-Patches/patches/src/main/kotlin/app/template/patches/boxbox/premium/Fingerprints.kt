package app.template.patches.boxbox.premium

import app.morphe.patcher.Fingerprint

object EntitlementInfoIsActiveFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == "Lcom/revenuecat/purchases/EntitlementInfo;" &&
                method.name == "isActive" &&
                method.returnType == "Z" &&
                method.parameters.isEmpty()
    }
)

// The plan enum (Free, Pro). Constant names are the Enum.name() strings, so they survive R8.
object PlanEnumClinitFingerprint : Fingerprint(
    name = "<clinit>",
    strings = listOf("Free", "Pro"),
    custom = { _, classDef -> classDef.superclass == "Ljava/lang/Enum;" },
)

// PlanDataStore: the DataStore file name and its preference key. Renaming either would lose
// every user's stored plan, so both are stable.
object PlanDataStoreClinitFingerprint : Fingerprint(
    name = "<clinit>",
    strings = listOf("plans_data_store", "plan_type"),
)
