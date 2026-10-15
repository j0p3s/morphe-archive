/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * Original code hard forked from:
 * https://github.com/ReVanced/revanced-patches/blob/724e6d61b2ecd868c1a9a37d465a688e83a74799/patches/src/main/kotlin/app/revanced/patches/all/misc/packagename/ChangePackageNamePatch.kt
 *
 * File-Specific License Notice (GPLv3 Section 7 Terms)
 *
 * This file is part of the Morphe project and is licensed under
 * the GNU General Public License version 3 (GPLv3), with the Additional
 * Terms under Section 7 described in the LICENSE file.
 *
 * https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Section 7b: Notice Preservation
 * -------------------------------
 * This entire comment block must be preserved in all copies,
 * distributions, and derivative works of this file, in both
 * original and modified source forms.
 *
 * Portions of this software are provided "AS IS" by the Morphe software project.
 * Any express or implied warranties, including the implied warranties of
 * merchantability and fitness for a particular purpose, are disclaimed.
 */

package app.morphe.patches.minecraft.clone

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import org.w3c.dom.NodeList


private fun NodeList.elements() = (0 until length).mapNotNull { item(it) as? Element }

@Suppress("unused")
val cloneAppPatch = resourcePatch(
    name = "Change Package Name",
    description = "Changes the app package name to install patch without uninstalling original app. " +
            "By default, .morphe is appended.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    val packageNameOption = stringOption(
        key = "packageName",
        default = "Default",
        values = mapOf("Default" to "Default"),
        title = "Package name",
        description = "Package name to use for the cloned app.",
        required = true,
    ) {
        it == "Default" || it!!.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$"))
    }

    val updatePermissionsOption = booleanOption(
        key = "updatePermissions",
        default = true,
        title = "Update permissions",
        description = "Update custom permissions declared by the app. " +
            "Fixes installation conflicts.",
    )

    val updateProvidersOption = booleanOption(
        key = "updateProviders",
        default = true,
        title = "Update providers",
        description = "Update provider names declared by the app. " +
            "Fixes installation conflicts.",
    )

    finalize {
        val packageName = packageMetadata.packageName
        val newPackageName = packageNameOption.value
            .takeIf { it != packageNameOption.default }
            ?: "$packageName.morphe"

        val applyUpdatePermissions = updatePermissionsOption.value!!
        val applyUpdateProviders = updateProvidersOption.value!!

        val providerStringResources = mutableSetOf<String>()

        document("AndroidManifest.xml").use { document ->
            document.documentElement.setAttribute("package", newPackageName)

            if (applyUpdatePermissions) {
                val usesPermissions = document.getElementsByTagName("uses-permission").elements()

                document.getElementsByTagName("permission").elements().forEach {
                    val oldName = it.getAttribute("android:name")
                    val newName = when {
                        oldName.startsWith('.') -> return@forEach
                        oldName.startsWith("$packageName.") -> oldName.replaceFirst(packageName, newPackageName)
                        else -> "${newPackageName}_$oldName"
                    }
                    it.setAttribute("android:name", newName)

                    usesPermissions
                        .firstOrNull { usesPermission -> usesPermission.getAttribute("android:name") == oldName }
                        ?.setAttribute("android:name", newName)
                }
            }

            if (applyUpdateProviders) {
                document.getElementsByTagName("provider").elements().forEach { provider ->
                    val authorities = provider.getAttribute("android:authorities").split(';')
                    val newAuthorities = authorities.map {
                        when {
                            it.startsWith("$packageName.") -> it.replaceFirst(packageName, newPackageName)
                            it.startsWith('@') -> {
                                providerStringResources.add(it.removePrefix("@string/"))
                                it
                            }
                            else -> "${newPackageName}_$it"
                        }
                    }
                    provider.setAttribute("android:authorities", newAuthorities.joinToString(";"))
                }
            }
        }

        if (providerStringResources.isNotEmpty()) {
            document("res/values/strings.xml").use { document ->
                document.documentElement.childNodes.elements()
                    .filter { it.getAttribute("name") in providerStringResources }
                    .forEach { node ->
                        val authority = node.textContent
                        node.textContent = if (authority.startsWith("$packageName.")) {
                            authority.replaceFirst(packageName, newPackageName)
                        } else {
                            "${newPackageName}_$authority"
                        }
                    }
            }
        }
    }
}
