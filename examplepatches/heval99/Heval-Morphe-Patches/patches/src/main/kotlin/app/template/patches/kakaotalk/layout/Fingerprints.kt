// Ported from AmpleReVanced/revanced-patches (GPL-3.0), layout/tab/fingerprints, written for
// KakaoTalk 26.7.2. Changes for 26.8.3: the tab config constructor is scoped by its data class
// toString, and the friend feed gate is taken from the settings view model's single ()Z call.

package app.template.patches.kakaotalk.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.methodCall
import app.morphe.util.getReference
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference

internal const val TAB_INFO = "Lcom/kakao/talk/activity/main/MainTabPagerAdapter\$TabInfo;"

// MainTabConfig's bottom tab list: LinkedHashMap.put(slot, resolve(slot, config)) for slots 0-4,
// then filterNotNull(values). Slot 3 resolves to SHOPPING_TAB, or CALL_TAB without shopping.
internal object AddNavigationTabFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.NEW_INSTANCE,
        Opcode.INVOKE_DIRECT,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
        Opcode.CONST_4,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_INTERFACE,
    ),
    custom = { _, classDef -> classDef.sourceFile == "MainTabConfig.kt" && !classDef.type.contains("$") },
)

// OpenChat tab: the fragment method that builds the RecyclerView's ConcatAdapter from the chat
// list adapter plus, behind a flag, the community (horizontal list) and feed adapters.
internal object SetupAdapterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    returnType = "V",
    filters = listOf(
        methodCall(
            definingClass = "Landroidx/recyclerview/widget/RecyclerView;",
            name = "setAdapter",
            parameters = listOf("L"),
            returnType = "V",
        ),
        methodCall(
            definingClass = "Landroidx/recyclerview/widget/RecyclerView;",
            name = "addOnScrollListener",
            parameters = listOf("L"),
            returnType = "V",
        ),
    ),
    custom = { method, classDef ->
        classDef.sourceFile == "OpenChatTabFragment.kt" && !classDef.type.contains("$") &&
            method.implementation?.instructions?.let { instructions ->
                val setAdapter = instructions.firstNotNullOfOrNull { instruction ->
                    instruction.getReference<MethodReference>()?.takeIf {
                        it.definingClass == "Landroidx/recyclerview/widget/RecyclerView;" &&
                            it.name == "setAdapter" && it.parameterTypes.size == 1 && it.returnType == "V"
                    }
                }
                val adapterArrayType = setAdapter?.parameterTypes?.single()?.let { "[$it" }
                setAdapter != null && instructions.any {
                    it.opcode == Opcode.NEW_ARRAY && it.getReference<TypeReference>()?.type == adapterArrayType
                }
            } == true
    },
)

// The chat room list adapter class shared by the chat and OpenChat tabs.
internal object CommonChatRoomListAdapterClassFingerprint : Fingerprint(
    custom = { _, classDef -> classDef.sourceFile == "CommonChatRoomListAdapter.kt" && !classDef.type.contains("$") },
)

// MainTabConfig's TabConfig data class; its first field is isFriendFeedTabEnabled.
internal object TabConfigToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    strings = listOf("TabConfig(isFriendFeedTabEnabled="),
)

internal object TabConfigConstructorFingerprint : Fingerprint(
    classFingerprint = TabConfigToStringFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_DIRECT,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_BOOLEAN,
        Opcode.IPUT_OBJECT,
    ),
    custom = { _, classDef -> classDef.sourceFile == "MainTabConfig.kt" },
)

// Friend settings view model: picks the friend feed or the friend list settings model from the
// FriendTab "friend feed enabled" gate, its only ()Z call.
internal object DetermineFeedOrListFingerprint : Fingerprint(
    name = "invokeSuspend",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    filters = listOf(methodCall(name = "<init>", parameters = listOf("Z", "Z", "Z"), returnType = "V")),
    custom = { _, classDef -> classDef.sourceFile == "FriendSettingsViewModel.kt" },
)
