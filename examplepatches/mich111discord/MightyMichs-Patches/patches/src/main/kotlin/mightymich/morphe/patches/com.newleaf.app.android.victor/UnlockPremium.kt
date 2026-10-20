package mightymich.morphe.patches.reelshort

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch

/**
 * Unlocks ReelShort VIP / premium.
 *
 * Key findings in 4.2.20:
 * - Account.getVip_status()I  → must be 1 (app checks == 1; old patch returned 2 which failed)
 * - EpisodeEntity / InteractEntity:
 *     isVipFreeAdvUnlock()I → 1
 *     is_adv_unlock()I      → 1
 * - PlayletEntity / InteractPlayletEntity.getVip_status()I → 1
 *
 * Method names are stable Kotlin getters (not R8-obfuscated).
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium Features",
    description = "Unlocks ReelShort VIP (vip_status=1) and unlocks ad-gated episodes.",
    default = true
) {
    compatibleWith(ReelShortCompatibility.REELSHORT)

    // Account VIP status (user is VIP when value == 1)
    val accountVipStatusFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/bean/Account;",
        name = "getVip_status",
        returnType = "I"
    )

    // Episode unlock flags
    val episodeVipFreeAdvFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/player/bean/EpisodeEntity;",
        name = "isVipFreeAdvUnlock",
        returnType = "I"
    )

    val episodeAdvUnlockFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/player/bean/EpisodeEntity;",
        name = "is_adv_unlock",
        returnType = "I"
    )

    val episodeVipStatusFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/player/bean/EpisodeEntity;",
        name = "getVip_status",
        returnType = "I"
    )

    // Playlet (series) VIP
    val playletVipStatusFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/player/bean/PlayletEntity;",
        name = "getVip_status",
        returnType = "I"
    )

    // Interactive player variants
    val interactVipFreeAdvFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/interackPlayer/bean/InteractEntity;",
        name = "isVipFreeAdvUnlock",
        returnType = "I"
    )

    val interactAdvUnlockFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/interackPlayer/bean/InteractEntity;",
        name = "is_adv_unlock",
        returnType = "I"
    )

    val interactVipStatusFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/interackPlayer/bean/InteractEntity;",
        name = "getVip_status",
        returnType = "I"
    )

    val interactPlayletVipStatusFingerprint = Fingerprint(
        definingClass = "Lcom/newleaf/app/android/victor/interackPlayer/bean/InteractPlayletEntity;",
        name = "getVip_status",
        returnType = "I"
    )

    execute {
        val returnOne = """
            const/4 v0, 0x1
            return v0
        """

        // Required: Account VIP
        val accountVip = accountVipStatusFingerprint.method
            ?: throw PatchException("Could not find Account.getVip_status()")
        accountVip.addInstructions(0, returnOne)

        // Required: episode ad/VIP unlock
        val epVipFree = episodeVipFreeAdvFingerprint.method
            ?: throw PatchException("Could not find EpisodeEntity.isVipFreeAdvUnlock()")
        epVipFree.addInstructions(0, returnOne)

        val epAdv = episodeAdvUnlockFingerprint.method
            ?: throw PatchException("Could not find EpisodeEntity.is_adv_unlock()")
        epAdv.addInstructions(0, returnOne)

        // Best-effort extras (same stable names)
        listOf(
            episodeVipStatusFingerprint,
            playletVipStatusFingerprint,
            interactVipFreeAdvFingerprint,
            interactAdvUnlockFingerprint,
            interactVipStatusFingerprint,
            interactPlayletVipStatusFingerprint
        ).forEach { fp ->
            try {
                fp.method?.addInstructions(0, returnOne)
            } catch (_: Exception) {
                // optional – class may not exist in all variants
            }
        }
    }
}
