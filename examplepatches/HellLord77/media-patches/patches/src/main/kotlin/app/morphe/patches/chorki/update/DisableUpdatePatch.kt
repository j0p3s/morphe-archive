package app.morphe.patches.chorki.update


import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.all.library.keepArmNativeLibraryPatch
import app.morphe.patches.all.misc.hex.hexPatch
import app.morphe.patches.chorki.shared.Constants.COMPATIBILITY_CHORKI
import app.morphe.patches.shared.requireArm

@Suppress("unused")
val disableUpdatePatch = rawResourcePatch(
    name = "Disable Update",
    description = "Disables forced update.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_CHORKI)

    availability(requireArm)

    dependsOn(
        keepArmNativeLibraryPatch,
        *Array(4) {
            hexPatch(true, block = {
                // package:chorki/data/models/settings_model.json -> SettingsModel.fromJson
                // min_app_version -> social_link
                // ADD             R2, R5, #0xD000
                // LDR             R2, [R2,#0x967] -> LDR             R2, [R2,#0x947]
                """
                    0d 2a 85 e2
                    67 29 92 e5
                """ asPatternTo """
                    0d 2a 85 e2
                    47 29 92 e5
                """ inFile "lib/armeabi-v7a/libapp.so"
            })
        },
        *Array(4) {
            hexPatch(true, block = {
                // package:chorki/data/models/settings_model.json -> SettingsModel.fromJson
                // min_app_version -> social_link
                // ADD             X2, X27, #0x19,LSL#12
                // LDR             X2, [X2,#0XBA8]       -> LDR             X0, [X0,#0XB68]
                """
                    62 67 40 91
                    42 d4 45 f9
                """ asPatternTo """
                    62 67 40 91
                    42 b4 45 f9
                """ inFile "lib/arm64-v8a/libapp.so"
            })
        },
    )
}