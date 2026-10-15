package app.template.patches.smartwod.premium

import app.morphe.patcher.Fingerprint

// react-native-iap's native module (RN registers it reflectively, so the class and @ReactMethod
// names are kept by R8). JS getAvailablePurchases() / SmartWOD's getPremiumInfoAndroid lands
// here once for "inapp" and once for "subs": it queries Play Billing's owned purchases and
// resolves them as an array of maps. The concrete method only, never an interface declaration.
internal object GetAvailableItemsByTypeFingerprint : Fingerprint(
    definingClass = "Lcom/dooboolab/rniap/RNIapModule;",
    name = "getAvailableItemsByType",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Lcom/facebook/react/bridge/Promise;"),
    custom = { method, _ -> method.implementation != null },
)
