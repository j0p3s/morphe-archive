/*
 * Forked from:
 * https://gitlab.com/ReVanced/revanced-patches/-/blob/main/patches/src/main/kotlin/app/revanced/patches/tiktok/misc/settings/Fingerprints.kt
 */
package app.czyeru.patches.tiktok.misc.settings

import app.morphe.patcher.Fingerprint

internal object AddSettingsEntryFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/SettingNewVersionFragment;") && method.name == "initUnitManger"
    },
)

internal object AdPersonalizationActivityOnCreateFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") && method.name == "onCreate"
    },
)

internal object AdPersonalizationActivityOnBackPressedFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/AdPersonalizationActivity;") &&
            method.name == "onBackPressed" &&
            method.parameterTypes.isEmpty() &&
            method.returnType == "V"
    },
)

internal object SettingsEntryFingerprint : Fingerprint(
    strings = listOf("pls pass item or extends the EventUnit"),
)

internal object SettingsEntryInfoFingerprint : Fingerprint(
    strings = listOf(
        "ExposeItem(title=",
        ", icon=",
    ),
)

internal object SettingsStatusLoadFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("Lapp/czyeru/extension/tiktok/settings/SettingsStatus;") && method.name == "load"
    },
)

internal object NpthExtentTaskInitFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/NpthExtentTask;") &&
            method.name == "LIZ" &&
            method.returnType == "V"
    },
)

/**
 * The compose function that builds the whole settings list: it reads the group view models,
 * sorts their rows and hands the result to the lazy column. It is the only method of the
 * fragment with 11 parameters that takes the support group view model (`ER` in TikTok 46.2.3,
 * `oX` in 47.2.41).
 */
internal object SettingsComposeRowsFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/SettingsComposeRvmpFragment;") &&
            method.returnType == "V" &&
            method.parameterTypes.size == 11 &&
            method.parameterTypes.any { it.endsWith("/group/support/SupportGroupVM;") }
    },
)

internal object SupportGroupDefaultStateFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/SupportGroupVM;") && method.name == "defaultState"
    },
)

internal object OpenDebugCellVmDefaultStateFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("/OpenDebugCellVM;") && method.name == "defaultState"
    },
)

internal object OpenDebugCellClickWrapperFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.endsWith("Lkotlin/jvm/internal/AwS350S0200000_2;") &&
            method.name == "invoke\$85" &&
            method.parameterTypes == listOf("Lkotlin/jvm/internal/AwS350S0200000_2;")
    },
)

