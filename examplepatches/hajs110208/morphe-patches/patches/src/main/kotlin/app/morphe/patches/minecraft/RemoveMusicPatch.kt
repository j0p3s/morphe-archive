package app.morphe.patches.minecraft.nomusic

import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch

private const val MUSIC = "resource_packs/vanilla_music/"

@Suppress("unused")
val removeVanillaMusicPatch = resourcePatch(
    name = "Remove music",
    description = "Removes music files. This reduces app size.",
    default = false,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    execute {
        val directories = listApkEntries()
            .filter { it.contains(MUSIC) }
            .map { it.substring(0, it.indexOf(MUSIC) + MUSIC.length) }
            .toSet()

        if (directories.isEmpty()) {
            throw PatchException("folder does not exist")
        }

        directories.forEach { delete(it) }
    }
}
