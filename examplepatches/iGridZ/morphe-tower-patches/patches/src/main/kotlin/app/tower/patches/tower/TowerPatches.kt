package app.tower.patches.tower

import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.security.MessageDigest

private fun hex(s: String): ByteArray {
    require(s.length % 2 == 0) { "odd hex length" }
    return ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

@Suppress("unused")
val towerOfflinePatch = resourcePatch(
    name = "Offline single-player",
    description = "Patches libil2cpp.so so The Tower starts and saves without Firebase/PlayFab/Play login, " +
        "skips the ad/analytics SDK startup calls, fixes the server clock, makes gem packs grant their " +
        "gems when tapped, and shows the real gem amounts in the shop. Only for v29.0.6 (build 1237).",
    default = true
) {
    compatibleWith(COMPATIBILITY_TOWER)

    // The native patches need the Remote Config cache to load (see SeedPatch.kt).
    dependsOn(towerSeedPatch)

    execute {
        val lib = get(IL2CPP_PATH)
        val data = lib.readBytes()

        val sha = MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }
        check(sha == IL2CPP_SHA256) {
            "libil2cpp.so is not The Tower v29.0.6 (build 1237) arm64-v8a: sha256 $sha. Refusing to patch."
        }

        // Verify everything first so a mismatch can never leave a half patched library.
        for (p in NATIVE_PATCHES) {
            val old = hex(p.old)
            for (i in old.indices) {
                check(data[p.fileOffset + i] == old[i]) {
                    "Unexpected code at file offset 0x%x (va 0x%x)".format(p.fileOffset, p.fileOffset + 0x4000)
                }
            }
        }
        for (p in NATIVE_PATCHES) {
            val new = hex(p.new)
            new.copyInto(data, p.fileOffset)
        }
        lib.writeBytes(data)
    }
}

private val changePackageOption = booleanOption(
    key = "changePackageName",
    default = true,
    title = "Change package name",
    description = "Install next to the original game instead of replacing it. The clone uses its own " +
        "data and save, and can never touch the original account.",
)

private val newPackageOption = stringOption(
    key = "packageName",
    default = TOWER_CLONE_PACKAGE,
    title = "New package name",
    description = "Package name of the clone. Must not be the original package name.",
    required = true,
) { it != null && it != TOWER_PACKAGE && Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+").matches(it) }

@Suppress("unused")
val towerPackagePatch = resourcePatch(
    name = "Change package name",
    description = "Renames the package so the patched game installs beside the original. Unity resolves " +
        "its string resources by package name, so the resource table is renamed together with the manifest.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TOWER)

    changePackageOption()
    newPackageOption()

    execute {
        if (changePackageOption.value != true) return@execute
        val newPackage = newPackageOption.value!!

        // Every occurrence: the package attribute, provider authorities, permissions, actions.
        fun rewrite(node: Node) {
            if (node is Element) {
                val attrs = node.attributes
                for (i in 0 until attrs.length) {
                    val a = attrs.item(i)
                    if (a.nodeValue.contains(TOWER_PACKAGE)) a.nodeValue = a.nodeValue.replace(TOWER_PACKAGE, newPackage)
                }
            } else if (node.nodeType == Node.TEXT_NODE && node.nodeValue?.contains(TOWER_PACKAGE) == true) {
                node.nodeValue = node.nodeValue.replace(TOWER_PACKAGE, newPackage)
            }
            var child = node.firstChild
            while (child != null) {
                rewrite(child)
                child = child.nextSibling
            }
        }

        document("AndroidManifest.xml").use { document ->
            rewrite(document.documentElement)
        }
    }
}
