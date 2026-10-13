package app.template.patches.meshy.unlockpreview

import app.morphe.patcher.Fingerprint

/**
 * Meshy's premium gate lives entirely in the Hermes JS layer: the JS decides
 * pro/free from the customer info the native RevenueCat SDK serialises. The
 * three Meshy native modules that the patch ultimately unlocks —
 *
 *  * `meshyar` (`ExpoMeshyAR` / `MeshyARSceneView`): `prepareLocalGlb`,
 *    `setModelUri` → AR preview;
 *  * `meshydecrypt` (`ExpoMeshyDecrypt` / `MeshyDecryptBridge`):
 *    `processMeshyToFile` → decrypt a `.meshy` model to `.glb` for export;
 *  * `meshysharetarget` (`ExpoMeshyShareTarget`): `shareFileToPackage` /
 *    `openFileWithChooser` → hand the model to another app;
 *
 * …do **not** gate anything internally (verified: no subscription/entitlement
 * logic in any of them), so there is nothing to hook there. The single choke
 * point is the method below.
 *
 * `EntitlementInfosMapperKt.map(EntitlementInfos)` builds the
 * `{"all":…, "active":…, "verification":…}` object that becomes
 * `customerInfo.entitlements` for JS. For a never-subscribed account `active`
 * is empty, so this is where an active `pro` entry is seeded (see
 * `MeshyHelper.meshyInjectProEntitlement`).
 *
 * The class is unobfuscated in the target APK (RevenueCat ships consumer
 * rules keeping its API), confirmed present in `classes4.dex`.
 */
internal val EntitlementInfosMapperMapFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/mappers/EntitlementInfosMapperKt;",
    name = "map",
    returnType = "Ljava/util/Map;",
    parameters = listOf("Lcom/revenuecat/purchases/EntitlementInfos;"),
)
