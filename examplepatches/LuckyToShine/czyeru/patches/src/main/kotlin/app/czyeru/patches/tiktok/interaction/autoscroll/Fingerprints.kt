package app.czyeru.patches.tiktok.interaction.autoscroll

import app.morphe.patcher.Fingerprint

internal object AutoScrollFeatureGateFingerprint : Fingerprint(
    definingClass = "Lfhd/o1;",
    name = "LIZ",
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("fyp_auto_scroll"),
)

internal object AutoScrollActionFactoryFingerprint : Fingerprint(
    definingClass = "LX/0pm6;",
    name = "LJIIIIZZ",
    returnType = "LX/0pmU;",
    parameters = listOf("LX/0pnN;"),
    strings = listOf("panel_auto_scroll", "auto_scroll"),
)
