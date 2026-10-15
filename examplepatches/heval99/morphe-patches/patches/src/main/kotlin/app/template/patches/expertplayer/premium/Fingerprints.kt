package app.template.patches.expertplayer.premium

import app.morphe.patcher.Fingerprint

// RevenueCat's EntitlementInfo (R8-renamed to G5.M in 11.2.0). Its constructor takes isActive as
// the second argument and stores it in a field. The purchases_flutter bridge serialises that field
// for Dart (isActive key in the entitlement map), and EntitlementInfos builds the active map from it.
// The class and field names rotate with R8, so the constructor is matched by its full parameter
// list (RevenueCat's model shape), which the smoke test re-checks.
object EntitlementInfoConstructorFingerprint : Fingerprint(
    definingClass = "LG5/M;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;", "Z", "Z", "LG5/q0;",
        "Ljava/util/Date;", "Ljava/util/Date;", "Ljava/util/Date;", "LG5/Y1;",
        "Ljava/lang/String;", "Ljava/lang/String;", "Z", "Ljava/util/Date;",
        "Ljava/util/Date;", "LG5/m0;", "Lorg/json/JSONObject;", "LG5/F2;",
    ),
)
