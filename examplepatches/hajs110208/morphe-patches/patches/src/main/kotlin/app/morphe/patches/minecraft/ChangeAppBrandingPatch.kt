package app.morphe.patches.minecraft.branding

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import org.w3c.dom.NodeList

private const val APP_NAME_RESOURCE = "morphe_app_name"

private fun NodeList.elements() = (0 until length).mapNotNull { item(it) as? Element }

private fun Element.childElements(tagName: String) = getElementsByTagName(tagName).elements()

private fun Element.isLauncher() = childElements("intent-filter").any { filter ->
    filter.childElements("action").any { it.getAttribute("android:name") == "android.intent.action.MAIN" } &&
        filter.childElements("category").any { it.getAttribute("android:name") == "android.intent.category.LAUNCHER" }
}

private fun String.escapeAndroidString() = replace("\\", "\\\\")
    .replace("'", "\\'")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .let { if (it.startsWith("@") || it.startsWith("?")) "\\$it" else it }

@Suppress("unused")
val changeAppBrandingPatch = resourcePatch(
    name = "Change app branding",
    description = "Changes the app name shown in the launcher.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    val appNameOption = stringOption(
        key = "appName",
        default = "Minecraft Patch",
        values = mapOf("Minecraft Patch" to "Minecraft Patch"),
        title = "App name",
        description = "The name of the app shown in the launcher.",
        required = true,
    ) {
        !it.isNullOrBlank()
    }

    execute {
        val appName = appNameOption.value!!.trim()

        document("res/values/strings.xml").use { document ->
            val resources = document.documentElement

            resources.childNodes.elements()
                .filter { it.tagName == "string" && it.getAttribute("name") == APP_NAME_RESOURCE }
                .forEach { resources.removeChild(it) }

            val string = document.createElement("string").apply {
                setAttribute("name", APP_NAME_RESOURCE)
                textContent = appName.escapeAndroidString()
            }
            resources.appendChild(string)
        }

        document("AndroidManifest.xml").use { document ->
            val label = "@string/$APP_NAME_RESOURCE"

            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("<application> not found in AndroidManifest.xml")
            application.setAttribute("android:label", label)

            (application.childElements("activity") + application.childElements("activity-alias"))
                .filter { it.isLauncher() }
                .forEach { it.setAttribute("android:label", label) }
        }
    }
}
