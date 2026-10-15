package app.morphe.patches.minecraft.documentsprovider

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private const val PROVIDER_CLASS =
    "app.morphe.extension.minecraft.documentsprovider.AppDataDocumentsProvider"

private val documentsProviderManifestPatch = resourcePatch {
    execute {
        val packageName = packageMetadata.packageName

        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("<application> not found in AndroidManifest.xml")

            val action = document.createElement("action").apply {
                setAttribute("android:name", "android.content.action.DOCUMENTS_PROVIDER")
            }

            val intentFilter = document.createElement("intent-filter").apply {
                appendChild(action)
            }

            val provider = document.createElement("provider").apply {
                setAttribute("android:name", PROVIDER_CLASS)
                setAttribute("android:authorities", "$packageName.documents")
                setAttribute("android:exported", "true")
                setAttribute("android:grantUriPermissions", "true")
                setAttribute("android:permission", "android.permission.MANAGE_DOCUMENTS")
                appendChild(intentFilter)
            }

            application.appendChild(provider)
        }
    }
}

@Suppress("unused")
val documentsProviderPatch = bytecodePatch(
    name = "Add documents provider",
    description = "Adds a documents provider so the app's data folders can be accessed from the system file manager.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    dependsOn(documentsProviderManifestPatch)

    extendWith("extensions/minecraft/documentsprovider.mpe")
}
