/*
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * "Build Your Own Deck" is sold as 50 slots (com.wb.Ellen.HeadsUp.Deck.BuildYourOwn01..50,
 * multiplePurchaseCounter 1..50), and the store only offers the next one. Unlock all decks
 * owns all of them, which puts 50 empty tiles in My Decks.
 *
 * OwnedDecksManager::InitDecksList sorts each deck into OwnedDecks (My Decks) or HiddenDecks
 * by reading isDeckBought. That read is redirected to a routine that keeps paid slots with
 * cards and only the first empty one, so one empty slot is left to build a new deck in. The
 * routine lives in the body of GetIsBought, which Unlock all decks has turned into a
 * two-instruction stub. My Decks is built once per launch, so the next empty slot appears
 * after a restart.
 *
 * The hook is the masked ownership check inside the name-resolved InitDecksList, and the
 * instructions that put &OwnedDecks where the routine reads it are checked too.
 *
 * Verified on com.wb.headsup 4.15.11 (versionCode 4151100) and 4.15.13
 * (versionCode 4151300), arm64-v8a and armeabi-v7a.
 */
package app.lockhart.patches.headsup.misc.customdecks

import app.lockhart.patches.headsup.misc.premium.GET_IS_BOUGHT
import app.lockhart.patches.headsup.misc.premium.IL2CPP
import app.lockhart.patches.headsup.misc.premium.METADATA
import app.lockhart.patches.headsup.misc.premium.RETURN_TRUE_BY_ABI
import app.lockhart.patches.headsup.misc.premium.unlockAllDecksPatch
import app.lockhart.patches.shared.Constants.COMPATIBILITY_HEADS_UP
import app.lockhart.patches.shared.il2cpp.Il2CppLibrary
import app.lockhart.patches.shared.il2cpp.Il2CppMetadata
import app.lockhart.patches.shared.il2cpp.Il2CppMethod
import app.lockhart.patches.shared.il2cpp.MISSING_IL2CPP_MESSAGE
import app.lockhart.patches.shared.il2cpp.MaskedWord
import app.lockhart.patches.shared.il2cpp.arm64Call
import app.lockhart.patches.shared.il2cpp.armCall
import app.lockhart.patches.shared.il2cpp.findWords
import app.lockhart.patches.shared.il2cpp.hex
import app.lockhart.patches.shared.il2cpp.resolve
import app.lockhart.patches.shared.il2cpp.words
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import java.io.RandomAccessFile

private val INIT_DECKS_LIST = Il2CppMethod("HeadsUp.Scripts.Utils.OwnedDecksManager", "InitDecksList", parameterCount = 0)

private class NativeCode(
    // The isDeckBought read in InitDecksList that picks OwnedDecks or HiddenDecks; its first word is replaced.
    val ownedCheck: List<MaskedWord>,
    // Puts &OwnedDecks where oneEmptySlot reads it.
    val ownedDecksSetup: List<MaskedWord>,
    val oneEmptySlot: ByteArray,
    val call: (from: Long, to: Long) -> Long,
)

// Keyed by the ABI directory under lib/.
private val NATIVE_CODE_BY_ABI = mapOf(
    "arm64-v8a" to NativeCode(
        // ldrb w8, [x26, #0x190] ; cbz w8, <HiddenDecks>
        ownedCheck = listOf(MaskedWord(0x39464348), MaskedWord(0x34000008, 0xFF00001F.toInt())),
        // mov x21, x20 ; str xN, [x21, #0x10]!
        ownedDecksSetup = listOf(MaskedWord(0xAA1403F5.toInt()), MaskedWord(0xF8010EA0.toInt(), 0xFFFFFFE0.toInt())),
        // in: x26 = DeckData, x21 = &OwnedDecks ; out: w8 = keep in My Decks.
        // Passes everything but paid slots (isCustomDeck, multiplePurchaseCounter > 0) without
        // cards, and passes one of those while OwnedDecks holds no other.
        oneEmptySlot = hex(
            "48434639 08040034 49834639 c9030034 49ff41b9 89030034 49fb40f9 a90000b4 291940f9 690000b4 " +
                "291940b9 c9020035 aa0240f9 8a0200b4 4b1940b9 4a0940f9 2a0200b4 4a810091 eb010034 4c8540f8 " +
                "6b050051 acffffb4 8d814639 6dffff34 8dfd41b9 2dffff34 8df940f9 ad0000b4 ad1940f9 6d0000b4 " +
                "ad1940b9 6dfeff35 08008052 c0035fd6",
        ),
        call = ::arm64Call,
    ),
    "armeabi-v7a" to NativeCode(
        // ldrb r0, [r6, #0xec] ; cmp r0, #0 ; beq <HiddenDecks>
        ownedCheck = listOf(MaskedWord(0xE5D600EC.toInt()), MaskedWord(0xE3500000.toInt()), MaskedWord(0x0A000000, 0xFF000000.toInt())),
        // str rN, [r0, #8]! ; str r0, [sp, #0x10]
        ownedDecksSetup = listOf(MaskedWord(0xE5A00008.toInt(), 0xFFFF0FFF.toInt()), MaskedWord(0xE58D0010.toInt())),
        // in: r6 = DeckData, [sp, #0x10] = &OwnedDecks ; out: r0, same rule as arm64.
        oneEmptySlot = hex(
            "ec00d6e5 000050e3 1eff2f01 f410d6e5 000051e3 1eff2f01 281196e5 000051e3 1eff2f01 201196e5 " +
                "000051e3 18109115 00005113 0c109115 00005113 1eff2f11 10109de5 001091e5 000051e3 1eff2f01 " +
                "0c2091e5 081091e5 000051e3 1eff2f01 101081e2 012052e2 1eff2f41 043091e4 000053e3 faffff0a " +
                "f4c0d3e5 00005ce3 f7ffff0a 28c193e5 00005ce3 f4ffff0a 20c193e5 00005ce3 18c09c15 00005c13 " +
                "0cc09c15 00005c13 edffff1a 0000a0e3 1eff2fe1",
        ),
        call = ::armCall,
    ),
)

@Suppress("unused")
val hideEmptyCustomDecksPatch = resourcePatch(
    name = "Hide empty custom decks",
    description = "Shows one empty custom deck in My Decks instead of all of them. " +
        "A new one appears after you restart the app.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HEADS_UP)
    dependsOn(unlockAllDecksPatch)

    execute {
        val metadataFile = get(METADATA)
        if (!metadataFile.isFile) throw PatchException("Could not find $METADATA")
        val metadata = Il2CppMetadata(metadataFile.readBytes())

        val libraries = get("lib").walk().filter { it.name == IL2CPP && it.isFile }.toList()
        if (libraries.isEmpty()) throw PatchException(MISSING_IL2CPP_MESSAGE)

        for (library in libraries) {
            val abi = library.parentFile.name
            val code = NATIVE_CODE_BY_ABI[abi] ?: throw PatchException("Unsupported ABI $abi for $IL2CPP")
            val returnTrue = RETURN_TRUE_BY_ABI.getValue(abi)

            val bytes = library.readBytes()
            val il2cpp = Il2CppLibrary(bytes)
            val offsets = il2cpp.resolve(metadata, listOf(GET_IS_BOUGHT, INIT_DECKS_LIST))

            val initDecksList = offsets.getValue(INIT_DECKS_LIST).toInt()
            val initDecksListEnd = il2cpp.functionEnd(initDecksList.toLong()).toInt()

            val hooks = bytes.findWords(initDecksList, initDecksListEnd, code.ownedCheck)
            val hook = hooks.singleOrNull()
                ?: throw PatchException("Expected one My Decks ownership check in $abi, found ${hooks.size}")
            if (bytes.findWords(initDecksList, hook, code.ownedDecksSetup).size != 1) {
                throw PatchException("My Decks list setup has an unexpected shape in $abi")
            }

            // The routine overwrites GetIsBought's body, which is only dead once Unlock all decks stubbed it.
            val getIsBought = offsets.getValue(GET_IS_BOUGHT)
            val stub = bytes.copyOfRange(getIsBought.toInt(), getIsBought.toInt() + returnTrue.size)
            if (!stub.contentEquals(returnTrue)) throw PatchException("GetIsBought is not stubbed in $abi")

            val cave = getIsBought + returnTrue.size
            if (cave + code.oneEmptySlot.size > il2cpp.functionEnd(getIsBought)) {
                throw PatchException("GetIsBought is too small for the custom deck routine in $abi")
            }

            RandomAccessFile(library, "rw").use { file ->
                file.seek(cave)
                file.write(code.oneEmptySlot)
                file.seek(hook.toLong())
                file.write(words(code.call(hook.toLong(), cave)))
            }
        }
    }
}
