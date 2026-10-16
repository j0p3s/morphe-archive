package app.tower.patches.tower

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch

private const val EXTENSION_CLASS = "Lapp/template/extension/extension/TowerSeed;"

private object SeedResources

/** Internal: puts the Remote Config cache into the APK as assets/tower_frc.json. */
internal val towerSeedAssetPatch = resourcePatch {
    execute {
        val bytes = SeedResources::class.java.getResourceAsStream("/tower_frc.json")!!.readBytes()
        val file = get("assets/tower_frc.json", copy = false)
        file.parentFile?.mkdirs()
        file.writeBytes(bytes)
    }
}

internal object UnityActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/player/UnityPlayerActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

internal object FirebaseInitProviderOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/provider/FirebaseInitProvider;",
    name = "onCreate",
    returnType = "Z",
)

@Suppress("unused")
val towerSeedPatch = bytecodePatch(
    name = "Seed Remote Config cache",
    description = "Copies the bundled Firebase Remote Config cache into the app on first start. Without it " +
        "SaveLoad.Load fails offline (no shop/mini boss config), so the game never saves.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TOWER)

    dependsOn(towerSeedAssetPatch)
    extendWith("extensions/extension.mpe")

    execute {
        // Earliest hook: Firebase reads the Remote Config cache after its init provider ran, so on a
        // fresh install the seed has to exist before FirebaseInitProvider.onCreate does anything.
        FirebaseInitProviderOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {p0}, $EXTENSION_CLASS->seedProvider(Landroid/content/ContentProvider;)V"
        )
        // Fallback, idempotent.
        UnityActivityOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {p0}, $EXTENSION_CLASS->seed(Landroid/content/Context;)V"
        )
    }
}
