import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OffsetInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.RegisterRangeInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.iface.value.MethodEncodedValue
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "com.kakao.talk"
private const val NATIVE_AD_LOADER = "Lcom/kakao/adfit/ads/media/NativeAdLoader;"
private const val FOCUS_AD_LOADER = "Lcom/kakao/adfit/ads/focus/FocusAdLoader;"
private const val FEED_AD_LAYOUT = "Lcom/kakao/adfit/ads/feed/FeedAdLayout;"
private const val BIZ_BOARD_FRAME_LAYOUT = "Lcom/kakao/talk/widget/ad/BizBoardFrameLayout;"
private const val NATIVE_AD_BINDER = "Lcom/kakao/adfit/ads/media/NativeAdBinder;"
private const val ALIMTALK_COMPANION = "Lcom/kakao/talk/bubble/alimtalk/AlimtalkType\$Companion;"
private const val TAB_INFO = "Lcom/kakao/talk/activity/main/MainTabPagerAdapter\$TabInfo;"
private const val RECYCLER_VIEW = "Landroidx/recyclerview/widget/RecyclerView;"
private const val CRASHLYTICS = "Lcom/google/firebase/crashlytics/FirebaseCrashlytics;"
private const val STRIP_IMAGE_METADATA = "Lapp/template/extension/kakaotalk/StripImageMetadata;"
private const val CHAT_ROOM_API_HELPER = "com.kakao.talk.chatroom.ChatRoomApiHelper\$Companion"

class KakaoTalkSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Method.params() = parameterTypes.map(CharSequence::toString)

    private fun Method.strings() = instructions().mapNotNull { it.stringOrNull() }

    private fun Method.label() = "$definingClass->$name(${params().joinToString("")})$returnType"

    private fun Instruction.call(): MethodReference? = (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun Instruction.field(): FieldReference? = (this as? ReferenceInstruction)?.reference as? FieldReference

    private fun Instruction.type(): String? = ((this as? ReferenceInstruction)?.reference as? TypeReference)?.type

    private fun Instruction.isLiteral(opcode: Opcode, value: Int) =
        this.opcode == opcode && (this as NarrowLiteralInstruction).narrowLiteral == value

    private fun Method.calls(owner: String, name: String) = instructions().any { it.call()?.let { c -> c.definingClass == owner && c.name == name } == true }

    private fun List<ClassDef>.fromSource(source: String) = filter { it.sourceFile == source }

    private fun List<ClassDef>.singleMethod(label: String, predicate: (ClassDef, Method) -> Boolean): Method {
        val found = flatMap { cls -> cls.methods.filter { predicate(cls, it) } }
        check(found.size == 1) { "expected one $label, found ${found.map { it.label() }}" }
        return found.single()
    }

    private fun assertReturnsObject(method: Method, owner: String, field: String) {
        val insns = method.instructions()
        val value = insns.getOrNull(0)?.field()
        check(insns.getOrNull(0)?.opcode == Opcode.SGET_OBJECT && value?.definingClass == owner && value.name == field) {
            "${method.label()} does not load $owner.$field first; got ${insns.getOrNull(0)?.opcode} $value"
        }
        check(insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT &&
            (insns[1] as OneRegisterInstruction).registerA == (insns[0] as OneRegisterInstruction).registerA) {
            "${method.label()} does not return $field at once"
        }
    }

    private fun assertReturnsNull(method: Method) {
        val insns = method.instructions()
        check(insns.getOrNull(0)?.isLiteral(Opcode.CONST_4, 0) == true && insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT) {
            "${method.label()} does not return null at once; got ${insns.getOrNull(0)?.opcode}"
        }
    }

    // Index of each branch's target, from the dex code-unit offsets.
    private fun branchTargets(insns: List<Instruction>): Map<Int, Int> {
        val offsets = insns.runningFold(0) { offset, insn -> offset + insn.codeUnits }
        val indexAt = offsets.withIndex().associate { (index, offset) -> offset to index }
        return insns.withIndex().filter { (_, it) -> it is OffsetInstruction }.associate { (index, insn) ->
            index to (indexAt[offsets[index] + (insn as OffsetInstruction).codeOffset] ?: error("bad branch target"))
        }
    }

    // The method enclosing the coroutine class with @DebugMetadata c = [className] (and m = [methodName]),
    // found the same way the patches do.
    private fun enclosingCoroutine(classes: List<ClassDef>, byType: Map<String, ClassDef>, className: String, methodName: String? = null): Method {
        val coroutine = classes.single { cls ->
            cls.annotations.any { a ->
                fun value(name: String) = (a.elements.firstOrNull { it.name == name }?.value as? StringEncodedValue)?.value
                a.type == "Lkotlin/coroutines/jvm/internal/DebugMetadata;" && value("c") == className &&
                    (methodName == null || value("m") == methodName)
            }
        }
        val ref = (coroutine.annotations.single { it.type == "Ldalvik/annotation/EnclosingMethod;" }
            .elements.single().value as MethodEncodedValue).value
        return (byType[ref.definingClass] ?: error("${ref.definingClass} not found")).methods.single {
            it.name == ref.name && it.params() == ref.parameterTypes.map(CharSequence::toString)
        }
    }

    @Test
    fun `KakaoTalk patches remove ads and telemetry, unread limits, mobile-only covers, tabs, ghost modes and photo metadata`() {
        val root = repoRoot()
        val apk = File(root, "apks/kakaotalk/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "26.8.3",
            patchNames = setOf(
                "Disable ads",
                "Show messages restricted to mobile",
                "Remove 300+ unread limit",
                "Remove 99+ unread limit",
                "Remove shop tab",
                "Disable community tab",
                "Disable friend feed tab",
                "Disable telemetry",
                "Ghost typing",
                "Ghost read",
                "Strip image metadata",
            ),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )
        val byType = classes.associateBy { it.type }

        assertDisableAds(classes, byType)
        assertMobileRestrictedMessages(classes, byType)
        assertUnreadLimits(classes)
        assertShopTab(classes)
        assertCommunityTab(classes)
        assertFriendFeedTab(classes, byType)
        assertDisableTelemetry(classes, byType)
        assertGhostModes(classes, byType)
        assertStripImageMetadata(classes, byType)
    }

    private fun assertDisableAds(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        fun require(type: String) = byType[type] ?: error("$type not found")

        // 1. Both NativeAdLoader.load overloads report "started" at once.
        val loader = require(NATIVE_AD_LOADER)
        val loads = loader.methods.filter { it.name == "load" && it.returnType == "Z" }
        check(loads.size == 2) { "expected two NativeAdLoader.load overloads, found ${loads.map { it.label() }}" }
        loads.forEach { assertForcedBoolean(it, expected = true, label = it.label()) }

        // 2. The FocusAdLoader implementation's loadAd.
        val focus = classes.singleMethod("FocusAdLoader.loadAd implementation") { cls, m ->
            cls.superclass == FOCUS_AD_LOADER && m.name == "loadAd" && m.implementation != null
        }
        assertForcedBoolean(focus, expected = true, label = focus.label())

        // 3. OpenChat room BizBoard controller load.
        val controller = classes.fromSource("OpenLinkChatAdController.kt").singleMethod("OpenChat BizBoard load") { _, m ->
            m.returnType == "V" && m.parameterTypes.isEmpty() && m.calls("Lcom/kakao/adfit/ads/bizboard/BizBoardAdView;", "loadAd")
        }
        assertReturnsEarlyVoid(controller, label = controller.label())

        // 4. Chat list ad checks.
        val chatList = classes.fromSource("ChatListAdHelper.kt")
        val enabled = chatList.singleMethod("chat list ad check") { _, m ->
            m.returnType == "Z" && m.params() == listOf("Landroid/content/Context;")
        }
        assertForcedBoolean(enabled, expected = false, label = enabled.label())
        val global = chatList.singleMethod("chat list global ad check") { _, m ->
            m.returnType == "Ljava/lang/Object;" &&
                m.params() == listOf("Landroid/content/Context;", "Z", "Lkotlin/coroutines/Continuation;")
        }
        assertReturnsObject(global, "Ljava/lang/Boolean;", "FALSE")

        // 5. OpenChat feed ad, KakaoPay banner and short-form ad page.
        val openChatFeed = classes.fromSource("OpenChatTabFeedAdViewHolder.kt")
        val feedLoad = openChatFeed.singleMethod("OpenChat feed ad load") { _, m ->
            m.returnType == "V" && m.calls("Lcom/kakao/adfit/ads/feed/FeedAdLoader;", "loadAd")
        }
        val feedRender = openChatFeed.singleMethod("OpenChat feed ad render") { _, m ->
            m.returnType == "V" && m.parameterTypes.size == 3 && m.calls(FEED_AD_LAYOUT, "setFeedAd")
        }
        val payLoad = classes.fromSource("PayAdPlatformBannerState.kt").singleMethod("Pay banner refresh") { _, m ->
            m.returnType == "V" && m.params() == listOf("Z") && m.instructions().any { it.opcode == Opcode.XOR_INT_LIT8 }
        }
        val payRender = classes.fromSource("PayAdPlatformAdFit.kt").singleMethod("Pay banner composable") { _, m ->
            m.returnType == "V" && AccessFlags.STATIC.isSet(m.accessFlags) && m.parameterTypes.size == 10 &&
                m.params()[1] == NATIVE_AD_BINDER
        }
        val sft = classes.fromSource("SftAdFragment.kt")
        val sftLoad = sft.singleMethod("short-form ad load") { _, m ->
            m.returnType == "V" && m.parameterTypes.isEmpty() && AccessFlags.PRIVATE.isSet(m.accessFlags) &&
                "feedAdLoader" in m.strings() && m.calls("Lcom/kakao/adfit/ads/feed/FeedAdLoader;", "isLoading")
        }
        val sftRender = sft.singleMethod("short-form ad render") { _, m ->
            m.returnType == "V" && m.parameterTypes.size == 2 && "feedAdLayout" in m.strings() && m.calls(FEED_AD_LAYOUT, "setFeedAd")
        }
        for (method in listOf(feedLoad, feedRender, payLoad, payRender, sftLoad, sftRender)) {
            assertReturnsEarlyVoid(method, label = method.label())
        }

        // 6. BizBoard frame: setMeasuredDimension(0, 0), setVisibility(GONE), return.
        val bizBoard = require(BIZ_BOARD_FRAME_LAYOUT).method("onMeasure", listOf("I", "I")).instructions()
        check(bizBoard.getOrNull(2)?.call()?.name == "setMeasuredDimension" &&
            bizBoard.getOrNull(3)?.isLiteral(Opcode.CONST_16, 8) == true &&
            bizBoard.getOrNull(4)?.call()?.name == "setVisibility" &&
            bizBoard.getOrNull(5)?.opcode == Opcode.RETURN_VOID) { "BizBoardFrameLayout.onMeasure is not neutered" }

        // 7. FeedAdLayout: GONE at the end of the constructor, setFeedAd returns, onMeasure is 0x0.
        val feedLayout = require(FEED_AD_LAYOUT)
        val ctor = feedLayout.method("<init>", listOf("Landroid/content/Context;", "Landroid/util/AttributeSet;", "I")).instructions()
        val hide = ctor.indexOfLast { it.call()?.name == "setVisibility" }
        check(hide > 0 && ctor[hide - 1].isLiteral(Opcode.CONST_16, 8) &&
            ctor.drop(hide).any { it.call()?.name == "requestLayout" } && ctor.last().opcode == Opcode.RETURN_VOID) {
            "FeedAdLayout constructor does not hide the view at its end"
        }
        assertReturnsEarlyVoid(feedLayout.method("setFeedAd", listOf("Lcom/kakao/adfit/ads/feed/FeedAd;")), label = "FeedAdLayout.setFeedAd")
        val onMeasure = feedLayout.method("onMeasure", listOf("I", "I")).instructions()
        check(onMeasure.getOrNull(2)?.call()?.name == "setMeasuredDimension" && onMeasure.getOrNull(3)?.opcode == Opcode.RETURN_VOID) {
            "FeedAdLayout.onMeasure does not measure 0x0"
        }

        // 8. Friends tab birthday BizBoard binder and global-region ad model.
        val birthday = classes.fromSource("FriendTabBirthdayFriendsBizBoardAdViewModel.kt").singleMethod("birthday BizBoard binder") { _, m ->
            m.returnType == "V" && m.params() == listOf(NATIVE_AD_BINDER)
        }
        assertReturnsEarlyVoid(birthday, label = birthday.label())
        val globalAd = classes.fromSource("FriendTabGlobalAdViewModel.kt").singleMethod("friend tab global ad model") { _, m ->
            m.name == "invokeSuspend" && m.instructions().any { it.opcode == Opcode.NEW_INSTANCE }
        }
        assertReturnsNull(globalAd)
    }

    private fun assertMobileRestrictedMessages(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // 1. AlimTalk and Leverage cover state getters return NONE.
        val alimtalk = (byType[ALIMTALK_COMPANION] ?: error("$ALIMTALK_COMPANION not found")).methods.single {
            it.params() == listOf("Z") && it.returnType.startsWith("L")
        }
        assertReturnsObject(alimtalk, alimtalk.returnType, "NONE")
        val leverage = classes.fromSource("LeverageType.kt").singleMethod("Leverage cover state") { _, m ->
            m.params() == listOf("Z", "Z") && m.returnType == alimtalk.returnType
        }
        assertReturnsObject(leverage, alimtalk.returnType, "NONE")

        // 2. Universal: the check before "new MobileOnlyCover" is forced false; the lock check is not.
        fun coverType(text: String) = classes.single { cls ->
            cls.methods.any { it.name == "toString" && it.strings().any { s -> s.startsWith(text) } }
        }.type
        val validator = classes.fromSource("UniversalDataValidator.kt").singleMethod("Universal cover state") { _, m ->
            m.instructions().any { it.opcode == Opcode.NEW_INSTANCE && it.type() == coverType("MobileOnlyCover(data=") }
        }
        fun checkBefore(cover: String): Method {
            val insns = validator.instructions()
            val index = insns.indexOfFirst { it.opcode == Opcode.NEW_INSTANCE && it.type() == coverType(cover) }
            val ref = insns[index - 3].call() ?: error("no check before $cover")
            return (byType[ref.definingClass] ?: error("${ref.definingClass} not found")).methods.single {
                it.name == ref.name && it.params() == ref.parameterTypes.map(CharSequence::toString)
            }
        }
        val mobileOnly = checkBefore("MobileOnlyCover(data=")
        assertForcedBoolean(mobileOnly, expected = false, label = mobileOnly.label())
        val lock = checkBefore("LockCover(data=")
        check(lock.instructions().first().opcode != Opcode.CONST_4) { "${lock.label()} (lock cover check) was changed" }
    }

    private fun assertUnreadLimits(classes: List<ClassDef>) {
        // 300+: the "count < 300" branch is now a goto in both list items.
        for (source in listOf("BaseChatRoomItemViewHolder.kt", "OpenLinkChatsItem.kt")) {
            val item = classes.fromSource(source).singleMethod("$source 300+ badge") { _, m -> "300+" in m.strings() }
            val insns = item.instructions()
            val limit = insns.indexOfFirst { it.isLiteral(Opcode.CONST_16, 300) }
            check(limit >= 0 && insns[limit + 1].opcode == Opcode.GOTO) {
                "${item.label()}: the 300 check is not a goto; got ${insns.getOrNull(limit + 1)?.opcode}"
            }
            check(insns.none { it.opcode == Opcode.IF_LT }) { "${item.label()} still has an if-lt" }
        }
        // The synchronized unread count getters no longer call Math.min(count, 300).
        val counters = classes.fromSource("UnreadCountInfo.kt").flatMap { it.methods }.filter {
            it.returnType == "I" && it.parameterTypes.isEmpty() && AccessFlags.DECLARED_SYNCHRONIZED.isSet(it.accessFlags)
        }
        check(counters.size >= 2) { "expected the unread count getters in UnreadCountInfo.kt, found ${counters.map { it.label() }}" }
        check(counters.none { m -> m.calls("Ljava/lang/Math;", "min") || m.instructions().any { it.isLiteral(Opcode.CONST_16, 300) } }) {
            "an UnreadCountInfo getter still clamps to 300"
        }

        // 99: the chat log item returns the count, the watermark cache keeps it.
        fun clampOf(source: String, predicate: (Method) -> Boolean): Pair<Method, Int> {
            val method = classes.fromSource(source).singleMethod("$source 99 clamp") { _, m ->
                predicate(m) && m.instructions().any { it.isLiteral(Opcode.CONST_16, 99) }
            }
            return method to method.instructions().indexOfFirst { it.isLiteral(Opcode.CONST_16, 99) }
        }
        val (item, i) = clampOf("ChatLogRecyclerItem.kt") { it.returnType == "I" }
        val itemInsns = item.instructions()
        val itemBranch = itemInsns[i + 1] as TwoRegisterInstruction
        check(itemInsns[i + 1].opcode == Opcode.IF_LE && itemInsns[i + 2].opcode == Opcode.RETURN &&
            (itemInsns[i + 2] as OneRegisterInstruction).registerA == itemBranch.registerA) {
            "${item.label()} does not return the real count after the 99 check"
        }
        val (cache, j) = clampOf("WatermarksManager.kt") { it.returnType == "I" && it.parameterTypes.size == 3 }
        val cacheInsns = cache.instructions()
        val cacheBranch = cacheInsns[j + 1] as TwoRegisterInstruction
        val next = cacheInsns[j + 2]
        check(cacheInsns[j + 1].opcode == Opcode.IF_LE &&
            !(next.opcode == Opcode.MOVE && (next as TwoRegisterInstruction).registerB == cacheBranch.registerB)) {
            "${cache.label()} still assigns 99 to the count"
        }
    }

    private fun assertShopTab(classes: List<ClassDef>) {
        val tabs = classes.fromSource("MainTabConfig.kt").filter { '$' !in it.type }.singleMethod("bottom tab list") { _, m ->
            m.returnType == "Ljava/util/List;" && m.calls("Ljava/util/Map;", "put")
        }
        val insns = tabs.instructions()
        val shop = insns.indexOfFirst { it.opcode == Opcode.SGET_OBJECT && it.field()?.name == "SHOPPING_TAB" && it.field()?.definingClass == TAB_INFO }
        check(shop > 0) { "${tabs.label()} does not compare with SHOPPING_TAB" }
        val tab = (insns[shop - 1] as OneRegisterInstruction).registerA
        check(insns[shop - 1].opcode == Opcode.MOVE_RESULT_OBJECT && insns[shop - 2].call()?.returnType == TAB_INFO) {
            "SHOPPING_TAB check is not right after the slot-3 resolve"
        }
        check(insns[shop + 1].opcode == Opcode.IF_NE && (insns[shop + 1] as TwoRegisterInstruction).registerA == tab &&
            insns[shop + 2].isLiteral(Opcode.CONST_4, 0) && (insns[shop + 2] as OneRegisterInstruction).registerA == tab) {
            "${tabs.label()} does not null out SHOPPING_TAB"
        }
        check(insns[shop + 3].isLiteral(Opcode.CONST_16, 3) && insns[shop + 4].call()?.name == "valueOf" &&
            insns[shop + 6].call()?.name == "put") { "${tabs.label()} does not rebuild the slot-3 key before put" }
        check(insns.count { it.call()?.name == "put" } == 5) { "${tabs.label()} lost a tab slot" }
    }

    private fun assertCommunityTab(classes: List<ClassDef>) {
        val fragment = classes.fromSource("OpenChatTabFragment.kt").filter { '$' !in it.type }
        val setup = fragment.singleMethod("OpenChat adapter setup") { _, m ->
            m.parameterTypes.isEmpty() && m.returnType == "V" && m.calls(RECYCLER_VIEW, "setAdapter") &&
                m.calls(RECYCLER_VIEW, "addOnScrollListener")
        }
        val insns = setup.instructions()
        val arrays = insns.withIndex().filter { (_, it) -> it.opcode == Opcode.NEW_ARRAY && it.type() == "[Landroidx/recyclerview/widget/RecyclerView\$h;" }
        check(arrays.size == 1 && insns[arrays.single().index - 1].isLiteral(Opcode.CONST_16, 1)) {
            "${setup.label()} does not build a one-adapter ConcatAdapter (arrays: ${arrays.size})"
        }
        check(insns.none { ins -> ins.call()?.let { it.definingClass == setup.definingClass && it.returnType == "Z" && it.parameterTypes.isEmpty() } == true }) {
            "${setup.label()} still reads the community flag"
        }
    }

    private fun assertFriendFeedTab(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // 1. TabConfig's constructor clears isFriendFeedTabEnabled before returning.
        val config = classes.single { cls -> cls.methods.any { m -> m.name == "toString" && m.strings().any { it.startsWith("TabConfig(isFriendFeedTabEnabled=") } } }
        val ctor = config.methods.single { it.name == "<init>" }.instructions()
        val feedField = ctor.first { it.opcode == Opcode.IPUT_BOOLEAN }.field()
        val n = ctor.size
        check(ctor[n - 3].isLiteral(Opcode.CONST_4, 0) && ctor[n - 2].opcode == Opcode.IPUT_BOOLEAN &&
            ctor[n - 2].field() == feedField && ctor[n - 1].opcode == Opcode.RETURN_VOID) {
            "TabConfig constructor does not clear $feedField at its end"
        }

        // 2. The FriendTab gate and its twin return false.
        val determine = classes.fromSource("FriendSettingsViewModel.kt").singleMethod("friend settings feed/list choice") { _, m ->
            m.name == "invokeSuspend" && m.instructions().any { it.call()?.let { c -> c.name == "<init>" && c.parameterTypes.size == 3 && c.parameterTypes.all { p -> p == "Z" } } == true }
        }
        val gate = determine.instructions().mapNotNull { it.call() }.single { it.parameterTypes.isEmpty() && it.returnType == "Z" }
        val gates = (byType[gate.definingClass] ?: error("${gate.definingClass} not found")).methods.filter {
            it.parameterTypes.isEmpty() && it.returnType == "Z"
        }
        check(gates.size == 2) { "expected the friend feed gate and its twin, found ${gates.map { it.label() }}" }
        gates.forEach { assertForcedBoolean(it, expected = false, label = it.label()) }
    }

    private fun assertDisableTelemetry(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // 1. Tiara enqueue and flush.
        val tiara = classes.fromSource("TiaraTracker.java")
        val enqueue = tiara.singleMethod("Tiara enqueue") { _, m ->
            m.params() == listOf("Lcom/kakao/tiara/data/TiaraLog;") && "log queue overflow, queue size: " in m.strings()
        }
        val flush = tiara.singleMethod("Tiara flush") { _, m ->
            m.parameterTypes.isEmpty() && "log queue is empty." in m.strings()
        }
        // 2. SentryAndroid.init.
        val sentry = classes.singleMethod("SentryAndroid.init") { _, m ->
            AccessFlags.STATIC.isSet(m.accessFlags) && "Fatal error during SentryAndroid.init(...)" in m.strings()
        }
        for (method in listOf(enqueue, flush, sentry)) assertReturnsEarlyVoid(method, label = method.label())

        // 3. Crashlytics.
        val crashlytics = byType[CRASHLYTICS] ?: error("$CRASHLYTICS not found")
        for ((name, params) in listOf(
            "sendUnsentReports" to emptyList(),
            "recordException" to listOf("Ljava/lang/Throwable;"),
            "log" to listOf("Ljava/lang/String;"),
            "setUserId" to listOf("Ljava/lang/String;"),
            "setCustomKey" to listOf("Ljava/lang/String;", "Ljava/lang/String;"),
        )) {
            assertReturnsEarlyVoid(crashlytics.method(name, params), label = "FirebaseCrashlytics.$name")
        }

        // 4. Flag resolver: USE_S2_EVENTS / USE_TALK_SHARE_LOG -> FALSE, else the original code.
        val available = classes.fromSource("Available2.kt")
        val flags = available.single { cls -> cls.fields.any { it.name == "USE_S2_EVENTS" } }
        val resolver = available.singleMethod("feature flag resolver") { _, m ->
            m.implementation != null && m.params() == listOf(flags.type, "Lkotlin/coroutines/Continuation;")
        }
        val insns = resolver.instructions()
        check(insns[0].field()?.name == "USE_S2_EVENTS" && insns[1].opcode == Opcode.IF_EQ &&
            insns[2].field()?.name == "USE_TALK_SHARE_LOG" && insns[3].opcode == Opcode.IF_NE) {
            "${resolver.label()} does not test USE_S2_EVENTS / USE_TALK_SHARE_LOG first"
        }
        val targets = branchTargets(insns)
        check(targets[1] == 4 && targets[3] == 6) { "${resolver.label()} flag branches go to $targets" }
        check(insns[4].field()?.name == "FALSE" && insns[5].opcode == Opcode.RETURN_OBJECT && insns[6].opcode == Opcode.INSTANCE_OF) {
            "${resolver.label()} does not return FALSE for the disabled flags and resume the original code"
        }
    }

    private fun assertGhostModes(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // Ghost typing: requestAction by metadata, which is also the shape fallback's method.
        val action = enclosingCoroutine(classes, byType, "$CHAT_ROOM_API_HELPER\$requestAction\$4")
        val byShape = classes.fromSource("ChatRoomApiHelper.kt").singleMethod("requestAction shape") { _, m ->
            m.params() == listOf("J", "I", "J") && m.returnType == "V" && AccessFlags.FINAL.isSet(m.accessFlags)
        }
        check(action.label() == byShape.label()) { "requestAction anchors disagree: ${action.label()} vs ${byShape.label()}" }
        assertReturnsEarlyVoid(action, label = action.label())

        // Ghost read: requestChatOnRoom returns FALSE at once...
        val chatOnRoom = enclosingCoroutine(classes, byType, CHAT_ROOM_API_HELPER, "requestChatOnRoom")
        assertReturnsObject(chatOnRoom, "Ljava/lang/Boolean;", "FALSE")
        // ...and MsgJob puts FALSE for "notiRead".
        val msgJob = classes.fromSource("MsgJob.kt").singleMethod("MsgJob notiRead") { _, m -> "notiRead" in m.strings() }
        val insns = msgJob.instructions()
        val key = insns.indexOfFirst { it.stringOrNull() == "notiRead" }
        val put = insns[key + 1] as FiveRegisterInstruction
        val value = insns[key - 1]
        check(value.opcode == Opcode.SGET_OBJECT && value.field()?.name == "FALSE" &&
            value.field()?.definingClass == "Ljava/lang/Boolean;" && (value as OneRegisterInstruction).registerA == put.registerE) {
            "${msgJob.label()} does not put Boolean.FALSE for notiRead; got ${value.opcode} ${value.field()}"
        }
    }

    private fun assertStripImageMetadata(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        check(byType.containsKey(STRIP_IMAGE_METADATA)) { "$STRIP_IMAGE_METADATA was not merged" }
        val resize = classes.fromSource("ImageItemSendHelper.kt").singleMethod("image resize step") { _, m ->
            AccessFlags.STATIC.isSet(m.accessFlags) && m.params() == listOf("Ljava/io/File;", "I", "J") &&
                m.returnType == "Ljava/io/File;"
        }
        val insns = resize.instructions()
        val returns = insns.indices.filter { insns[it].opcode == Opcode.RETURN_OBJECT }
        check(returns.size >= 2) { "${resize.label()} lost a return" }
        for (r in returns) {
            val register = (insns[r] as OneRegisterInstruction).registerA
            val call = insns[r - 2]
            check(call.call()?.definingClass == STRIP_IMAGE_METADATA && call.call()?.name == "sanitize" &&
                (call as RegisterRangeInstruction).startRegister == register &&
                insns[r - 1].opcode == Opcode.MOVE_RESULT_OBJECT && (insns[r - 1] as OneRegisterInstruction).registerA == register) {
                "${resize.label()}: return at $r is not sanitized"
            }
        }
        // Branches must land on the sanitize call, not skip it to the return.
        val jumpsToReturn = branchTargets(insns).filterValues { it in returns }
        check(jumpsToReturn.isEmpty()) { "${resize.label()}: branches skip the sanitize call: $jumpsToReturn" }
    }
}
