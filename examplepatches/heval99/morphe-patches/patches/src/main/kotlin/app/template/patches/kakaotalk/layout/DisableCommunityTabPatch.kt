// Ported from AmpleReVanced/revanced-patches (GPL-3.0), layout/tab/DisableCommunityTabPatch.kt,
// written for KakaoTalk 26.7.2. The edit is unchanged; only the imports and smali formatting follow
// this repo.

package app.template.patches.kakaotalk.layout

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import app.morphe.util.indexOfFirstInstructionReversedOrThrow
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ThreeRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

@Suppress("unused")
val disableCommunityTabPatch = bytecodePatch(
    name = "Disable community tab",
    description = "Shows only your open chat rooms in the OpenChat tab, without the community " +
        "list and feed below them.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        SetupAdapterFingerprint.method.apply {
            val fragmentType = SetupAdapterFingerprint.classDef.type

            // 1. recyclerView.setAdapter(concat): the ConcatAdapter register and its type.
            val setAdapterIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.let {
                    it.definingClass == "Landroidx/recyclerview/widget/RecyclerView;" && it.name == "setAdapter" &&
                        it.parameterTypes.size == 1 && it.returnType == "V"
                } == true
            }
            val setAdapter = getInstruction<FiveRegisterInstruction>(setAdapterIndex)
            val setAdapterReference = setAdapter.getReference<MethodReference>()
                ?: throw PatchException("Could not inspect the OpenChat RecyclerView.setAdapter call")
            val targetAdapterRegister = setAdapter.registerD
            val adapterArrayType = "[${setAdapterReference.parameterTypes.single()}"

            // 2. The nearest new ConcatAdapter(adapters[]) into that register.
            val concatConstructorIndex = indexOfFirstInstructionReversedOrThrow(setAdapterIndex - 1) {
                opcode == Opcode.INVOKE_DIRECT && (this as? FiveRegisterInstruction)?.registerC == targetAdapterRegister &&
                    getReference<MethodReference>()?.let {
                        it.name == "<init>" && it.parameterTypes == listOf(adapterArrayType) && it.returnType == "V"
                    } == true
            }
            val concatConstructorInstruction = getInstruction<FiveRegisterInstruction>(concatConstructorIndex)
            val concatConstructor = concatConstructorInstruction.getReference<MethodReference>()
                ?: throw PatchException("Could not inspect the OpenChat ConcatAdapter constructor")
            val concatAdapterType = concatConstructor.definingClass
            val adapterArrayRegister = concatConstructorInstruction.registerD

            val concatAdapterIndex = indexOfFirstInstructionReversedOrThrow(concatConstructorIndex - 1) {
                opcode == Opcode.NEW_INSTANCE && (this as? OneRegisterInstruction)?.registerA == targetAdapterRegister &&
                    getReference<TypeReference>()?.type == concatAdapterType
            }

            // 3. The fragment's boolean flag that chooses between [chats, community, feed] and
            // [chats, feed].
            val conditionIndex = indexOfFirstInstructionReversedOrThrow(concatAdapterIndex - 1) {
                opcode == Opcode.INVOKE_VIRTUAL && getReference<MethodReference>()?.let {
                    it.definingClass == fragmentType && it.parameterTypes.isEmpty() && it.returnType == "Z"
                } == true
            }
            val conditionResult = getInstruction(conditionIndex + 1)
            if (conditionResult.opcode != Opcode.MOVE_RESULT) {
                throw PatchException("Could not inspect the OpenChat community adapter condition")
            }
            val conditionRegister = (conditionResult as OneRegisterInstruction).registerA
            indexOfFirstInstructionOrThrow(conditionIndex + 2) {
                opcode == Opcode.IF_EQZ && (this as? OneRegisterInstruction)?.registerA == conditionRegister
            }

            // 4. The chat list adapter field, the array register and the zero index register.
            val chatListAdapterType = CommonChatRoomListAdapterClassFingerprint.classDef.type
            val chatListAdapterGetIndex = indexOfFirstInstructionOrThrow(conditionIndex) {
                opcode == Opcode.IGET_OBJECT && getReference<FieldReference>()?.type == chatListAdapterType
            }
            val chatListAdapterGet = getInstruction<TwoRegisterInstruction>(chatListAdapterGetIndex)
            val chatListAdapterField = chatListAdapterGet.getReference<FieldReference>()
                ?: throw PatchException("Could not inspect the OpenChat chat list adapter field")
            val chatListAdapterRegister = chatListAdapterGet.registerA
            val fragmentRegister = chatListAdapterGet.registerB

            indexOfFirstInstructionOrThrow(conditionIndex) {
                opcode == Opcode.NEW_ARRAY && (this as? TwoRegisterInstruction)?.registerA == adapterArrayRegister &&
                    getReference<TypeReference>()?.type == adapterArrayType
            }
            val zeroRegister = getInstruction<ThreeRegisterInstruction>(
                indexOfFirstInstructionOrThrow(conditionIndex) {
                    opcode == Opcode.APUT_OBJECT && (this as? ThreeRegisterInstruction)?.let {
                        it.registerA == chatListAdapterRegister && it.registerB == adapterArrayRegister
                    } == true
                },
            ).registerC

            // 5. Replace the whole branch with new ConcatAdapter(new Adapter[] { chatListAdapter }).
            removeInstructions(conditionIndex, setAdapterIndex - conditionIndex)
            addInstructions(
                conditionIndex,
                """
                    new-instance v$targetAdapterRegister, $concatAdapterType
                    iget-object v$chatListAdapterRegister, v$fragmentRegister, $chatListAdapterField
                    const/16 v$adapterArrayRegister, 0x1
                    new-array v$adapterArrayRegister, v$adapterArrayRegister, $adapterArrayType
                    const/16 v$zeroRegister, 0x0
                    aput-object v$chatListAdapterRegister, v$adapterArrayRegister, v$zeroRegister
                    invoke-direct {v$targetAdapterRegister, v$adapterArrayRegister}, $concatConstructor
                """,
            )
        }
    }
}
