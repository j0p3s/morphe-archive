import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OffsetInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.iface.value.MethodEncodedValue
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val LINE_PKG = "jp.naver.line.android"
private const val HEADER_BUTTON = "Ljp/naver/line/android/common/view/header/HeaderButton;"
private const val SMART_CHANNEL_VIEW_LAYOUT = "Lcom/linecorp/line/admolin/smartch/v2/view/SmartChannelViewLayout;"
private const val LAD_AD_VIEW = "Lcom/linecorp/line/ladsdk/ui/common/view/lifecycle/LadAdView;"
private const val LYAD_AD_VIEW = "Lcom/linecorp/line/ladsdk/ui/v2/common/lifecycle/LyadAdView;"
private const val TALK_SERVICE_CLIENT = "Ljp/naver/line/android/thrift/client/TalkServiceClient;"
private const val LINE_VOOM_ACTIVITY = "Lcom/linecorp/line/timeline/notification/impl/activity/LineVoomActivity;"
private const val KEEP_UNSENT_MESSAGES = "Lapp/template/extension/line/KeepUnsentMessages;"
private const val READ_WITHOUT_RECEIPT = "Lapp/template/extension/line/ReadWithoutReceipt;"
private const val READ_WITHOUT_RECEIPT_ACTION = "Lapp/template/extension/line/ReadWithoutReceiptMenuAction;"
private const val LINE_ORIGINAL_CERT_SHA1 = "89396DC419292473972813922867E6973D6F5C50"
private const val GREEN_DOT_SET_COROUTINE =
    "com.linecorp.line.chattab.header.ChatTabHeaderStateImpl\$greenDotVisibleIconSet\$1"

private val FLAG_KEYS = listOf(
    "function.ai.character.display_button",
    "function.ai.agenti.enabled",
    "function.search.line_ai_entry.enabled",
)

class LineSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Method.params() = parameterTypes.map(CharSequence::toString)

    private fun Method.label() = "$definingClass->$name(${params().joinToString("")})$returnType"

    private fun Instruction.call(): MethodReference? = (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun Instruction.field(): FieldReference? = (this as? ReferenceInstruction)?.reference as? FieldReference

    private fun Instruction.string(): String? = ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

    private fun Instruction.isLiteral(opcode: Opcode, value: Int) =
        this.opcode == opcode && (this as NarrowLiteralInstruction).narrowLiteral == value

    private fun Instruction.register() = (this as OneRegisterInstruction).registerA

    // Register number of parameter [index] (0 = first declared parameter).
    private fun Method.parameterRegister(index: Int): Int {
        val sizes = params().map { if (it == "J" || it == "D") 2 else 1 }
        return implementation!!.registerCount - sizes.sum() + sizes.take(index).sum()
    }

    // Index of each branch's target, from the dex code-unit offsets.
    private fun branchTargets(insns: List<Instruction>): Set<Int> {
        val offsets = insns.runningFold(0) { offset, insn -> offset + insn.codeUnits }
        val indexAt = offsets.withIndex().associate { (index, offset) -> offset to index }
        return insns.withIndex().filter { (_, it) -> it is OffsetInstruction }.mapNotNull { (index, insn) ->
            indexAt[offsets[index] + (insn as OffsetInstruction).codeOffset]
        }.toSet()
    }

    private fun single(label: String, found: List<Method>): Method {
        check(found.size == 1) { "expected one $label, found ${found.map { it.label() }}" }
        return found.single()
    }

    /** A method with code and its decoded instructions. */
    private class Indexed(val classDef: ClassDef, val method: Method, val insns: List<Instruction>)

    // Strings that the lookups below search for.
    private val lookupStrings = FLAG_KEYS.toSet() + setOf(
        "getBanners", "getPrefetchableBanners", "X-Android-Cert", "createEndButtons", "hashtaglist",
        "reactions", "sendChatChecked", "CUSTOM_TABS_MODE", "ChatRowMenuItem(labelRes=",
    )

    // Whether any lookup below could match [m]: a lookup string, or one of the shapes it checks.
    private fun isCandidate(m: Indexed): Boolean {
        val method = m.method
        return m.insns.any { it.string() in lookupStrings } ||
            (method.returnType == "Ljava/util/List;" && method.parameterTypes.isEmpty()) ||
            (method.name == "<init>" && method.params().firstOrNull() == "Landroid/widget/FrameLayout;") ||
            (method.name == "<clinit>" && m.classDef.fields.any { it.name == "isVisible" }) ||
            (method.name == "invokeSuspend" && m.classDef.annotations.any { a -> a.elements.any { it.name == "c" } }) ||
            (method.returnType == "V" && method.params() == listOf("J", "Ljava/lang/String;", "Z")) ||
            m.insns.any { it.call()?.definingClass == READ_WITHOUT_RECEIPT } ||
            m.insns.any { insn ->
                insn.field()?.let { it.definingClass == HEADER_BUTTON || it.name == "GREEN_DOT_BADGE" } == true
            }
    }

    @Test
    fun `LINE patches remove ads, tabs, header buttons, badges, Agent i and VOOM, keep chats unread and unsent, read without receipt, open links externally and fix push`() {
        val root = repoRoot()
        val apk = File(root, "apks/line/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = LINE_PKG,
            version = "26.15.1",
            patchNames = setOf(
                "Disable ads",
                "Fix push notifications",
                "Hide LINE TODAY tab",
                "Hide Shopping tab",
                "Hide VOOM tab",
                "Hide Wallet tab",
                "Hide community button",
                "Hide calendar button",
                "Hide AI friends button",
                "Disable Agent i",
                "Hide new badges",
                "Disable VOOM",
                "Keep unsent messages",
                "Keep chats unread",
                "Read without receipt",
                "Read notifications without receipt",
                "Open links in external browser",
            ),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        val byType = classes.associateBy { it.type }
        // One decoding pass; keep only the methods some lookup below can match, so the ~280 MB
        // APK's instructions are not all held in memory at once.
        val indexed = classes.flatMap { cls ->
            cls.methods.filter { it.implementation != null }.mapNotNull { method ->
                val insns = method.instructions()
                Indexed(cls, method, insns).takeIf { isCandidate(it) }
            }
        }
        fun withStrings(vararg strings: String) = indexed.filter { m ->
            strings.all { s -> m.insns.any { it.string() == s } }
        }

        assertDisableAds(indexed, byType, ::withStrings)
        assertFixPush(::withStrings)
        assertTabs(indexed)
        val iconEnum = assertHeaderButtons(byType, ::withStrings)
        assertServerFlags(indexed, byType)
        assertNewBadges(indexed, byType, iconEnum)
        assertDisableVoom(byType, ::withStrings)
        assertKeepUnsent(byType, ::withStrings)
        assertKeepUnread(indexed, byType, ::withStrings)
        assertReadWithoutReceipt(indexed, byType, ::withStrings)
        assertExternalBrowser(byType, ::withStrings)
    }

    private fun assertDisableAds(
        indexed: List<Indexed>,
        byType: Map<String, ClassDef>,
        withStrings: (Array<out String>) -> List<Indexed>,
    ) {
        // 1. Both Smart Channel receive branches return null right after their op-name string.
        for (operation in listOf("getBanners", "getPrefetchableBanners")) {
            val receive = single("$operation receive lambda", withStrings(arrayOf(operation, "$operation failed: unknown result"))
                .filter { it.method.returnType == "Ljava/lang/Object;" }.map { it.method })
            val insns = receive.instructions()
            val name = insns.indexOfFirst { it.string() == operation }
            check(insns.getOrNull(name + 1)?.isLiteral(Opcode.CONST_4, 0) == true &&
                insns.getOrNull(name + 2)?.opcode == Opcode.RETURN_OBJECT &&
                (insns[name + 2]).register() == insns[name + 1].register()) {
                "${receive.label()} does not return null after \"$operation\""
            }
        }

        // 2. The Smart Channel controller hides its host FrameLayout (p1) first.
        val controller = single("Smart Channel controller", indexed.filter { m ->
            m.method.name == "<init>" && m.method.params().firstOrNull() == "Landroid/widget/FrameLayout;" &&
                m.insns.any { it.opcode == Opcode.CHECK_CAST && ((it as ReferenceInstruction).reference as TypeReference).type == SMART_CHANNEL_VIEW_LAYOUT }
        }.map { it.method })
        val ctor = controller.instructions()
        check(ctor[0].isLiteral(Opcode.CONST_16, 8) && ctor[1].call()?.name == "setVisibility" &&
            (ctor[1] as FiveRegisterInstruction).registerC == controller.parameterRegister(0) &&
            (ctor[1] as FiveRegisterInstruction).registerD == ctor[0].register()) {
            "${controller.label()} does not hide its host first"
        }

        // 3. Both LINE Ads views collapse before super.onAttachedToWindow.
        for (type in listOf(LAD_AD_VIEW, LYAD_AD_VIEW)) {
            val attach = (byType[type] ?: error("$type not found")).method("onAttachedToWindow", listOf()).instructions()
            val superCall = attach.indexOfFirst { it.opcode == Opcode.INVOKE_SUPER && it.call()?.name == "onAttachedToWindow" }
            check(attach[0].call()?.name == "getLayoutParams" &&
                attach.take(superCall).any { it.call()?.name == "setLayoutParams" } &&
                attach.take(superCall).any { it.call()?.name == "setVisibility" }) { "$type.onAttachedToWindow does not collapse the view" }
        }
    }

    private fun assertFixPush(withStrings: (Array<out String>) -> List<Indexed>) {
        val builder = single("FIS request builder", withStrings(arrayOf("X-Android-Cert")).filter {
            it.method.returnType == "Ljava/net/HttpURLConnection;" &&
                it.method.params() == listOf("Ljava/net/URL;", "Ljava/lang/String;")
        }.map { it.method })
        val insns = builder.instructions()
        val header = insns.indexOfFirst { it.string() == "X-Android-Cert" }
        val add = (header until insns.size).first { insns[it].call()?.name == "addRequestProperty" }
        val value = (insns[add] as FiveRegisterInstruction).registerE
        check(insns[add - 1].string() == LINE_ORIGINAL_CERT_SHA1 && insns[add - 1].register() == value) {
            "${builder.label()} does not send LINE's original certificate"
        }
    }

    private fun assertTabs(indexed: List<Indexed>) {
        // Found like the patch: the ()List method that reads HOME, CHAT, SQUARE and CALL in order.
        val anchors = listOf("HOME", "CHAT", "SQUARE", "CALL")
        val builder = single("main tab list builder", indexed.filter { m ->
            m.method.returnType == "Ljava/util/List;" && m.method.parameterTypes.isEmpty() && run {
                var next = 0
                m.insns.forEach { if (next < anchors.size && it.opcode == Opcode.SGET_OBJECT && it.field()?.name == anchors[next]) next++ }
                next == anchors.size
            }
        }.map { it.method })
        val reads = builder.instructions().filter { it.opcode == Opcode.SGET_OBJECT }.mapNotNull { it.field() }
        val tabEnum = reads.single { it.name == "SQUARE" }.definingClass
        val tabs = reads.filter { it.definingClass == tabEnum }.map { it.name }.toSet()
        for (hidden in listOf("NEWS", "NEWS_ROW", "COMMERCE", "COMMERCE_TW", "TIMELINE", "MINI", "WALLET")) {
            check(hidden !in tabs) { "${builder.label()} still adds the $hidden tab" }
        }
        check(tabs.containsAll(anchors)) { "${builder.label()} lost a tab it must keep: $tabs" }
    }

    private fun assertHeaderButtons(byType: Map<String, ClassDef>, withStrings: (Array<out String>) -> List<Indexed>): String {
        val builder = single("Chats tab header builder", withStrings(arrayOf("createEndButtons"))
            .filter { it.method.name == "<init>" }.map { it.method })
        val reads = builder.instructions().filter { it.opcode == Opcode.SGET_OBJECT }.mapNotNull { it.field() }
        val iconEnum = reads.single { it.name == "PLUS_MENU" }.definingClass
        check(byType.getValue(iconEnum).staticFields.map { it.name }.containsAll(listOf("OPEN_CHAT", "CALENDAR", "AI_FRIEND", "ALBUM"))) {
            "$iconEnum is not the header icon enum"
        }
        val icons = reads.filter { it.definingClass == iconEnum }.map { it.name }.toSet()
        check("OPEN_CHAT" !in icons && "CALENDAR" !in icons) { "${builder.label()} still adds $icons" }
        check(icons.containsAll(listOf("AI_FRIEND", "ALBUM", "PLUS_MENU"))) { "${builder.label()} lost a kept button: $icons" }
        return iconEnum
    }

    private fun assertServerFlags(indexed: List<Indexed>, byType: Map<String, ClassDef>) {
        for (key in FLAG_KEYS) {
            val sites = indexed.flatMap { m ->
                m.insns.indices.filter { m.insns[it].string() == key }.mapNotNull { m.insns.getOrNull(it + 1)?.call() }
                    .filter { it.returnType == "Z" && it.parameterTypes.isEmpty() }
            }
            check(sites.size == 1) { "expected one parse site for $key, found $sites" }
            val accessor = byType.getValue(sites.single().definingClass).methods.single {
                it.name == sites.single().name && it.returnType == "Z" && it.parameterTypes.isEmpty()
            }
            assertForcedBoolean(accessor, expected = false, label = "$key accessor ${accessor.label()}")
        }
    }

    private fun assertNewBadges(indexed: List<Indexed>, byType: Map<String, ClassDef>, iconEnum: String) {
        // 1. HeaderButton's (I, Z) dot setter forces its visibility argument to GONE first.
        val dot = byType.getValue(HEADER_BUTTON).methods.single { it.returnType == "V" && it.params() == listOf("I", "Z") }
        check(dot.instructions()[0].isLiteral(Opcode.CONST_16, 8) && dot.instructions()[0].register() == dot.parameterRegister(0)) {
            "${dot.label()} does not force GONE"
        }

        // 2. The "N" badge: the only outside read of a HeaderButton ImageView is shown with GONE.
        val nBadge = single("N badge renderer", indexed.filter { m ->
            m.classDef.type != HEADER_BUTTON && m.insns.any {
                it.opcode == Opcode.IGET_OBJECT && it.field()?.let { f -> f.definingClass == HEADER_BUTTON && f.type == "Landroid/widget/ImageView;" } == true
            }
        }.map { it.method })
        assertGoneAfter(nBadge) { it.opcode == Opcode.IGET_OBJECT && it.field()?.definingClass == HEADER_BUTTON && it.field()?.type == "Landroid/widget/ImageView;" }

        // 3-4. Settings rows read "new" as false.
        for ((type, name) in listOf(
            "Lcom/linecorp/line/settings/base/itemview/LineUserSettingTextItemView;" to "setNewBadgeVisible",
            "Lcom/linecorp/line/settings/base/itemview/LineUserSettingSwitchItemView;" to "setIsNewMarkVisible",
        )) {
            val setter = byType.getValue(type).method(name, listOf("Z"))
            check(setter.instructions()[0].isLiteral(Opcode.CONST_4, 0) && setter.instructions()[0].register() == setter.parameterRegister(0)) {
                "${setter.label()} does not force false"
            }
        }

        // 5. The Home service badge enum's isVisible accessor returns false.
        val serviceEnum = single("service badge enum", indexed.filter { m ->
            m.method.name == "<clinit>" && listOf("NEW", "UPDATE", "EVENT", "EXPIRED").all { c ->
                m.insns.any { it.opcode == Opcode.SPUT_OBJECT && it.field()?.name == c && it.field()?.definingClass == m.classDef.type }
            } && m.classDef.fields.any { it.name == "isVisible" }
        }.map { it.method })
        val isVisible = byType.getValue(serviceEnum.definingClass).methods.single { it.name == "isVisible" && it.returnType == "Z" }
        assertForcedBoolean(isVisible, expected = false, label = isVisible.label())

        // 6. Friend rows: the setVisibility after `badge == GREEN_DOT_BADGE` passes GONE on every path.
        val friendRow = single("friend row binder", indexed.filter { m ->
            m.insns.indices.any { i ->
                m.insns[i].opcode == Opcode.SGET_OBJECT && m.insns[i].field()?.name == "GREEN_DOT_BADGE" &&
                    m.insns.getOrNull(i + 1)?.opcode in setOf(Opcode.IF_NE, Opcode.IF_EQ)
            }
        }.map { it.method })
        assertGoneAfter(friendRow) {
            it.opcode == Opcode.SGET_OBJECT && it.field()?.name == "GREEN_DOT_BADGE"
        }

        // 7. The Chats tab green dot set adds no header icon.
        val greenDots = single("green dot set", indexed.filter { m ->
            m.method.name == "invokeSuspend" && m.classDef.annotations.any { a ->
                a.elements.any { it.name == "c" && (it.value as? StringEncodedValue)?.value == GREEN_DOT_SET_COROUTINE }
            }
        }.map { it.method })
        val added = greenDots.instructions().filter { it.opcode == Opcode.SGET_OBJECT && it.field()?.definingClass == iconEnum }
        check(added.isEmpty()) { "${greenDots.label()} still adds ${added.map { it.field()?.name }}" }
        check(greenDots.instructions().none { it.call()?.name == "add" }) { "${greenDots.label()} still adds to the set" }
    }

    // The last setVisibility after the last [anchor] whose comparison/branch picks the visibility is
    // preceded by `const/16 vD, 0x8`, and no branch jumps past that constant to the call.
    private fun assertGoneAfter(method: Method, anchor: (Instruction) -> Boolean) {
        val insns = method.instructions()
        val start = insns.indexOfLast(anchor)
        val call = (start until insns.size).first { insns[it].call()?.name == "setVisibility" }
        val visibility = (insns[call] as FiveRegisterInstruction).registerD
        check(insns[call - 1].isLiteral(Opcode.CONST_16, 8) && insns[call - 1].register() == visibility) {
            "${method.label()} does not force GONE before setVisibility at $call"
        }
        check(call !in branchTargets(insns)) { "${method.label()} has a branch past the GONE constant" }
    }

    private fun assertDisableVoom(byType: Map<String, ClassDef>, withStrings: (Array<out String>) -> List<Indexed>) {
        val handler = single("VOOM scheme handler", withStrings(arrayOf("hashtaglist", "deleteCache"))
            .filter { it.method.params().take(2) == listOf("Landroid/content/Context;", "Ljava/lang/String;") }.map { it.method })
        val insns = handler.instructions()
        val result = insns[0].field()
        check(insns[0].opcode == Opcode.SGET_OBJECT && result?.definingClass == handler.returnType &&
            insns[1].opcode == Opcode.RETURN_OBJECT && insns[1].register() == insns[0].register()) {
            "${handler.label()} does not return its not-handled result at once"
        }
        val errorToString = byType.getValue(result!!.type).method("toString", listOf()).instructions()
        check(errorToString.any { it.string()?.startsWith("Error(") == true }) { "${result.type} is not the Error result" }

        val onCreate = byType.getValue(LINE_VOOM_ACTIVITY).method("onCreate", listOf("Landroid/os/Bundle;")).instructions()
        check(onCreate[0].opcode == Opcode.INVOKE_SUPER && onCreate[1].call()?.name == "finish" &&
            onCreate[2].opcode == Opcode.RETURN_VOID) { "LineVoomActivity.onCreate does not finish after super" }
    }

    private fun assertKeepUnsent(byType: Map<String, ClassDef>, withStrings: (Array<out String>) -> List<Indexed>) {
        val unsend = single("unsend lambda", withStrings(arrayOf("reactions", "server_message_id=?", "multiple_image_message_mapping", "local_message_id = ?"))
            .filter { it.method.returnType == "Ljava/lang/Object;" && it.method.params() == listOf("Ljava/lang/Object;") }.map { it.method })
        val insns = unsend.instructions()
        val insert = insns.indexOfFirst { it.call()?.let { c -> c.definingClass == KEEP_UNSENT_MESSAGES && c.name == "insertPlaceholder" } == true }
        check(insert > 2) { "${unsend.label()} does not call insertPlaceholder" }
        val guard = (insns[insert] as FiveRegisterInstruction).registerC
        check(insns[insert - 2].opcode == Opcode.IF_NEZ && insns[insert - 2].register() == guard &&
            insns[insert - 1].opcode == Opcode.IGET_OBJECT && insns[insert - 1].field()?.type == "Landroid/database/sqlite/SQLiteDatabase;" &&
            (insns[insert - 1] as TwoRegisterInstruction).registerA == guard &&
            insns[insert + 1].isLiteral(Opcode.CONST_16, 1) && insns[insert + 1].register() == guard &&
            insns[insert + 2].opcode == Opcode.IF_EQZ && insns[insert + 2].register() == guard &&
            insns[insert - 3].opcode == Opcode.MOVE_RESULT && insns[insert - 4].call()?.returnType == "Z") {
            "${unsend.label()} does not force the already-unsent guard around the placeholder insert"
        }
        val extension = byType[KEEP_UNSENT_MESSAGES] ?: error("$KEEP_UNSENT_MESSAGES was not merged")
        extension.method("insertPlaceholder", listOf("Landroid/database/sqlite/SQLiteDatabase;", "J"))
    }

    private fun assertKeepUnread(
        indexed: List<Indexed>,
        byType: Map<String, ClassDef>,
        withStrings: (Array<out String>) -> List<Indexed>,
    ) {
        // The client method that sends the receipt, from the sendChatChecked lambda's EnclosingMethod.
        val sendLambda = single("sendChatChecked lambda", withStrings(arrayOf("sendChatChecked")).map { it.method })
        val sendMethod = (byType.getValue(sendLambda.definingClass).annotations.single { it.type == "Ldalvik/annotation/EnclosingMethod;" }
            .elements.single().value as MethodEncodedValue).value
        val worker = single("read worker", indexed.filter { m ->
            m.method.returnType == "V" && m.method.params() == listOf("J", "Ljava/lang/String;", "Z") &&
                m.insns.any { it.call()?.let { c -> c.definingClass == TALK_SERVICE_CLIENT && c.name == sendMethod.name } == true }
        }.map { it.method })
        val wrapper = byType.getValue(worker.definingClass).methods.single {
            it.returnType == "V" && it.params() == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }
        assertReturnsEarlyVoid(wrapper, label = wrapper.label())
    }

    private fun assertReadWithoutReceipt(
        indexed: List<Indexed>,
        byType: Map<String, ClassDef>,
        withStrings: (Array<out String>) -> List<Indexed>,
    ) {
        fun Instruction.isHook(name: String) =
            call()?.let { it.definingClass == READ_WITHOUT_RECEIPT && it.name == name } == true
        fun caller(name: String) = single("$name caller", indexed.filter { m -> m.insns.any { it.isHook(name) } }.map { it.method })
        fun Instruction.rangeStart() = (this as RegisterRangeInstruction).startRegister

        // 1. ChatHistoryActivity claims the session right after super.onCreate(savedState).
        val onCreate = caller("onChatCreated")
        val create = onCreate.instructions()
        check(onCreate.name == "onCreate" && create[0].opcode in setOf(Opcode.INVOKE_SUPER, Opcode.INVOKE_SUPER_RANGE) &&
            create[0].call()?.name == "onCreate" && create[1].isHook("onChatCreated") &&
            create[1].rangeStart() == onCreate.implementation!!.registerCount - 2) {
            "${onCreate.label()} does not pass (this, savedState) to onChatCreated after super.onCreate"
        }

        // 2. The read worker and sendChatChecked return first when the chat is read without receipt.
        val sendLambda = single("sendChatChecked lambda", withStrings(arrayOf("sendChatChecked")).map { it.method })
        val sendMethod = (byType.getValue(sendLambda.definingClass).annotations.single { it.type == "Ldalvik/annotation/EnclosingMethod;" }
            .elements.single().value as MethodEncodedValue).value
        val blockers = indexed.filter { m -> m.insns.any { it.isHook("shouldBlockRead") } }.map { it.method }
        check(blockers.size == 2 && blockers.any { it.params() == listOf("J", "Ljava/lang/String;", "Z") } &&
            blockers.any {
                it.definingClass == sendMethod.definingClass && it.name == sendMethod.name &&
                    it.params() == sendMethod.parameterTypes.map(CharSequence::toString)
            }) {
            "shouldBlockRead is called by ${blockers.map { it.label() }}, expected the read worker and " +
                "${sendMethod.definingClass}->${sendMethod.name}"
        }
        for (blocker in blockers) {
            val insns = blocker.instructions()
            check(insns[0].isHook("shouldBlockRead") && insns[0].rangeStart() == blocker.parameterRegister(1) &&
                insns[1].opcode == Opcode.MOVE_RESULT && insns[2].opcode == Opcode.IF_EQZ &&
                insns[2].register() == insns[1].register() && insns[3].opcode == Opcode.RETURN_VOID &&
                4 in branchTargets(insns)) {
                "${blocker.label()} does not return early for a chat read without receipt"
            }
        }

        // 3. The row class and its action type; the extension's action is re-parented onto it.
        val rowToString = single("ChatRowMenuItem.toString", withStrings(arrayOf("ChatRowMenuItem(labelRes=")).map { it.method })
        val rowClass = byType.getValue(rowToString.definingClass)
        val actionType = rowClass.fields.single { it.type != "I" }.type
        val action = byType[READ_WITHOUT_RECEIPT_ACTION] ?: error("$READ_WITHOUT_RECEIPT_ACTION was not merged")
        check(action.superclass == actionType) { "$READ_WITHOUT_RECEIPT_ACTION extends ${action.superclass}, not $actionType" }
        val actionInit = action.methods.single { it.name == "<init>" }.instructions()
        check(actionInit.any { it.opcode == Opcode.INVOKE_DIRECT && it.call()?.let { c -> c.definingClass == actionType && c.name == "<init>" } == true } &&
            actionInit.none { it.call()?.let { c -> c.definingClass == "Ljava/lang/Object;" && c.name == "<init>" } == true }) {
            "$READ_WITHOUT_RECEIPT_ACTION's constructor does not call $actionType.<init>"
        }

        // 4. The generated menuRows helper builds the stock rows, then adds the injected one. Only the
        //    bottom-sheet menu (in the builder's own class) uses it; the shared builder is unchanged,
        //    so the chat preview screen's menu never shows the row.
        val menuRows = caller("addMenuRow")
        val rows = menuRows.instructions()
        val builderCall = rows[0].call() ?: error("${menuRows.label()} does not start with the row builder")
        val add = rows.indexOfFirst { it.isHook("addMenuRow") }
        check(menuRows.definingClass == READ_WITHOUT_RECEIPT && menuRows.name == "menuRows" &&
            builderCall.returnType == "Ljava/util/ArrayList;" && rows[1].opcode == Opcode.MOVE_RESULT_OBJECT &&
            (rows[add] as FiveRegisterInstruction).registerC == rows[1].register() &&
            rows[add + 1].opcode == Opcode.RETURN_OBJECT && rows[add + 1].register() == rows[1].register() &&
            rows.take(add).any { it.isHook("menuRowLabel") } &&
            rows.any { insn ->
                insn.opcode == Opcode.NEW_INSTANCE &&
                    ((insn as ReferenceInstruction).reference as? TypeReference)?.type == READ_WITHOUT_RECEIPT_ACTION
            } &&
            rows.any { it.call()?.let { c -> c.definingClass == rowClass.type && c.name == "<init>" } == true }) {
            "${menuRows.label()} does not append the row to the builder's rows"
        }
        val builderClass = byType.getValue(builderCall.definingClass)
        val sheet = single("bottom-sheet menuRows caller", indexed.filter { m -> m.insns.any { it.isHook("menuRows") } }.map { it.method })
        check(sheet.definingClass == builderClass.type &&
            builderClass.methods.none { m -> m.implementation != null && m.instructions().any { it.call() == builderCall } } &&
            builderClass.methods.single { it.name == builderCall.name && it.params() == builderCall.parameterTypes.map(CharSequence::toString) }
                .instructions().none { it.call()?.definingClass == READ_WITHOUT_RECEIPT }) {
            "${sheet.label()} is not the only menu that gets the row, or the shared builder was changed"
        }

        // 5. The generated labelText helper returns the extension's text for the sentinel, else
        //    stringResource's; every row label lambda reads the label field straight into it.
        val labelText = caller("menuLabel")
        val helper = labelText.instructions()
        val stock = helper.firstOrNull { it.opcode == Opcode.INVOKE_STATIC && !it.isHook("menuLabel") }?.call()
        check(labelText.definingClass == READ_WITHOUT_RECEIPT && labelText.name == "labelText" &&
            helper[0].isHook("menuLabel") && helper[2].opcode == Opcode.IF_EQZ && helper[3].opcode == Opcode.RETURN_OBJECT &&
            stock?.returnType == "Ljava/lang/String;" && stock.parameterTypes.map(CharSequence::toString) == labelText.params()) {
            "${labelText.label()} does not fall back to stringResource"
        }
        val lambdas = indexed.filter { m -> m.insns.any { it.isHook("labelText") } }
        check(lambdas.isNotEmpty() && lambdas.all { m ->
            val call = m.insns.indexOfFirst { it.isHook("labelText") }
            m.insns[call - 1].opcode == Opcode.IGET && m.insns[call - 1].field()?.definingClass == rowClass.type &&
                (m.insns[call] as FiveRegisterInstruction).registerD == (m.insns[call - 1] as TwoRegisterInstruction).registerA
        } && indexed.none { m -> m.insns.any { it.call() == stock } && m.insns.any { it.field()?.definingClass == rowClass.type && it.opcode == Opcode.IGET } }) {
            "row label lambdas ${lambdas.map { it.method.label() }} do not all read their label through labelText"
        }

        // 6. The action dispatcher hands the row's action to the extension right after closing the menu.
        val dispatcher = caller("onMenuAction")
        val dispatch = dispatcher.instructions()
        val hand = dispatch.indexOfFirst { it.isHook("onMenuAction") }
        check(dispatch[0].opcode == Opcode.CHECK_CAST &&
            ((dispatch[0] as ReferenceInstruction).reference as? TypeReference)?.type == actionType &&
            (dispatch[hand] as FiveRegisterInstruction).registerC == dispatch[0].register() &&
            dispatch[hand - 1].opcode == Opcode.INVOKE_INTERFACE && dispatch[hand - 1].call()?.name == "invoke" &&
            dispatch[hand + 2].opcode == Opcode.IF_EQZ && dispatch[hand + 3].opcode == Opcode.SGET_OBJECT &&
            dispatch[hand + 4].opcode == Opcode.RETURN_OBJECT) {
            "${dispatcher.label()} does not hand the action to onMenuAction after closing the menu"
        }

        // 7. Chat launches pass their intent to onChatLaunch first.
        val launcher = caller("onChatLaunch")
        val launch = launcher.instructions()
        check(launcher.params() == listOf("Landroid/content/Intent;", "Z") && launch[0].isHook("onChatLaunch") &&
            launch[0].rangeStart() == launcher.parameterRegister(0) && launch.any { it.string() == "shouldLaunchMainActivity" }) {
            "${launcher.label()} does not pass its intent to onChatLaunch first"
        }
    }

    private fun assertExternalBrowser(byType: Map<String, ClassDef>, withStrings: (Array<out String>) -> List<Indexed>) {
        val builder = single("OpenUri intent builder", withStrings(arrayOf("CUSTOM_TABS_MODE", "SHOULD_USE_IAB", "IS_NEW_TAB_IN_EXT"))
            .filter { it.method.returnType == "Landroid/content/Intent;" }.map { it.method })
        val modeIndex = builder.params().indexOfFirst { type -> byType[type]?.staticFields?.any { it.name == "EXTERNAL_WITHOUT_CUSTOMTABS" } == true }
        val first = builder.instructions()[0]
        check(first.opcode == Opcode.SGET_OBJECT && first.field()?.name == "EXTERNAL_WITHOUT_CUSTOMTABS" &&
            first.register() == builder.parameterRegister(modeIndex)) {
            "${builder.label()} does not force EXTERNAL_WITHOUT_CUSTOMTABS into its mode parameter"
        }
    }
}
