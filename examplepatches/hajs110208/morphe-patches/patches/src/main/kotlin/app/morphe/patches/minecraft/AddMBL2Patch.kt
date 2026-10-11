package app.morphe.patches.minecraft.mbl2

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch

private val patchClassLoader = object {}.javaClass.classLoader

private val NATIVE_ABIS = listOf(
    "arm64-v8a",
    "armeabi-v7a",
    "x86_64",
)

private val mtbinloader2LibPatch = resourcePatch {
    execute {
        var copied = 0

        NATIVE_ABIS.forEach { abi ->
            val source = "minecraft/lib/$abi/libmtbinloader2.so"
            val target = "lib/$abi/libmtbinloader2.so"
            val input = patchClassLoader.getResourceAsStream(source) ?: return@forEach

            input.use { stream ->
                val file = get(target, false)
                file.parentFile?.mkdirs()
                file.outputStream().use { out -> stream.copyTo(out) }
            }
            copied++
        }

        if (copied == 0) {
            throw PatchException("libmtbinloader2.so not found in patch resources (minecraft/lib/<abi>/)")
        }
    }
}

@Suppress("unused")
val mtbinloader2Patch = bytecodePatch(
    name = "Add mtbinloader2 lib",
    description = "Add libmtbinloader2.so to the app's native library path.",
    default = true,
) {
    compatibleWith(
        Compatibility(
            packageName = "com.mojang.minecraftpe",
            name = "Minecraft",
        )
    )

    dependsOn(mtbinloader2LibPatch)

    execute {
        MinecraftPlatformOnCreateFingerprint.method.addInstructions(
            0,
            """
            const-string v0, "mtbinloader2"
            invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
            """,
        )
    }
}
