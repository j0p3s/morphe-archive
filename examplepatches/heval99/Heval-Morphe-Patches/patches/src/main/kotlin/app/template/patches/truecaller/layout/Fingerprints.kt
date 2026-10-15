package app.template.patches.truecaller.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

internal const val BOTTOM_BAR_BUTTON_TYPE = "Lcom/truecaller/bottombar/BottomBarButtonType;"

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

// The bottom bar builder (xm6.g(SparseIntArray) in 26.39.6): reads the CALLS / MESSAGES /
// SCAM_FEED badge counts, fills an array with one tab item per slot (null when a slot has no
// tab), then filterNotNull(array).take(4) and hands the list to BottomBarView, which ignores lists
// with fewer than two tabs. The tab items share an obfuscated base class whose only no-argument
// getter returning BottomBarButtonType (kept enum) gives each tab's type.
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
