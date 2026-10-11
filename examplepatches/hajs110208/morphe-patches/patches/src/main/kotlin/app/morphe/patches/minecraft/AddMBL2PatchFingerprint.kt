package app.morphe.patches.minecraft.mbl2

import app.morphe.patcher.Fingerprint

object MinecraftPlatformOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/mojang/minecraftpe/MainActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
