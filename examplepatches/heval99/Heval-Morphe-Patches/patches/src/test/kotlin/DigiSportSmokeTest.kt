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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

private const val PKG = "ro.rcsrds.digisport"
private const val ADMOB_MANAGER = "Lro/rcsrds/digisport/tools/helpers/ads/AdMobManager;"
private const val UI_AD = "Lro/rcsrds/digisport/data/model/ui/others/UIAd;"
private const val VH_SECTION_AD = "Lro/rcsrds/digisport/ui/article/adapter/viewholder/VHSectionAd;"
private const val UI_LIVE_PREROLL = "Lro/rcsrds/digisport/data/model/ui/live/UiLivePreroll;"
private const val UI_LIVE_DAI = "Lro/rcsrds/digisport/data/model/ui/live/UiLiveDai;"
private const val ARTICLE_API_PAYLOADS =
    "Lro/rcsrds/digisport/tools/helpers/parsing/articleTextAndLive/ArticleApiPayloads;"
private const val EXTENSION_SERVICE = "Lro/rcsrds/lib/player/core/extensions/ExtensionServiceKt;"
private const val PLAYER_SERVICE = "Lro/rcsrds/lib/player/PlayerService;"
private val AD_SLOT_PARAMETERS = listOf("Landroid/view/ViewGroup;", UI_AD)
private const val PARSING = "Lro/rcsrds/digisport/tools/helpers/parsing/"
private const val ARTICLE_WIDGETS = "${PARSING}articleTextAndLive/GetArticleDetailsWidgets;"
private const val ARTICLE_SECTION = "Lro/rcsrds/digisport/data/model/ui/articles/ArticleSection;"
private const val API_ARTICLE = "Lro/rcsrds/digisport/data/model/api/article/ApiArticle;"
private const val API_ARTICLE_LAYOUT = "Lro/rcsrds/digisport/data/model/api/article/ApiArticleLayout;"
private const val PARSING_EXTENSIONS = "${PARSING}ParsingExtensionsKt;"
private const val CONSENT = "Lro/rcsrds/digisport/tools/helpers/googleConsent/GoogleConsentUmp;"
private const val CONSENT_STATUS = "Lro/rcsrds/digisport/tools/helpers/googleConsent/GoogleConsentUmp\$STATUS;"
private const val CRASHLYTICS = "Lcom/google/firebase/crashlytics/FirebaseCrashlytics;"
private const val PERFORMANCE = "Lcom/google/firebase/perf/FirebasePerformance;"
private const val BOOLEAN = "Ljava/lang/Boolean;"

class DigiSportSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Instruction.methodRef() = (this as? ReferenceInstruction)?.reference as? MethodReference
    private fun Instruction.fieldRef() = (this as? ReferenceInstruction)?.reference as? FieldReference
    private fun Instruction.registerA() = (this as OneRegisterInstruction).registerA

    private fun List<ClassDef>.byType(type: String): ClassDef =
        firstOrNull { it.type == type } ?: error("$type not found in emitted dexes")

    /** The register of parameter [index] (0 = this) of an instance method. */
    private fun Method.p(index: Int): Int = implementation!!.registerCount - (parameterTypes.size + 1) + index

    /** Index of the instruction a branch at [index] jumps to, or -1. */
    private fun List<Instruction>.branchTarget(index: Int): Int {
        val target = take(index).sumOf { it.codeUnits } + (this[index] as OffsetInstruction).codeOffset
        var address = 0
        forEachIndexed { i, insn ->
            if (address == target) return i
            address += insn.codeUnits
        }
        return -1
    }

    private fun Instruction.calls(owner: String, name: String) =
        methodRef()?.let { it.definingClass == owner && it.name == name } == true

    private fun Instruction.rangeStart() = (this as RegisterRangeInstruction).startRegister

    /** Asserts `const/4 vX, 0x0` + `return-object vX` at the top. */
    private fun assertReturnsNull(method: Method, label: String) {
        val insns = method.instructions()
        val first = insns.getOrNull(0)
        assertTrue(
            first?.opcode == Opcode.CONST_4 && (first as NarrowLiteralInstruction).narrowLiteral == 0 &&
                insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT && insns[1].registerA() == first.registerA(),
            "$label does not return null; starts with ${first?.opcode}, ${insns.getOrNull(1)?.opcode}"
        )
    }

    /** Asserts `new-instance vX, ArrayList` + `ArrayList.<init>()` + `return-object vX` at the top. */
    private fun assertReturnsNewEmptyArrayList(method: Method, label: String) {
        val insns = method.instructions()
        val create = insns.getOrNull(0)
        val init = insns.getOrNull(1)
        val type = ((create as? ReferenceInstruction)?.reference as? TypeReference)?.type
        assertTrue(
            create?.opcode == Opcode.NEW_INSTANCE && type == "Ljava/util/ArrayList;" &&
                init?.opcode == Opcode.INVOKE_DIRECT && init.calls("Ljava/util/ArrayList;", "<init>") &&
                init.methodRef()!!.parameterTypes.isEmpty() &&
                (init as FiveRegisterInstruction).registerC == create.registerA() &&
                insns.getOrNull(2)?.opcode == Opcode.RETURN_OBJECT && insns[2].registerA() == create.registerA(),
            "$label does not return a new empty ArrayList; starts with ${create?.opcode} $type, ${init?.opcode}, " +
                "${insns.getOrNull(2)?.opcode}"
        )
    }

    /** Asserts the first instruction loads false (const/4 0 or Boolean.FALSE) into parameter 1. */
    private fun assertArgumentForcedOff(method: Method, label: String) {
        val first = method.instructions().firstOrNull()
        val boxed = method.parameterTypes.single().toString() == BOOLEAN
        val loadsFalse = if (boxed) {
            first?.opcode == Opcode.SGET_OBJECT && first.fieldRef()?.definingClass == BOOLEAN &&
                first.fieldRef()?.name == "FALSE"
        } else {
            first?.opcode == Opcode.CONST_4 && (first as NarrowLiteralInstruction).narrowLiteral == 0
        }
        assertTrue(
            loadsFalse && first!!.registerA() == method.p(1),
            "$label does not force its argument (p1 = v${method.p(1)}) off first; found ${first?.opcode} ${first?.fieldRef()}"
        )
    }

    /**
     * Asserts that a `getSponsored()` + null-safe isBlank guard of [length] instructions starts at
     * [start] on [articleRegister] (a range invoke when [range]), and that both branches skip to its
     * trailing nop.
     */
    private fun assertSponsoredGuard(
        insns: List<Instruction>,
        start: Int,
        length: Int,
        articleRegister: Int,
        range: Boolean,
        label: String,
    ) {
        val get = insns.getOrNull(start)
        val register = if (range) get?.takeIf { it.opcode == Opcode.INVOKE_VIRTUAL_RANGE }?.rangeStart()
        else (get as? FiveRegisterInstruction)?.takeIf { get.opcode == Opcode.INVOKE_VIRTUAL }?.registerC
        assertTrue(
            get?.calls(API_ARTICLE, "getSponsored") == true && register == articleRegister,
            "$label does not read the article's getSponsored() first; found ${get?.opcode} ${get?.methodRef()}"
        )
        val nop = start + length - 1
        assertEquals(Opcode.MOVE_RESULT_OBJECT, insns[start + 1].opcode, "$label: no move-result after getSponsored")
        assertEquals(Opcode.IF_EQZ, insns[start + 2].opcode, "$label: no null check on the sponsored text")
        assertTrue(insns[start + 3].calls("Lkotlin/text/StringsKt;", "isBlank"), "$label: no isBlank check")
        assertEquals(Opcode.IF_NEZ, insns[start + 5].opcode, "$label: no blank check branch")
        assertEquals(Opcode.NOP, insns[nop].opcode, "$label: the guard does not end with its nop label")
        assertEquals(nop, insns.branchTarget(start + 2), "$label: the null branch does not skip the guard")
        assertEquals(nop, insns.branchTarget(start + 5), "$label: the blank branch does not skip the guard")
    }

    @Test
    fun `Digi Sport patches neutralise ads, odds, telemetry, update and consent prompts, sponsored and cross-promo cards`() {
        val root = repoRoot()
        val apk = File(root, "apks/digisport/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "1.1.15",
            patchNames = setOf(
                "Disable ads",
                "Disable live stream ads",
                "Hide betting odds",
                "Disable telemetry",
                "Disable update prompts",
                "Skip ad consent form",
                "Hide sponsored articles",
                "Hide promotions",
            ),
            allPatches = loadAllPatches(newestPatchBundle(root)),
        )

        // Disable ads 1: AdMobManager.loadAd starts with const/16 8, container.setVisibility(GONE),
        // return-void.
        val manager = classes.byType(ADMOB_MANAGER)
        val loadAd = manager.method("loadAd", AD_SLOT_PARAMETERS)
        val loadInsns = loadAd.instructions()
        val gone = loadInsns.getOrNull(0)
        val hide = loadInsns.getOrNull(1)
        val hideRef = hide?.methodRef()
        assertTrue(
            gone?.opcode == Opcode.CONST_16 && (gone as NarrowLiteralInstruction).narrowLiteral == 8,
            "AdMobManager.loadAd does not start with const/16 0x8; found ${gone?.opcode}"
        )
        assertTrue(
            hide?.opcode == Opcode.INVOKE_VIRTUAL &&
                hideRef?.definingClass == "Landroid/view/View;" && hideRef.name == "setVisibility" &&
                (hide as FiveRegisterInstruction).registerC == loadAd.p(1) &&
                hide.registerD == gone!!.registerA(),
            "AdMobManager.loadAd does not call container.setVisibility(GONE); found ${hide?.opcode} $hideRef"
        )
        assertEquals(Opcode.RETURN_VOID, loadInsns.getOrNull(2)?.opcode, "AdMobManager.loadAd does not return after hiding")

        // Disable ads 1: requestAd and putAd return at once.
        assertReturnsEarlyVoid(manager.method("requestAd", AD_SLOT_PARAMETERS), label = "AdMobManager.requestAd")
        assertReturnsEarlyVoid(manager.method("putAd", AD_SLOT_PARAMETERS), label = "AdMobManager.putAd")

        // Disable ads 2: VHSectionAd.bind (.locals 0) loads itemView into p1, sets it GONE, zeroes
        // its LayoutParams height when there are any, and returns before loadAd.
        val bind = classes.byType(VH_SECTION_AD).method("bind", listOf(UI_AD))
        val b = bind.instructions()
        val p0 = bind.p(0)
        val p1 = bind.p(1)
        val label = "VHSectionAd.bind"
        val itemView = b.getOrNull(0)
        assertTrue(
            itemView?.opcode == Opcode.IGET_OBJECT && itemView.fieldRef()?.name == "itemView" &&
                (itemView as TwoRegisterInstruction).registerA == p1 && itemView.registerB == p0,
            "$label does not load this.itemView into p1 first; found ${itemView?.opcode} ${itemView?.fieldRef()}"
        )
        val bindGone = b.getOrNull(1)
        assertTrue(
            bindGone?.opcode == Opcode.CONST_16 && (bindGone as NarrowLiteralInstruction).narrowLiteral == 8 &&
                bindGone.registerA() == p0,
            "$label does not load View.GONE into p0; found ${bindGone?.opcode}"
        )
        val setVisibility = b.getOrNull(2)
        assertTrue(
            setVisibility?.methodRef()?.name == "setVisibility" &&
                (setVisibility as FiveRegisterInstruction).registerC == p1 && setVisibility.registerD == p0,
            "$label does not call itemView.setVisibility(GONE); found ${setVisibility?.opcode} ${setVisibility?.methodRef()}"
        )
        val getParams = b.getOrNull(3)
        assertTrue(
            getParams?.methodRef()?.name == "getLayoutParams" && (getParams as FiveRegisterInstruction).registerC == p1 &&
                b.getOrNull(4)?.opcode == Opcode.MOVE_RESULT_OBJECT && b[4].registerA() == p0,
            "$label does not read itemView's LayoutParams into p0; found ${getParams?.opcode} ${getParams?.methodRef()}"
        )
        val nullCheck = b.getOrNull(5)
        assertTrue(
            nullCheck?.opcode == Opcode.IF_EQZ && nullCheck.registerA() == p0 &&
                (nullCheck as OffsetInstruction).codeOffset == b.subList(5, 8).sumOf { it.codeUnits },
            "$label does not skip the height write for null LayoutParams; found ${nullCheck?.opcode}"
        )
        val zero = b.getOrNull(6)
        val height = b.getOrNull(7)
        assertTrue(
            zero?.opcode == Opcode.CONST_4 && (zero as NarrowLiteralInstruction).narrowLiteral == 0 &&
                height?.opcode == Opcode.IPUT && height.fieldRef()?.definingClass == "Landroid/view/ViewGroup\$LayoutParams;" &&
                height.fieldRef()?.name == "height" &&
                (height as TwoRegisterInstruction).registerA == zero.registerA() && height.registerB == p0,
            "$label does not set LayoutParams.height = 0; found ${zero?.opcode}, ${height?.opcode} ${height?.fieldRef()}"
        )
        assertEquals(Opcode.RETURN_VOID, b.getOrNull(8)?.opcode, "$label does not return after collapsing the row")

        // Disable ads 3: UiLivePreroll.getEnable() answers false.
        assertForcedBoolean(
            classes.byType(UI_LIVE_PREROLL).method("getEnable", emptyList()),
            expected = false,
            label = "UiLivePreroll.getEnable()",
        )

        // Disable ads 4: ArticleApiPayloads.getMVideoPreroll() returns "" (not null).
        val preroll = classes.byType(ARTICLE_API_PAYLOADS).method("getMVideoPreroll", emptyList()).instructions()
        val empty = preroll.getOrNull(0)
        assertTrue(
            empty?.opcode == Opcode.CONST_STRING &&
                ((empty as ReferenceInstruction).reference as StringReference).string == "" &&
                preroll.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT && preroll[1].registerA() == empty.registerA(),
            "ArticleApiPayloads.getMVideoPreroll does not return \"\"; starts with ${empty?.opcode}, ${preroll.getOrNull(1)?.opcode}"
        )

        // Disable ads 5: ExtensionServiceKt.initImaAds returns null.
        val service = classes.byType(EXTENSION_SERVICE)
        val initIma = service.method(
            "initImaAds",
            listOf(PLAYER_SERVICE, "Landroidx/media3/common/MediaItem\$AdsConfiguration;"),
        ).instructions()
        val nullConst = initIma.getOrNull(0)
        assertTrue(
            nullConst?.opcode == Opcode.CONST_4 && (nullConst as NarrowLiteralInstruction).narrowLiteral == 0 &&
                initIma.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT && initIma[1].registerA() == nullConst.registerA(),
            "ExtensionServiceKt.initImaAds does not return null; starts with ${nullConst?.opcode}, ${initIma.getOrNull(1)?.opcode}"
        )

        // Disable live stream ads 1: UiLiveDai.getEnable() answers false.
        assertForcedBoolean(
            classes.byType(UI_LIVE_DAI).method("getEnable", emptyList()),
            expected = false,
            label = "UiLiveDai.getEnable()",
        )

        // Disable live stream ads 2: ExtensionServiceKt.updateAdViewForDai returns at once.
        assertReturnsEarlyVoid(
            service.method("updateAdViewForDai", listOf(PLAYER_SERVICE)),
            label = "ExtensionServiceKt.updateAdViewForDai",
        )

        // Disable ads 6: the four server ad-list getters return a new empty ArrayList.
        for ((type, name) in listOf(
            "Lro/rcsrds/digisport/data/model/api/article/ApiArticleData;" to "getAds",
            "Lro/rcsrds/digisport/data/model/api/epg/ApiEpgData;" to "getAds",
            "Lro/rcsrds/digisport/data/model/api/listingArticles/Data;" to "getAdsList",
            "Lro/rcsrds/digisport/data/model/api/soccer/APIResultsResponseData;" to "getAds",
        )) {
            assertReturnsNewEmptyArrayList(classes.byType(type).method(name, emptyList()), label = "$type.$name")
        }

        // Disable ads 7: in-article <ad-api> elements are never ads.
        assertForcedBoolean(
            classes.byType("${PARSING}articleTextAndLive/GetArticleDetailsLiveTab1;")
                .method("adConstraints", listOf("Lorg/jsoup/nodes/Element;")),
            expected = false,
            label = "GetArticleDetailsLiveTab1.adConstraints(Element)",
        )

        // Disable ads 8: no ad section between article widgets.
        val widgets = classes.byType(ARTICLE_WIDGETS)
        assertReturnsNull(widgets.method("addAdWidget", listOf(ARTICLE_API_PAYLOADS, "I")), "GetArticleDetailsWidgets.addAdWidget")

        // Disable ads 9: the EPG cache gets no ad rows.
        assertReturnsEarlyVoid(
            classes.byType("Lro/rcsrds/digisport/data/sync/epg/repository/SyncEpgRepositoryBase;").method(
                "addBanner",
                listOf("Ljava/util/List;", "Lro/rcsrds/digisport/data/model/ui/epg/UIEpgCacheDay;", "Ljava/lang/String;"),
            ),
            label = "SyncEpgRepositoryBase.addBanner",
        )

        // Hide betting odds: UIResultsMatch.getBet() returns null.
        assertReturnsNull(
            classes.byType("Lro/rcsrds/digisport/data/model/ui/articles/UIResultsMatch;").method("getBet", emptyList()),
            "UIResultsMatch.getBet",
        )

        // Disable telemetry: the Crashlytics and Performance collection setters force their
        // argument off; the Crashlytics senders, Cxense pushEvents and Analytics logEvent return.
        val crashlytics = classes.byType(CRASHLYTICS)
        val performance = classes.byType(PERFORMANCE)
        for ((klass, name) in listOf(crashlytics to "setCrashlyticsCollectionEnabled", performance to "setPerformanceCollectionEnabled")) {
            for (parameter in listOf("Z", BOOLEAN)) {
                assertArgumentForcedOff(klass.method(name, listOf(parameter)), "${klass.type}.$name($parameter)")
            }
        }
        val recordException = crashlytics.methods.filter { it.name == "recordException" && it.implementation != null }
        assertTrue(
            recordException.map { it.parameterTypes.map(CharSequence::toString) }.toSet() == setOf(
                listOf("Ljava/lang/Throwable;"),
                listOf("Ljava/lang/Throwable;", "Lcom/google/firebase/crashlytics/CustomKeysAndValues;"),
            ),
            "unexpected recordException overloads: ${recordException.map { it.parameterTypes }}"
        )
        recordException.forEach { assertReturnsEarlyVoid(it, label = "FirebaseCrashlytics.recordException(${it.parameterTypes.joinToString()})") }
        assertReturnsEarlyVoid(crashlytics.method("log", listOf("Ljava/lang/String;")), label = "FirebaseCrashlytics.log")
        assertReturnsEarlyVoid(crashlytics.method("sendUnsentReports", emptyList()), label = "FirebaseCrashlytics.sendUnsentReports")
        assertReturnsEarlyVoid(
            classes.byType("Lio/piano/android/cxense/CxenseSdk;").method("pushEvents", listOf("[Lio/piano/android/cxense/model/Event;")),
            label = "CxenseSdk.pushEvents",
        )
        assertReturnsEarlyVoid(
            classes.byType("Lcom/google/firebase/analytics/FirebaseAnalytics;")
                .method("logEvent", listOf("Ljava/lang/String;", "Landroid/os/Bundle;")),
            label = "FirebaseAnalytics.logEvent",
        )

        // Disable telemetry (manifest): the Firebase switches are set on <application>.
        val manifestFile = workDir.walkTopDown()
            .filter { it.name == "AndroidManifest.xml" && it.readText().contains("firebase_crashlytics_collection_enabled") }
            .firstOrNull() ?: error("no decoded AndroidManifest.xml with the telemetry meta-data under $workDir")
        val manifest = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifestFile).documentElement
        fun Element.children(tag: String) =
            getElementsByTagName(tag).let { list -> (0 until list.length).map { list.item(it) as Element } }
        val application = manifest.children("application").single()
        val metaData = application.children("meta-data")
            .filter { it.parentNode == application }
            .groupBy({ it.getAttribute("android:name") }, { it.getAttribute("android:value") })
        for ((name, value) in mapOf(
            "firebase_analytics_collection_deactivated" to "true",
            "firebase_performance_collection_deactivated" to "true",
            "firebase_crashlytics_collection_enabled" to "false",
        )) {
            check(metaData[name] == listOf(value)) { "meta-data $name is ${metaData[name]}, expected [$value]" }
        }

        // Disable update prompts: MainActivityBase.checkUpdate returns at once.
        assertReturnsEarlyVoid(
            classes.byType("Lro/rcsrds/digisport/ui/main/MainActivityBase;").method("checkUpdate", emptyList()),
            label = "MainActivityBase.checkUpdate",
        )

        // Skip ad consent form: loadConsentInfo posts STATUS.SUCCESS to mStatus and returns this.
        val loadConsent = classes.byType(CONSENT).method("loadConsentInfo", listOf("Z"))
        val c = loadConsent.instructions()
        assertTrue(
            c.getOrNull(0)?.opcode == Opcode.IGET_OBJECT && c[0].fieldRef()?.name == "mStatus" &&
                (c[0] as TwoRegisterInstruction).registerB == loadConsent.p(0),
            "GoogleConsentUmp.loadConsentInfo does not read this.mStatus first; found ${c.getOrNull(0)?.opcode}"
        )
        assertTrue(
            c.getOrNull(1)?.opcode == Opcode.SGET_OBJECT && c[1].fieldRef()?.definingClass == CONSENT_STATUS &&
                c[1].fieldRef()?.name == "SUCCESS",
            "GoogleConsentUmp.loadConsentInfo does not load STATUS.SUCCESS; found ${c.getOrNull(1)?.opcode} ${c.getOrNull(1)?.fieldRef()}"
        )
        val post = c.getOrNull(2) as? FiveRegisterInstruction
        assertTrue(
            post != null && c[2].calls("Landroidx/lifecycle/MutableLiveData;", "postValue") &&
                post.registerC == c[0].registerA() && post.registerD == c[1].registerA(),
            "GoogleConsentUmp.loadConsentInfo does not post SUCCESS to mStatus; found ${c.getOrNull(2)?.opcode}"
        )
        assertTrue(
            c.getOrNull(3)?.opcode == Opcode.RETURN_OBJECT && c[3].registerA() == loadConsent.p(0),
            "GoogleConsentUmp.loadConsentInfo does not return this right after; found ${c.getOrNull(3)?.opcode}"
        )

        // Skip ad consent form 2: the Settings ad consent block's executeBindings first sets its
        // root (mboundView0) GONE, and no later instruction touches mboundView0 again.
        val userDataAds = classes.byType("Lro/rcsrds/digisport/databinding/IncludeSettingsUserDataAdsBindingImpl;")
        val bindings = userDataAds.method("executeBindings", emptyList())
        val e = bindings.instructions()
        assertTrue(
            e.getOrNull(0)?.opcode == Opcode.IGET_OBJECT && e[0].fieldRef()?.name == "mboundView0" &&
                (e[0] as TwoRegisterInstruction).registerB == bindings.p(0),
            "executeBindings does not read this.mboundView0 first; found ${e.getOrNull(0)?.opcode} ${e.getOrNull(0)?.fieldRef()}"
        )
        assertTrue(
            e.getOrNull(1)?.opcode == Opcode.CONST_16 && (e[1] as NarrowLiteralInstruction).narrowLiteral == 8,
            "executeBindings does not load View.GONE second; found ${e.getOrNull(1)?.opcode}"
        )
        val hideRoot = e.getOrNull(2) as? FiveRegisterInstruction
        assertTrue(
            hideRoot != null && e[2].calls("Landroid/view/View;", "setVisibility") &&
                hideRoot.registerC == e[0].registerA() && hideRoot.registerD == e[1].registerA(),
            "executeBindings does not call mboundView0.setVisibility(GONE); found ${e.getOrNull(2)?.opcode} ${e.getOrNull(2)?.methodRef()}"
        )
        assertTrue(
            e.drop(3).none { it.fieldRef()?.name == "mboundView0" },
            "executeBindings reads mboundView0 again after hiding it"
        )

        // Hide sponsored articles 1: parseListingArticles returns early for a sponsored article.
        val parseListing = classes.byType("${PARSING}listing/GetListingDetailsArticles;")
            .method("parseListingArticles", listOf(API_ARTICLE))
        val l = parseListing.instructions()
        assertSponsoredGuard(l, start = 0, length = 8, articleRegister = parseListing.p(1), range = false, label = "parseListingArticles")
        assertEquals(Opcode.RETURN_VOID, l[6].opcode, "parseListingArticles does not return for a sponsored article")

        // Hide sponsored articles 2 + Hide promotions 1: addArticleWidget starts with both
        // self-contained guards (in either order), each returning getSpacing().
        val addArticle = widgets.method("addArticleWidget", listOf("Z", API_ARTICLE))
        val a = addArticle.instructions()
        fun returnsSpacing(at: Int) = a[at].calls(PARSING_EXTENSIONS, "getSpacing") &&
            a[at + 1].opcode == Opcode.MOVE_RESULT_OBJECT && a[at + 2].opcode == Opcode.RETURN_OBJECT &&
            a[at + 2].registerA() == a[at + 1].registerA()
        var index = 0
        var sponsoredGuards = 0
        var crossGuards = 0
        repeat(2) {
            if (a[index].opcode == Opcode.IF_EQZ) {
                // Cross guard: if-eqz p1, getSpacing, move-result-object, return-object, nop.
                assertTrue(
                    a[index].registerA() == addArticle.p(1) && returnsSpacing(index + 1) &&
                        a[index + 4].opcode == Opcode.NOP && a.branchTarget(index) == index + 4,
                    "addArticleWidget's cross guard at $index is malformed"
                )
                crossGuards++
                index += 5
            } else {
                assertSponsoredGuard(a, start = index, length = 10, articleRegister = addArticle.p(2), range = true, label = "addArticleWidget")
                assertTrue(returnsSpacing(index + 6), "addArticleWidget's sponsored guard does not return getSpacing()")
                sponsoredGuards++
                index += 10
            }
        }
        assertTrue(sponsoredGuards == 1 && crossGuards == 1, "addArticleWidget guards: $sponsoredGuards sponsored, $crossGuards cross")

        // Hide promotions 2: widgetTitle returns for a "widgetCross" widget.
        val widgetTitle = widgets.method("widgetTitle", listOf("Ljava/util/ArrayList;", API_ARTICLE_LAYOUT))
        val t = widgetTitle.instructions()
        assertTrue(
            t[0].opcode == Opcode.INVOKE_VIRTUAL_RANGE && t[0].calls(API_ARTICLE_LAYOUT, "getType") &&
                t[0].rangeStart() == widgetTitle.p(2) && t[1].opcode == Opcode.MOVE_RESULT_OBJECT &&
                t[2].opcode == Opcode.CONST_STRING &&
                ((t[2] as ReferenceInstruction).reference as StringReference).string == "widgetCross" &&
                t[3].calls("Ljava/lang/String;", "equals") && t[4].opcode == Opcode.MOVE_RESULT &&
                t[5].opcode == Opcode.IF_EQZ && t[6].opcode == Opcode.RETURN_VOID && t[7].opcode == Opcode.NOP &&
                t.branchTarget(5) == 7,
            "widgetTitle does not return for a widgetCross widget; starts with ${t.take(8).map { it.opcode }}"
        )

        // Hide promotions 3: HomeViewModel.getExclusiveVideo returns at once.
        assertReturnsEarlyVoid(
            classes.byType("Lro/rcsrds/digisport/ui/main/home/HomeViewModel;").method("getExclusiveVideo", emptyList()),
            label = "HomeViewModel.getExclusiveVideo",
        )
    }
}
