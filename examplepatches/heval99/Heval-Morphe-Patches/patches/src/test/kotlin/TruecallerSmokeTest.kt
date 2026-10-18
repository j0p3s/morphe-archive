import com.android.tools.smali.dexlib2.AccessFlags
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
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

private const val PKG = "com.truecaller"
private const val TRUECALLER_INIT = "Lcom/truecaller/ui/TruecallerInit;"
private const val SIGNATURE_SHA1 = "0ac1169ae6cead75264c725febd8e8d941f25e31"

private const val PREMIUM_TIER_TYPE = "Lcom/truecaller/premium/data/tier/PremiumTierType;"
private const val PREMIUM_FEATURE = "Lcom/truecaller/premium/data/feature/PremiumFeature;"
private const val PREMIUM_FEATURE_STATUS = "Lcom/truecaller/premium/data/feature/PremiumFeatureStatus;"
private const val SUBSCRIPTION_WORKER = "Lcom/truecaller/premium/PremiumSubscriptionStatusNotificationWorker;"
private const val OTP_SMS_API = "Lcom/truecaller/wizard/verification/otp/sms/OtpSmsApi;"
private const val UPDATE_TRIGGER = "Lcom/truecaller/inappupdate/UpdateTrigger;"
private const val ADS_CONTAINER = "Lcom/truecaller/ads/ui/AdsContainer;"
private const val ADS_CONTAINER_LIGHT = "Lcom/truecaller/ads/ui/AdsContainerLight;"
private const val ADS_SWITCH_VIEW = "Lcom/truecaller/ads/ui/AdsSwitchView;"
private const val AD_LAYOUT_TYPE = "Lcom/truecaller/ads/AdLayoutTypeX;"
private const val BOTTOM_BAR_BUTTON_TYPE = "Lcom/truecaller/bottombar/BottomBarButtonType;"
private const val BOTTOM_BAR_VIEW = "Lcom/truecaller/bottombar/BottomBarView;"
private const val AD_PLACEMENT = "Lcom/truecaller/ads/api/model/ad/AdPlacement;"
private const val FEATURE_STATE = "Lcom/truecaller/featuretoggles/FeatureState;"
private const val T9_REBUILD_ALL_KEY = "RefreshT9MappingWorker.RebuildAll"
private const val T9_INCREMENTAL_HELPER = "morpheRefreshT9Incremental"

class TruecallerSmokeTest {

    @TempDir
    lateinit var workDir: File

    private fun Method.params() = parameterTypes.map(CharSequence::toString)

    private fun Method.strings() = instructions().mapNotNull { it.stringOrNull() }

    private fun Instruction.field(): FieldReference? = (this as? ReferenceInstruction)?.reference as? FieldReference

    private fun Instruction.call(): MethodReference? = (this as? ReferenceInstruction)?.reference as? MethodReference

    private fun List<ClassDef>.methodsWhere(predicate: (ClassDef, Method) -> Boolean): List<Pair<ClassDef, Method>> =
        flatMap { cls -> cls.methods.filter { predicate(cls, it) }.map { cls to it } }

    private fun List<ClassDef>.singleMethod(label: String, predicate: (ClassDef, Method) -> Boolean): Pair<ClassDef, Method> {
        val found = methodsWhere(predicate)
        check(found.size == 1) { "expected one $label, found ${found.map { (c, m) -> "${c.type}.${m.name}" }}" }
        return found.single()
    }

    /** Index of the instruction the branch at [index] jumps to. */
    private fun List<Instruction>.branchTarget(index: Int): Int {
        val offsets = runningFold(0) { offset, instruction -> offset + instruction.codeUnits }
        val target = offsets[index] + (this[index] as OffsetInstruction).codeOffset
        return offsets.indexOf(target)
    }

    private fun assertReturnsGold(method: Method, label: String) {
        val insns = method.instructions()
        val gold = insns.getOrNull(0)?.field()
        check(insns.getOrNull(0)?.opcode == Opcode.SGET_OBJECT && gold?.definingClass == PREMIUM_TIER_TYPE && gold.name == "GOLD") {
            "$label does not load PremiumTierType.GOLD first; got ${insns.getOrNull(0)?.opcode} $gold"
        }
        check(insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT) { "$label does not return GOLD at once" }
    }

    @Test
    fun `Truecaller patches unlock premium, remove ads, telemetry, update prompts and optional UI, and route GMS to MicroG RE`() {
        val root = repoRoot()
        val apk = File(root, "apks/truecaller/base.apk")

        // The APK lives in the gitignored apks/ directory. Skip rather than fail when it is
        // absent, so CI stays green; locally it is present and the test really runs.
        assumeTrue(apk.exists(), "skipping: base.apk not present at ${apk.path}")

        val allPatches = loadAllPatches(newestPatchBundle(root))
        // No Clone package name option (#50): a com.truecaller clone cannot install next to stock.
        val gmsCore = allPatches.single { p ->
            p.name == "GmsCore support" && p.compatibility.orEmpty().any { it.packageName == PKG }
        }
        check("packageName" !in gmsCore.options.keys) { "Truecaller GmsCore support still offers the packageName option" }

        val classes = applyPatches(
            apk = apk,
            workDir = workDir,
            pkg = PKG,
            version = "26.40.5",
            patchNames = setOf(
                "Enable Premium",
                "Disable ads",
                "Disable telemetry",
                "Disable update prompts",
                "Hide Assistant tab",
                "Hide Scam feed",
                "Hide Premium tab",
                "Hide Blocking tab",
                "Hide Messages tab",
                "Hide family button",
                "Restore classic call log",
                "Use SMS for verification codes",
                "Fix T9 contact search",
                "Hide Wear OS settings",
                "GmsCore support",
                "Spoof signature",
            ),
            allPatches = allPatches,
        )
        val byType = classes.associateBy { it.type }

        assertEnablePremium(classes, byType)
        assertDisableAds(classes, byType)
        assertDisableTelemetry(classes, byType)
        assertDisableUpdatePrompts(classes)
        assertFeatureToggles(classes)
        assertHiddenTabs(classes)
        assertHideMessagesTab(classes)
        assertHiddenSettingsRows(classes)
        assertToggleOverrides(classes)
        assertUseSmsVerification(classes)
        assertFixT9ContactSearch(classes, byType)
        // onGetCredential tests isAvailableOnDevice(int) in this build, so the routing applies.
        assertGmsCoreSupport(classes, workDir, PKG, TRUECALLER_INIT, SIGNATURE_SHA1, expectCredentialRouting = true)
        assertSpoofSignature(classes, PKG, SIGNATURE_SHA1)
    }

    private fun assertEnablePremium(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // 1. Tier getter ("premiumLevel") returns GOLD.
        val (store, tier) = classes.singleMethod("premiumLevel tier getter") { _, m ->
            m.returnType == PREMIUM_TIER_TYPE && m.parameterTypes.isEmpty() && "premiumLevel" in m.strings()
        }
        assertReturnsGold(tier, "${store.type}.${tier.name}() (tier)")

        // 2. The plain isPremium getter in the same class; the grace-period getter is untouched.
        val isPremium = store.methods.single {
            it.returnType == "Z" && it.parameterTypes.isEmpty() &&
                "isPremiumExpired" in it.strings() && "isInGracePeriod" !in it.strings()
        }
        assertForcedBoolean(isPremium, expected = true, label = "${store.type}.${isPremium.name}() (isPremium)")
        val grace = store.methods.single { "isInGracePeriod" in it.strings() && it.returnType == "Z" }
        check(grace.instructions().first().opcode != Opcode.CONST_4) { "${store.type}.${grace.name}() (grace period) was changed" }

        // 3. Static PremiumFeatureStatus == INCLUDED check.
        val (gateClass, gate) = classes.singleMethod("PremiumFeatureStatus gate") { _, m ->
            m.returnType == "Z" && m.params() == listOf(PREMIUM_FEATURE_STATUS) && AccessFlags.STATIC.isSet(m.accessFlags) &&
                m.instructions().any { it.opcode == Opcode.SGET_OBJECT && it.field()?.name == "INCLUDED" }
        }
        assertForcedBoolean(gate, expected = true, label = "${gateClass.type}.${gate.name}(PremiumFeatureStatus)")

        // 4. isFeatureAvailable(feature, refresh): true at once, except CALL_ASSISTANT and
        // FAMILY_SHARING, which branch to the untouched stock check.
        val (featureClass, feature) = classes.singleMethod("feature availability check") { _, m ->
            m.returnType == "Z" && m.params() == listOf(PREMIUM_FEATURE, "Z") && "disabledPremiumFeatures" in m.strings()
        }
        val featureLabel = "${featureClass.type}.${feature.name}(PremiumFeature, Z)"
        val featureInsns = feature.instructions()
        val featureP1 = feature.implementation!!.registerCount - 2 // this + feature + Z
        for ((index, constant) in listOf(0 to "CALL_ASSISTANT", 2 to "FAMILY_SHARING")) {
            val get = featureInsns.getOrNull(index)
            check(get?.opcode == Opcode.SGET_OBJECT && get.field()?.definingClass == PREMIUM_FEATURE && get.field()?.name == constant) {
                "$featureLabel does not load PremiumFeature.$constant at $index; got ${get?.opcode} ${get?.field()}"
            }
            val branch = featureInsns[index + 1]
            check(branch.opcode == Opcode.IF_EQ && (branch as TwoRegisterInstruction).registerA == featureP1 &&
                branch.registerB == (get as OneRegisterInstruction).registerA) {
                "$featureLabel does not compare p1 with PremiumFeature.$constant"
            }
            check(featureInsns.branchTarget(index + 1) == 6) { "$featureLabel: the $constant branch does not reach the stock check" }
        }
        val forced = featureInsns.getOrNull(4)
        check(forced?.opcode == Opcode.CONST_4 && (forced as NarrowLiteralInstruction).narrowLiteral == 1 &&
            featureInsns.getOrNull(5)?.opcode == Opcode.RETURN &&
            (featureInsns[5] as OneRegisterInstruction).registerA == (forced as OneRegisterInstruction).registerA
        ) { "$featureLabel does not return true for the other features" }
        check(featureInsns.drop(6).any { it.stringOrNull() == "disabledPremiumFeatures" }) { "$featureLabel lost its stock check" }

        // 5. PremiumState constructor: const/16 p1, 0x1 and sget-object p2, GOLD before anything else.
        val stateClass = classes.single { cls -> cls.methods.any { m -> m.strings().any { it.startsWith("PremiumState(isPremium=") } } }
        val constructor = stateClass.method(
            "<init>",
            listOf(
                "Z", PREMIUM_TIER_TYPE, "Lcom/truecaller/premium/data/ProductKind;",
                "Lcom/truecaller/premium/data/PremiumScope;", "Lcom/truecaller/premium/data/InsuranceState;",
            ),
        )
        val p1 = constructor.implementation!!.registerCount - 5 // this + 5 single-register parameters
        val insns = constructor.instructions()
        val first = insns.getOrNull(0)
        check(
            first?.opcode == Opcode.CONST_16 && (first as OneRegisterInstruction).registerA == p1 &&
                (first as NarrowLiteralInstruction).narrowLiteral == 1
        ) { "${stateClass.type}.<init> does not start with const/16 p1, 0x1; got ${first?.opcode}" }
        val second = insns.getOrNull(1)
        check(
            second?.opcode == Opcode.SGET_OBJECT && (second as OneRegisterInstruction).registerA == p1 + 1 &&
                second.field()?.definingClass == PREMIUM_TIER_TYPE && second.field()?.name == "GOLD"
        ) { "${stateClass.type}.<init> does not set p2 to GOLD second; got ${second?.opcode} ${second?.field()}" }

        // 6. The subscription worker returns a fresh no-arg Result (Failure in this build) at once.
        val result = listOf("Success {mOutputData=", "Failure {mOutputData=").map { text ->
            classes.single { cls -> cls.methods.any { m -> m.name == "toString" && text in m.strings() } }
        }.first { cls -> cls.methods.any { it.name == "<init>" && it.parameterTypes.isEmpty() } }
        val doWork = (byType[SUBSCRIPTION_WORKER] ?: error("$SUBSCRIPTION_WORKER not found")).methods.single { it.name == "doWork" }
        val work = doWork.instructions()
        check(work.getOrNull(0)?.opcode == Opcode.NEW_INSTANCE &&
            ((work[0] as ReferenceInstruction).reference as TypeReference).type == result.type) {
            "doWork does not start with new-instance ${result.type}; got ${work.getOrNull(0)?.opcode}"
        }
        val init = work.getOrNull(1)?.call()
        check(work.getOrNull(1)?.opcode == Opcode.INVOKE_DIRECT && init?.definingClass == result.type && init.name == "<init>" &&
            init.parameterTypes.isEmpty()) { "doWork does not construct ${result.type}; got $init" }
        check(work.getOrNull(2)?.opcode == Opcode.RETURN_OBJECT) { "doWork does not return the Result at once" }

        // 7. The premium status sync writer returns Unit before storing anything or sending the
        // PremiumStatusUpdate event.
        val (writerClass, writer) = classes.singleMethod("premium status writer") { _, m ->
            m.returnType == "Ljava/lang/Object;" && m.parameterTypes.size == 2 &&
                m.strings().containsAll(listOf("premiumHadPremiumBefore", "entitledCatalogItemIds"))
        }
        val writerInsns = writer.instructions()
        val unit = writerInsns.getOrNull(0)
        check(unit?.opcode == Opcode.SGET_OBJECT && unit.field()?.definingClass == "Lkotlin/Unit;" &&
            unit.field()?.type == "Lkotlin/Unit;") {
            "${writerClass.type}.${writer.name} does not load kotlin.Unit first; got ${unit?.opcode} ${unit?.field()}"
        }
        val ret = writerInsns.getOrNull(1)
        check(ret?.opcode == Opcode.RETURN_OBJECT && (ret as OneRegisterInstruction).registerA == (unit as OneRegisterInstruction).registerA) {
            "${writerClass.type}.${writer.name} does not return Unit at once"
        }
    }

    private fun assertDisableAds(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        fun require(type: String) = byType[type] ?: error("$type not found")

        // Banner and native binds: the holder types are derived the same way the patch does.
        val switchView = require(ADS_SWITCH_VIEW)
        val bannerHolder = switchView.methods.single {
            it.returnType == "V" && it.params().size == 2 && it.params()[1] == AD_LAYOUT_TYPE
        }.params().first()
        val container = require(ADS_CONTAINER)
        val light = require(ADS_CONTAINER_LIGHT)
        val layoutConfig = container.methods.first {
            it.returnType == "V" && it.params().size >= 2 && it.params().first() == bannerHolder
        }.params()[1]
        val nativeHolders = (container.methods + light.methods).map { it.params() }
            .filter { it.size >= 2 && it[1] == layoutConfig && it[0] != bannerHolder }.map { it.first() }.toSet()
        check(nativeHolders.isNotEmpty()) { "no native ad holder type found" }
        val adTypes = nativeHolders + bannerHolder + AD_LAYOUT_TYPE

        var banner = 0
        var native = 0
        for (cls in listOf(container, light, switchView)) {
            val binds = cls.methods.filter { m ->
                m.returnType == "V" && m.name != "<init>" && !AccessFlags.STATIC.isSet(m.accessFlags) &&
                    m.params().firstOrNull()?.startsWith("L") == true && m.params().any { it in adTypes }
            }
            check(binds.isNotEmpty()) { "no ad bind method in ${cls.type}" }
            for (bind in binds) {
                val label = "${cls.type}.${bind.name}(${bind.params().joinToString()})"
                val p0 = bind.implementation!!.registerCount - bind.params().sumOf { if (it == "J" || it == "D") 2 else 1 } - 1
                val insns = bind.instructions()
                val gone = insns.getOrNull(0)
                check(gone?.opcode == Opcode.CONST_16 && (gone as OneRegisterInstruction).registerA == p0 + 1 &&
                    (gone as NarrowLiteralInstruction).narrowLiteral == 8) { "$label does not start with const/16 p1, 0x8" }
                val set = insns.getOrNull(1)
                check(set?.opcode == Opcode.INVOKE_VIRTUAL_RANGE && set.call()?.name == "setVisibility" &&
                    (set as RegisterRangeInstruction).startRegister == p0 && set.registerCount == 2) {
                    "$label does not call setVisibility(p0, GONE); got ${set?.opcode} ${set?.call()}"
                }
                check(insns.getOrNull(2)?.opcode == Opcode.RETURN_VOID) { "$label does not return after hiding" }
                if (bannerHolder in bind.params()) banner++
                if (bind.params().any { it in nativeHolders }) native++
            }
        }
        check(banner >= 3 && native >= 4) { "only $banner banner and $native native bind methods patched" }
        // AdsSwitchView's position helper c(I)V is left alone.
        val position = switchView.methods.single { it.returnType == "V" && it.params() == listOf("I") }
        check(position.instructions().first().opcode != Opcode.CONST_16) { "AdsSwitchView.${position.name}(I) was changed" }

        // Interstitials: the manager's "featureInterstitialAd" check returns false, so placements
        // run the user's action at once.
        val (gateClass, gate) = classes.singleMethod("interstitial ads check") { _, m ->
            m.returnType == "Z" && m.parameterTypes.isEmpty() && "featureInterstitialAd" in m.strings()
        }
        assertForcedBoolean(gate, expected = false, label = "${gateClass.type}.${gate.name}() (featureInterstitialAd)")

        // Ads V2 placements: the (AdPlacement, continuation) check returns Boolean.FALSE at once, which
        // also drops the call log's "Upgrade for an ad-free experience" placeholder row.
        val (placementClass, placement) = classes.singleMethod("Ads V2 placement check") { _, m ->
            m.returnType == "Ljava/lang/Object;" && m.params().size == 2 && m.params().first() == AD_PLACEMENT &&
                "featureAdsV2OnClList" in m.strings()
        }
        val placementInsns = placement.instructions()
        val no = placementInsns.getOrNull(0)
        check(no?.opcode == Opcode.SGET_OBJECT && no.field()?.definingClass == "Ljava/lang/Boolean;" && no.field()?.name == "FALSE" &&
            placementInsns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT &&
            (placementInsns[1] as OneRegisterInstruction).registerA == (no as OneRegisterInstruction).registerA
        ) { "${placementClass.type}.${placement.name}(AdPlacement, ...) does not return Boolean.FALSE at once; got ${no?.opcode} ${no?.field()}" }

        // The master "ads allowed" check (the only ()Z method reading both strings) returns false.
        val (allowedClass, allowed) = classes.singleMethod("ads allowed check") { _, m ->
            m.returnType == "Z" && m.parameterTypes.isEmpty() && m.strings().containsAll(listOf("qaForceAds", "adsDisabledUntil"))
        }
        assertForcedBoolean(allowed, expected = false, label = "${allowedClass.type}.${allowed.name}() (ads allowed)")

        // Only the app-open loading screen still finishes right after super.onCreate; the
        // interstitial activities, which deliver the pending action or reward, are untouched.
        for ((type, finishes) in listOf(
            "Lcom/truecaller/ads/interstitial/AdAppOpeningActivity;" to true,
            "Lcom/truecaller/ads/interstitial/AdInterstitialActivity;" to false,
            "Lcom/truecaller/ads/adsrouter/ui/interstitial/AdRouterInterstitialActivity;" to false,
        )) {
            val insns = require(type).method("onCreate", listOf("Landroid/os/Bundle;")).instructions()
            val superCall = insns.indexOfFirst { it.opcode == Opcode.INVOKE_SUPER && it.call()?.name == "onCreate" }
            check(superCall >= 0) { "$type.onCreate does not call super.onCreate" }
            val finish = insns.getOrNull(superCall + 1)
            val finished = finish?.opcode == Opcode.INVOKE_VIRTUAL_RANGE && finish.call()?.name == "finish" &&
                finish.call()?.definingClass == "Landroid/app/Activity;"
            check(finished == finishes) {
                "$type.onCreate ${if (finishes) "does not finish()" else "still finishes"} right after super.onCreate; got ${finish?.opcode} ${finish?.call()}"
            }
            if (finishes) {
                check(insns.getOrNull(superCall + 2)?.opcode == Opcode.RETURN_VOID) { "$type.onCreate does not return after finish()" }
            }
        }
    }

    private fun assertDisableTelemetry(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        val tracker = byType["Lcom/truecaller/analytics/technical/AppStartTracker;"] ?: error("AppStartTracker not found")
        assertReturnsEarlyVoid(tracker.method("enableTracking", emptyList()), label = "AppStartTracker.enableTracking()")

        val cleverTap = classes.filter { "Lcom/truecaller/clevertap/CleverTapManager;" in it.interfaces }
        check(cleverTap.isNotEmpty()) { "no CleverTapManager implementation found" }
        for (cls in cleverTap) {
            assertReturnsEarlyVoid(cls.method("push", listOf("Ljava/lang/String;")), label = "${cls.type}.push(String)")
            assertReturnsEarlyVoid(
                cls.method("push", listOf("Ljava/lang/String;", "Ljava/util/Map;")),
                label = "${cls.type}.push(String, Map)",
            )
        }

        val appsFlyer = classes.filter { it.superclass == "Lcom/appsflyer/AppsFlyerLib;" }
        check(appsFlyer.isNotEmpty()) { "no AppsFlyerLib subclass found" }
        for (cls in appsFlyer) {
            val starts = cls.methods.filter { it.name == "start" && it.params().firstOrNull() == "Landroid/content/Context;" }
            check(starts.any { it.params().size == 1 }) { "${cls.type} has no start(Context)" }
            starts.forEach { assertReturnsEarlyVoid(it, label = "${cls.type}.start(${it.params().joinToString()})") }
        }
    }

    private fun assertDisableUpdatePrompts(classes: List<ClassDef>) {
        val manager = classes.single { cls ->
            cls.methods.any {
                it.returnType == "Ljava/util/concurrent/CompletableFuture;" &&
                    it.params() == listOf("Landroid/app/Activity;", UPDATE_TRIGGER)
            }
        }
        val start = manager.methods.single {
            it.returnType == "Ljava/lang/Object;" && it.params().size == 3 &&
                it.params().take(2) == listOf("Landroid/app/Activity;", UPDATE_TRIGGER)
        }
        val insns = start.instructions()
        val value = insns.getOrNull(0)?.field()
        check(insns.getOrNull(0)?.opcode == Opcode.SGET_OBJECT && value?.definingClass == "Ljava/lang/Boolean;" && value.name == "FALSE") {
            "${manager.type}.${start.name} does not load Boolean.FALSE first; got ${insns.getOrNull(0)?.opcode} $value"
        }
        check(insns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT) { "${manager.type}.${start.name} does not return at once" }

        // The stored server forced-update state always reads NONE.
        val (typeOwner, typeGetter) = classes.singleMethod("forced update type getter") { _, m ->
            m.returnType == "Lcom/truecaller/forcedupdate/UpdateType;" && m.parameterTypes.isEmpty() &&
                "forcedUpdate_updateType" in m.strings()
        }
        val typeInsns = typeGetter.instructions()
        val type = typeInsns.getOrNull(0)?.field()
        check(typeInsns.getOrNull(0)?.opcode == Opcode.SGET_OBJECT && type?.name == "NONE" &&
            type.definingClass == "Lcom/truecaller/forcedupdate/UpdateType;" &&
            typeInsns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT
        ) { "${typeOwner.type}.${typeGetter.name}() does not return UpdateType.NONE at once; got ${typeInsns.getOrNull(0)?.opcode} $type" }
    }

    private fun assertFeatureToggles(classes: List<ClassDef>) {
        for (key in listOf("featureCallAssistant", "featureScamFeed")) {
            val (cls, toggle) = classes.singleMethod("$key toggle") { _, m ->
                m.returnType == "Z" && m.parameterTypes.isEmpty() && key in m.strings()
            }
            assertForcedBoolean(toggle, expected = false, label = "${cls.type}.${toggle.name}() ($key)")
        }
    }

    private fun assertHiddenTabs(classes: List<ClassDef>) {
        // The bottom bar builder calls one helper per tab patch on the tab array right before
        // filterNotNull; together they null PREMIUM, BLOCKING, FAMILY_PROTECTION_CONFIG and MESSAGES.
        val (builderClass, builder) = classes.singleMethod("bottom bar builder") { _, m ->
            m.returnType == "V" && m.params() == listOf("Landroid/util/SparseIntArray;") &&
                m.instructions().any { it.opcode == Opcode.SGET_OBJECT && it.field()?.definingClass == BOTTOM_BAR_BUTTON_TYPE && it.field()?.name == "CALLS" }
        }
        val label = "${builderClass.type}.${builder.name}(SparseIntArray)"
        val insns = builder.instructions()
        val filter = insns.indexOfFirst {
            it.opcode == Opcode.INVOKE_STATIC && it.call()?.returnType == "Ljava/util/List;" &&
                it.call()?.parameterTypes?.map(CharSequence::toString) == listOf("[Ljava/lang/Object;")
        }
        check(filter >= 3) { "$label has no filterNotNull call after the helpers" }
        val array = (insns[filter] as FiveRegisterInstruction).registerC
        val arrayType = insns.take(filter).last { it.opcode == Opcode.NEW_ARRAY && (it as TwoRegisterInstruction).registerA == array }
            .let { ((it as ReferenceInstruction).reference as TypeReference).type }
        val hidden = mutableSetOf<String>()
        for (call in insns.subList(filter - 3, filter)) {
            val ref = call.call()
            check(call.opcode == Opcode.INVOKE_STATIC_RANGE && ref?.definingClass == builderClass.type &&
                ref.parameterTypes.map(CharSequence::toString) == listOf(arrayType) &&
                (call as RegisterRangeInstruction).startRegister == array && call.registerCount == 1
            ) { "$label does not pass the tab array v$array to a helper before filterNotNull; got ${call.opcode} $ref" }
            val helper = builderClass.method(ref.name, listOf(arrayType))
            val helperInsns = helper.instructions()
            check(helperInsns.any { it.opcode == Opcode.APUT_OBJECT }) { "${builderClass.type}.${helper.name} does not null array entries" }
            check(helperInsns.any { it.call()?.returnType == BOTTOM_BAR_BUTTON_TYPE && it.call()?.definingClass == arrayType.removePrefix("[") }) {
                "${builderClass.type}.${helper.name} does not read the tab type"
            }
            hidden += helperInsns.filter { it.opcode == Opcode.SGET_OBJECT && it.field()?.definingClass == BOTTOM_BAR_BUTTON_TYPE }
                .map { it.field()!!.name }
        }
        check(hidden == setOf("PREMIUM", "BLOCKING", "FAMILY_PROTECTION_CONFIG", "MESSAGES")) { "$label hides $hidden" }
    }

    private fun assertHideMessagesTab(classes: List<ClassDef>) {
        val (builderClass, builder) = classes.singleMethod("bottom bar builder") { _, m ->
            m.returnType == "V" && m.params() == listOf("Landroid/util/SparseIntArray;") &&
                m.instructions().any { it.opcode == Opcode.SGET_OBJECT && it.field()?.definingClass == BOTTOM_BAR_BUTTON_TYPE && it.field()?.name == "CALLS" }
        }
        fun Instruction.isTabType(name: String) =
            opcode == Opcode.SGET_OBJECT && field()?.definingClass == BOTTOM_BAR_BUTTON_TYPE && field()?.name == name

        // The "default_tab_on_launch" getter returns CALLS at once.
        val defaultTab = builderClass.methods.single {
            it.returnType == BOTTOM_BAR_BUTTON_TYPE && it.parameterTypes.isEmpty() && "default_tab_on_launch" in it.strings()
        }
        val defaultInsns = defaultTab.instructions()
        check(defaultInsns.getOrNull(0)?.isTabType("CALLS") == true && defaultInsns.getOrNull(1)?.opcode == Opcode.RETURN_OBJECT &&
            (defaultInsns[1] as OneRegisterInstruction).registerA == (defaultInsns[0] as OneRegisterInstruction).registerA
        ) { "${builderClass.type}.${defaultTab.name}() (default tab) does not return CALLS at once" }

        // selectTab(type, Z) swaps MESSAGES for CALLS before its stock code.
        val select = builderClass.methods.single {
            it.returnType == "V" && it.params() == listOf(BOTTOM_BAR_BUTTON_TYPE, "Z") &&
                it.instructions().any { insn -> insn.call()?.name == "setSelectedItemId" }
        }
        val selectLabel = "${builderClass.type}.${select.name}(BottomBarButtonType, Z)"
        val selectInsns = select.instructions()
        val p1 = select.implementation!!.registerCount - 2 // the type, then Z
        check(selectInsns.getOrNull(0)?.isTabType("MESSAGES") == true) { "$selectLabel does not load MESSAGES first" }
        val compare = selectInsns[1]
        check(compare.opcode == Opcode.IF_NE && (compare as TwoRegisterInstruction).registerA == p1 &&
            compare.registerB == (selectInsns[0] as OneRegisterInstruction).registerA && selectInsns.branchTarget(1) == 3
        ) { "$selectLabel does not skip the swap for other tabs" }
        check(selectInsns[2].isTabType("CALLS") && (selectInsns[2] as OneRegisterInstruction).registerA == p1) {
            "$selectLabel does not replace MESSAGES with CALLS"
        }
        check(selectInsns.drop(3).any { it.call()?.name == "setSelectedItemId" }) { "$selectLabel lost its stock code" }

        // Right after the BottomBarView cast, the builder passes the bar and the final tab list to a
        // helper that keeps the bar visible (it pads for the navigation bar, issue #61) and, with
        // fewer than two tabs, hides its tab menu and drops its minimum height (saved in a new field).
        val insns = builder.instructions()
        val take = insns.indexOfFirst {
            it.call()?.definingClass == "Lkotlin/collections/CollectionsKt;" &&
                it.call()?.parameterTypes?.map(CharSequence::toString) == listOf("Ljava/lang/Iterable;", "I")
        }
        val cast = insns.indexOfFirst {
            it.opcode == Opcode.CHECK_CAST && ((it as ReferenceInstruction).reference as TypeReference).type == BOTTOM_BAR_VIEW
        }
        check(take >= 0 && cast > take) { "bottom bar builder has no take() before its BottomBarView cast" }
        val list = (insns[take + 1] as OneRegisterInstruction).registerA
        val call = insns[cast + 1]
        val ref = call.call()
        check(call.opcode == Opcode.INVOKE_STATIC && ref?.definingClass == builderClass.type &&
            ref.parameterTypes.map(CharSequence::toString) == listOf(BOTTOM_BAR_VIEW, "Ljava/util/List;") &&
            (call as FiveRegisterInstruction).registerC == (insns[cast] as OneRegisterInstruction).registerA && call.registerD == list
        ) { "bottom bar builder does not pass the bar and the tab list to a helper after the cast; got ${call.opcode} $ref" }
        val helperLabel = "${builderClass.type}.${ref.name}"
        val helper = builderClass.method(ref.name, listOf(BOTTOM_BAR_VIEW, "Ljava/util/List;")).instructions()
        val bar = 4 // p0 of the 6-register helper
        check(helper.getOrNull(0)?.opcode == Opcode.CONST_4 && (helper[0] as NarrowLiteralInstruction).narrowLiteral == 0 &&
            helper.getOrNull(1)?.call()?.name == "setVisibility" && (helper[1] as FiveRegisterInstruction).registerC == bar &&
            (helper[1] as FiveRegisterInstruction).registerD == (helper[0] as OneRegisterInstruction).registerA
        ) { "$helperLabel does not keep the bar VISIBLE first" }
        val barVisibility = helper.drop(2).filter { it.call()?.name == "setVisibility" }
        check(barVisibility.size == 1 && (barVisibility.single() as FiveRegisterInstruction).registerC != bar) {
            "$helperLabel changes the bar's visibility again instead of its children's"
        }
        check(helper.any { it.call()?.name == "size" && it.call()?.definingClass == "Ljava/util/List;" } &&
            helper.any { it.opcode == Opcode.IF_LT } &&
            helper.any { it.call()?.name == "getChildAt" && it.call()?.definingClass == "Landroid/view/ViewGroup;" } &&
            helper.any { it.opcode == Opcode.CONST_16 && (it as NarrowLiteralInstruction).narrowLiteral == 8 }
        ) { "$helperLabel does not hide the bar's children below two tabs" }
        check(helper.count { it.call()?.name == "setMinimumHeight" && (it as FiveRegisterInstruction).registerC == bar } == 2 &&
            helper.any { it.call()?.name == "getMinimumHeight" }
        ) { "$helperLabel does not drop and restore the bar's minimum height" }
        val savedField = helper.mapNotNull { it.field() }.distinct().singleOrNull()
        check(savedField?.definingClass == BOTTOM_BAR_VIEW && savedField.type == "Ljava/lang/Integer;" &&
            helper.any { it.opcode == Opcode.IPUT_OBJECT } && helper.any { it.opcode == Opcode.IGET_OBJECT }
        ) { "$helperLabel does not save the minimum height on the bar; got $savedField" }
        val barClass = classes.single { it.type == BOTTOM_BAR_VIEW }
        check(barClass.fields.any { it.name == savedField.name && it.type == savedField.type && AccessFlags.PUBLIC.isSet(it.accessFlags) }) {
            "$BOTTOM_BAR_VIEW has no public ${savedField.name} field"
        }
    }

    private fun assertHiddenSettingsRows(classes: List<ClassDef>) {
        // The Settings screen state's toString reads isPremiumItemVisible and isWatchItemVisible
        // first, in that order ("Hide Premium tab" and "Hide Wear OS settings" zero both).
        val (stateClass, toString) = classes.singleMethod("CategoriesSettingsState.toString") { _, m ->
            m.name == "toString" && m.parameterTypes.isEmpty() && m.strings().any { it.startsWith("CategoriesSettingsState(") }
        }
        check(toString.strings().containsAll(listOf("CategoriesSettingsState(isPremiumItemVisible=", ", isWatchItemVisible="))) {
            "${stateClass.type}.toString lost the premium/watch labels: ${toString.strings()}"
        }
        val flags = toString.instructions().filter { it.opcode == Opcode.IGET_BOOLEAN && it.field()?.definingClass == stateClass.type }
            .map { it.field()!!.name }
        check(flags.size >= 3) { "${stateClass.type}.toString reads only $flags" }
        val hidden = flags.take(2).toSet() // isPremiumItemVisible, isWatchItemVisible

        val constructors = stateClass.methods.filter { it.name == "<init>" && it.implementation != null }
        check(constructors.isNotEmpty()) { "${stateClass.type} has no constructor" }
        for (constructor in constructors) {
            val label = "${stateClass.type}.<init>(${constructor.params().joinToString()})"
            val insns = constructor.instructions()
            val zeroed = insns.indices.filter { insns[it].opcode == Opcode.IPUT_BOOLEAN && insns[it].field()?.definingClass == stateClass.type }
                .associate { index ->
                    val store = insns[index] as TwoRegisterInstruction
                    val previous = insns.getOrNull(index - 1)
                    store.field()!!.name to (previous?.opcode == Opcode.CONST_16 && (previous as OneRegisterInstruction).registerA == store.registerA &&
                        (previous as NarrowLiteralInstruction).narrowLiteral == 0)
                }
            check(zeroed.filterValues { it }.keys == hidden) { "$label zeroes ${zeroed.filterValues { it }.keys}, expected $hidden" }
        }
    }

    private fun assertToggleOverrides(classes: List<ClassDef>) {
        // The SharedPreferences toggle resolver starts with one "key.equals(p1) -> return value"
        // block per override, each falling through to the next and finally to the stock code.
        // Hide family button and Restore classic call log add one each, Disable ads four.
        val (cls, resolver) = classes.singleMethod("feature toggle resolver") { _, m ->
            m.returnType == "Z" && m.params() == listOf("Ljava/lang/String;", FEATURE_STATE) &&
                m.instructions().any { it.call()?.definingClass == "Landroid/content/SharedPreferences;" && it.call()?.name == "getBoolean" }
        }
        val label = "${cls.type}.${resolver.name}(String, FeatureState)"
        val insns = resolver.instructions()
        val keyRegister = resolver.implementation!!.registerCount - 2 // this + key + state
        val expected = mapOf(
            "featureFamilyProtect" to 0,
            "featureCallLogRevamp" to 0,
            "featureAdsOptimisationOnListView" to 1,
            "featureAdsRestrictCampaignProcessing" to 1,
            "featureConfigManagementSystem" to 0,
            "featureEnableOfflineAds" to 0,
        )
        val overrides = mutableMapOf<String, Int>()
        for (block in expected.keys.indices) {
            val start = block * 6
            val key = insns.getOrNull(start)?.stringOrNull() ?: error("$label: override block $block has no key")
            val equals = insns[start + 1]
            check(equals.opcode == Opcode.INVOKE_VIRTUAL && equals.call()?.definingClass == "Ljava/lang/String;" &&
                equals.call()?.name == "equals" && (equals as FiveRegisterInstruction).registerD == keyRegister
            ) { "$label: $key is not compared with p1" }
            check(insns[start + 2].opcode == Opcode.MOVE_RESULT && insns[start + 3].opcode == Opcode.IF_EQZ) { "$label: $key has no branch" }
            check(insns.branchTarget(start + 3) == start + 6) { "$label: a non-matching $key does not fall through" }
            val value = insns[start + 4]
            check(value.opcode == Opcode.CONST_4 && insns[start + 5].opcode == Opcode.RETURN) { "$label: $key does not return a constant" }
            overrides[key] = (value as NarrowLiteralInstruction).narrowLiteral
        }
        check(overrides == expected) { "$label overrides $overrides, expected $expected" }
        // Nothing past the override blocks is another override: the stock code follows.
        val next = insns.getOrNull(expected.size * 6)
        check(next?.opcode != Opcode.CONST_STRING || insns.getOrNull(expected.size * 6 + 1)?.call()?.name != "equals") {
            "$label has more than ${expected.size} override blocks"
        }
    }

    private fun assertUseSmsVerification(classes: List<ClassDef>) {
        // The selection branch now loads SMS where it loaded GOOGLE and returns it. Located by its
        // check-cast to OtpSmsApi right before, since the GOOGLE read the patch anchored on is gone.
        val selections = classes.methodsWhere { _, m ->
            m.returnType == "Ljava/lang/Object;" && m.parameterTypes.isEmpty() &&
                m.instructions().windowed(3).any { (cast, sms, ret) ->
                    cast.opcode == Opcode.CHECK_CAST &&
                        ((cast as ReferenceInstruction).reference as TypeReference).type == OTP_SMS_API &&
                        sms.opcode == Opcode.SGET_OBJECT && sms.field()?.definingClass == OTP_SMS_API && sms.field()?.name == "SMS" &&
                        ret.opcode == Opcode.RETURN_OBJECT &&
                        (ret as OneRegisterInstruction).registerA == (sms as OneRegisterInstruction).registerA
                }
        }
        check(selections.size == 1) { "expected one OtpSmsApi selection returning SMS, found ${selections.map { (c, m) -> "${c.type}.${m.name}" }}" }
        val (cls, method) = selections.single()
        check(method.instructions().none { it.opcode == Opcode.SGET_OBJECT && it.field()?.definingClass == OTP_SMS_API && it.field()?.name == "GOOGLE" }) {
            "${cls.type}.${method.name} still compares with OtpSmsApi.GOOGLE"
        }
    }

    private fun assertFixT9ContactSearch(classes: List<ClassDef>, byType: Map<String, ClassDef>) {
        // The RebuildAll helper and its incremental copy share the shape the patch anchors on.
        val helpers = classes.methodsWhere { _, m ->
            m.returnType == "V" && m.params() == listOf("Landroid/content/Context;") && AccessFlags.STATIC.isSet(m.accessFlags) &&
                m.strings().containsAll(listOf(T9_REBUILD_ALL_KEY, "RefreshT9MappingWorker"))
        }
        val (cloneClass, clone) = helpers.singleOrNull { (_, m) -> m.name == T9_INCREMENTAL_HELPER }
            ?: error("expected one $T9_INCREMENTAL_HELPER, found ${helpers.map { (c, m) -> "${c.type}.${m.name}" }}")
        val (helperClass, helper) = helpers.singleOrNull { (_, m) -> m.name != T9_INCREMENTAL_HELPER }
            ?: error("expected one RefreshT9MappingWorker helper, found ${helpers.map { (c, m) -> "${c.type}.${m.name}" }}")
        check(cloneClass.type == helperClass.type) { "$T9_INCREMENTAL_HELPER is in ${cloneClass.type}, not ${helperClass.type}" }
        val label = "${cloneClass.type}.$T9_INCREMENTAL_HELPER(Context)"
        check(AccessFlags.PUBLIC.isSet(clone.accessFlags) && AccessFlags.STATIC.isSet(clone.accessFlags)) { "$label is not public static" }

        fun Method.booleanLoads(name: String) = instructions().withIndex().filter { (_, insn) ->
            insn.opcode == Opcode.SGET_OBJECT && insn.field()?.definingClass == "Ljava/lang/Boolean;" && insn.field()?.name == name
        }.map { it.index }

        // The original still enqueues RebuildAll = TRUE; the copy is identical except for FALSE there.
        val original = helper.booleanLoads("TRUE")
        check(original.size == 1 && helper.booleanLoads("FALSE").isEmpty()) {
            "${helperClass.type}.${helper.name} (RebuildAll helper) was changed: TRUE at $original"
        }
        check(clone.booleanLoads("FALSE") == original && clone.booleanLoads("TRUE").isEmpty()) {
            "$label does not load Boolean.FALSE in place of TRUE; FALSE at ${clone.booleanLoads("FALSE")}, TRUE at ${clone.booleanLoads("TRUE")}"
        }
        check(clone.instructions().map { it.opcode } == helper.instructions().map { it.opcode } && clone.strings() == helper.strings()) {
            "$label is not a copy of ${helperClass.type}.${helper.name}"
        }

        // TruecallerInit.onCreate calls the copy with p0 right after super.onCreate.
        val onCreate = (byType[TRUECALLER_INIT] ?: error("$TRUECALLER_INIT not found")).method("onCreate", listOf("Landroid/os/Bundle;"))
        val insns = onCreate.instructions()
        val superCall = insns.indexOfFirst {
            (it.opcode == Opcode.INVOKE_SUPER || it.opcode == Opcode.INVOKE_SUPER_RANGE) && it.call()?.name == "onCreate"
        }
        check(superCall >= 0) { "TruecallerInit.onCreate does not call super.onCreate" }
        val call = insns.getOrNull(superCall + 1)
        val p0 = onCreate.implementation!!.registerCount - 2 // this + Bundle
        check(call?.opcode == Opcode.INVOKE_STATIC_RANGE && call.call()?.definingClass == helperClass.type &&
            call.call()?.name == T9_INCREMENTAL_HELPER && (call as RegisterRangeInstruction).startRegister == p0 && call.registerCount == 1
        ) { "TruecallerInit.onCreate does not call $label with p0 after super.onCreate; got ${call?.opcode} ${call?.call()}" }
    }
}
