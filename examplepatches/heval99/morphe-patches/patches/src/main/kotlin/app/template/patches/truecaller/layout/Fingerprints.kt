package app.template.patches.truecaller.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal const val BOTTOM_BAR_BUTTON_TYPE = "Lcom/truecaller/bottombar/BottomBarButtonType;"
internal const val BOTTOM_BAR_VIEW = "Lcom/truecaller/bottombar/BottomBarView;"

// Feature toggles are read as isEnabled("<key>", default state) in small R8-renamed getters. The
// keys are exact-matched: both are prefixes of other toggles ("featureCallAssistantNumberSync",
// "featureScamFeedBottomTab", ...), which a plain `strings` match would also accept.

// Master switch of the Call Assistant: about 30 call sites check it, among them the Assistant
// tab, its deep links, FCM message handling and the assistant auth token worker.
internal object CallAssistantFeatureFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(string("featureCallAssistant")),
)

// Master switch of the Scam Feed: the bottom tab, the drawer menu entry, its deep link, the
// call-history hook and the Scam Feed notification worker all check it first.
internal object ScamFeedFeatureFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(string("featureScamFeed")),
)

// The bottom bar builder (xm6.g(SparseIntArray) in 26.39.6, cp6.g in 26.40.5): reads the CALLS /
// MESSAGES / SCAM_FEED badge counts, fills an array with one tab item per slot (null when a slot
// has no tab), then filterNotNull(array).take(n) and hands the list to BottomBarView (inlined in
// 26.40.5), which ignores lists with fewer than two tabs. The tab items share an obfuscated base
// class whose only no-argument getter returning BottomBarButtonType (kept enum) gives each tab's
// type.
internal object BottomBarTabsFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/util/SparseIntArray;"),
    filters = listOf(
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = BOTTOM_BAR_BUTTON_TYPE, name = "CALLS"),
        fieldAccess(opcode = Opcode.SGET_OBJECT, definingClass = BOTTOM_BAR_BUTTON_TYPE, name = "MESSAGES"),
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            parameters = listOf("[Ljava/lang/Object;"),
            returnType = "Ljava/util/List;",
        ),
        methodCall(
            definingClass = "Lkotlin/collections/CollectionsKt;",
            parameters = listOf("Ljava/lang/Iterable;", "I"),
            returnType = "Ljava/util/List;",
        ),
    ),
)

// The builder class's default tab getter (cp6.c() in 26.40.5): "default_tab_on_launch" == 0 ?
// CALLS : MESSAGES. The back button reads it ("return to the default tab before leaving"); the
// launch-tab helper reads the pref itself, but its choice then goes through selectTab below.
internal object DefaultTabFingerprint : Fingerprint(
    classFingerprint = BottomBarTabsFingerprint,
    returnType = BOTTOM_BAR_BUTTON_TYPE,
    parameters = listOf(),
    filters = listOf(string("default_tab_on_launch")),
)

// toString() of the main Settings screen's state (a7a in 26.40.5), a Kotlin data class whose
// isPremiumItemVisible / isWatchItemVisible / ... flags CategoriesFragment applies to its fixed
// rows (item_premium, item_watch, ... plus their dividers). Only the stable prefix is matched.
internal object CategoriesSettingsStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(string("CategoriesSettingsState(", StringComparisonType.STARTS_WITH)),
)

// The builder class's selectTab(type, fromUser) (cp6.e in 26.40.5): every programmatic tab switch
// (launch tab, notification and deep-link intents, back button) goes through it. It calls the
// fragment switch directly for CALLS on a fresh bar, otherwise NavigationBarView.setSelectedItemId,
// which does nothing for a tab that is not in the bar.
internal object SelectTabFingerprint : Fingerprint(
    classFingerprint = BottomBarTabsFingerprint,
    returnType = "V",
    parameters = listOf(BOTTOM_BAR_BUTTON_TYPE, "Z"),
    filters = listOf(methodCall(name = "setSelectedItemId", parameters = listOf("I"))),
)
