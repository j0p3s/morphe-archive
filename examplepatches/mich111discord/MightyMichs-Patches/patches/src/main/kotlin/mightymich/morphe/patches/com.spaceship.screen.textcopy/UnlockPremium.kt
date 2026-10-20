package mightymich.morphe.patches.com.spaceship.screen.textcopy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium Features",
    description = "Unlocks Screen Translate Premium.",
    default = true
) {
    compatibleWith(ScreenTranslateCompatibility.SCREEN_TRANSLATE)

    /**
     * Main isPremium check.
     * Does NOT rely on the short obfuscated class name ("d").
     * Instead matches any static method that returns Z and references
     * the Kotlin inner class generated from PremiumUtilsKt.isPremium / isPurchased.
     * That name survives R8 because it comes from the original source file.
     */
    val premiumCheckFingerprint = Fingerprint(
        returnType = "Z",
        parameters = emptyList(),
        custom = { method, _ ->
            // Must be a static method (the original is static)
            method.accessFlags and 0x0008 /* ACC_STATIC */ != 0 &&
                method.implementation?.instructions?.any { insn ->
                    val s = insn.toString()
                    s.contains("PremiumUtilsKt\$isPremium") ||
                        s.contains("isPremium\$isPurchased") ||
                        s.contains("PremiumUtilsKt\$isPurchased")
                } == true
        }
    )

    /**
     * Config feature-gating getters.
     * Method names are Kotlin property getters and stay stable across builds.
     * Class path "manager/config/Config" is also stable.
     */
    val homeKeyNeedPremiumFingerprint = Fingerprint(
        definingClass = "Lcom/spaceship/screen/translate/manager/config/Config;",
        name = "getHomeKeyActionNeedPremium",
        returnType = "Z"
    )

    val gestureNeedPremiumFingerprint = Fingerprint(
        definingClass = "Lcom/spaceship/screen/translate/manager/config/Config;",
        name = "getGestureControlNeedPremium",
        returnType = "Z"
    )

    val mangaNeedPremiumFingerprint = Fingerprint(
        definingClass = "Lcom/spaceship/screen/translate/manager/config/Config;",
        name = "getMangaNeedPremium",
        returnType = "Z"
    )

    execute {
        // 1. Force isPremium() → true
        val premiumMethod = premiumCheckFingerprint.method
            ?: throw PatchException(
                "Could not find isPremium check. " +
                "Looked for static method returning Z that references PremiumUtilsKt.isPremium/isPurchased."
            )
        premiumMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """
        )

        // 2. Force all "need premium" flags → false
        listOf(
            homeKeyNeedPremiumFingerprint to "getHomeKeyActionNeedPremium",
            gestureNeedPremiumFingerprint to "getGestureControlNeedPremium",
            mangaNeedPremiumFingerprint to "getMangaNeedPremium"
        ).forEach { (fp, name) ->
            val method = fp.method
                ?: throw PatchException("Could not find Config.$name()")
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """
            )
        }
    }
}
