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
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

// Assertions for the shared failFlutterGmaPluginLoads() (shared/misc/ads/FlutterGmaPluginLoads.kt):
// every google_mobile_ads plugin load reports NO_FILL to Dart. The plugin is found the way the
// patch finds it (channel strings and method shapes), so this works whether R8 renamed it or not.

private const val STRING = "Ljava/lang/String;"
private const val METHOD_CALL = "Lio/flutter/plugin/common/MethodCall;"
private const val METHOD_RESULT = "Lio/flutter/plugin/common/MethodChannel\$Result;"
private const val FAIL_LOAD_HELPER = "morpheFailLoad"
private val LOAD_METHOD_NAMES = listOf(
    "loadBannerAd", "loadAdManagerBannerAd", "loadFluidAd", "loadNativeAd", "loadInterstitialAd",
    "loadAdManagerInterstitialAd", "loadRewardedAd", "loadRewardedInterstitialAd", "loadAppOpenAd",
)

private fun Instruction.methodRef(): MethodReference? = (this as? ReferenceInstruction)?.reference as? MethodReference
private fun Instruction.fieldRef(): FieldReference? = (this as? ReferenceInstruction)?.reference as? FieldReference
private fun Instruction.typeRef(): String? = ((this as? ReferenceInstruction)?.reference as? TypeReference)?.type
private fun Instruction.stringRef(): String? = ((this as? ReferenceInstruction)?.reference as? StringReference)?.string

private fun Instruction.registers(): List<Int> = when (this) {
    is RegisterRangeInstruction -> (startRegister until startRegister + registerCount).toList()
    is FiveRegisterInstruction -> listOf(registerC, registerD, registerE, registerF, registerG).take(registerCount)
    else -> emptyList()
}

private fun Method.strings(): List<String> = instructions().mapNotNull { it.stringRef() }
private fun Method.params(): List<String> = parameterTypes.map(CharSequence::toString)
private fun MethodReference.params(): List<String> = parameterTypes.map(CharSequence::toString)

/** p-register [p] as a v-register index. */
private fun Method.p(p: Int): Int {
    val ins = params().sumOf { if (it == "J" || it == "D") 2 else 1 } + if (AccessFlags.STATIC.isSet(accessFlags)) 0 else 1
    return implementation!!.registerCount - ins + p
}

private fun List<ClassDef>.singleMethod(label: String, predicate: (Method) -> Boolean): Pair<ClassDef, Method> {
    val found = flatMap { c -> c.methods.map { c to it } }.filter { (_, m) -> m.implementation != null && predicate(m) }
    return found.singleOrNull() ?: error("expected one $label, found ${found.map { "${it.first.type}->${it.second.name}" }}")
}

/**
 * Asserts that the plugin's AdInstanceManager gained morpheFailLoad(I)V, which builds
 * FlutterLoadAdError(3, GMA domain, "Ad blocked", null) and hands it to onAdFailedToLoad, and that
 * the load() of every concrete FlutterAd (all nine channel load types) starts by calling it with
 * the ad's adId and returns. Returns the patched load methods by their class.
 */
fun assertFlutterGmaPluginLoadsFail(classes: List<ClassDef>): Map<String, Method> {
    val byType = classes.associateBy { it.type }

    val (managerClass, fail) = classes.singleMethod("AdInstanceManager.onAdFailedToLoad") {
        it.returnType == "V" && it.params().size == 2 && it.params()[0] == "I" &&
            it.strings().containsAll(listOf("adId", "eventName", "onAdFailedToLoad", "loadAdError"))
    }
    val manager = managerClass.type
    val loadAdError = fail.params()[1]
    val (trackClass, track) = classes.singleMethod("AdInstanceManager.trackAd") {
        it.returnType == "V" && it.params().size == 2 && it.params()[1] == "I" &&
            "Ad for following adId already exists: %d" in it.strings()
    }
    assertEquals(manager, trackClass.type, "trackAd and onAdFailedToLoad must share the manager")
    val flutterAd = track.params()[0]
    val adIdField = byType.getValue(flutterAd).fields.single { it.type == "I" && !AccessFlags.STATIC.isSet(it.accessFlags) }

    // The helper: new FlutterLoadAdError(3, domain, "Ad blocked", null); this.onAdFailedToLoad(adId, error).
    val helper = managerClass.methods.singleOrNull {
        it.name == FAIL_LOAD_HELPER && it.params() == listOf("I") && it.returnType == "V"
    } ?: error("$manager has no $FAIL_LOAD_HELPER(I)V")
    val h = helper.instructions()
    assertTrue(
        h.getOrNull(0)?.opcode == Opcode.NEW_INSTANCE && h[0].typeRef() == loadAdError,
        "$FAIL_LOAD_HELPER does not create a $loadAdError first; found ${h.getOrNull(0)?.opcode}",
    )
    val error = (h[0] as OneRegisterInstruction).registerA
    val code = h.getOrNull(1)
    assertTrue(code is NarrowLiteralInstruction && code.narrowLiteral == 3, "$FAIL_LOAD_HELPER does not load error code 3 (NO_FILL)")
    assertEquals("com.google.android.gms.ads", h.getOrNull(2)?.stringRef(), "$FAIL_LOAD_HELPER error domain")
    assertEquals("Ad blocked", h.getOrNull(3)?.stringRef(), "$FAIL_LOAD_HELPER error message")
    val info = h.getOrNull(4)
    assertTrue(info is NarrowLiteralInstruction && info.narrowLiteral == 0, "$FAIL_LOAD_HELPER does not pass a null ResponseInfo")
    val init = h.getOrNull(5)
    val initRef = init?.methodRef()
    assertTrue(
        init?.opcode == Opcode.INVOKE_DIRECT_RANGE && initRef?.definingClass == loadAdError && initRef.name == "<init>" &&
            initRef.params().take(3) == listOf("I", STRING, STRING) && initRef.params().size == 4 &&
            init.registers() == listOf(
                error, (code as OneRegisterInstruction).registerA, (h[2] as OneRegisterInstruction).registerA,
                (h[3] as OneRegisterInstruction).registerA, (info as OneRegisterInstruction).registerA,
            ),
        "$FAIL_LOAD_HELPER does not construct the error from code, domain, message and null; found ${init?.opcode} $initRef",
    )
    val report = h.getOrNull(6)
    assertTrue(
        report?.opcode == Opcode.INVOKE_VIRTUAL && report.methodRef()?.let {
            it.definingClass == manager && it.name == fail.name && it.params() == fail.params()
        } == true && report.registers() == listOf(helper.p(0), helper.p(1), error),
        "$FAIL_LOAD_HELPER does not call $manager->${fail.name}(adId, error) on itself; found ${report?.opcode} ${report?.methodRef()}",
    )
    assertTrue(h.getOrNull(7)?.opcode == Opcode.RETURN_VOID, "$FAIL_LOAD_HELPER does not return after reporting")

    // The load methods, as the patch finds them: onMethodCall's trackAd(ad, adId) calls, each
    // followed by a no-arg load on the ad. One per load* channel method.
    val (_, onMethodCall) = classes.singleMethod("GoogleMobileAdsPlugin.onMethodCall") {
        it.returnType == "V" && it.params() == listOf(METHOD_CALL, METHOD_RESULT) &&
            it.strings().containsAll(LOAD_METHOD_NAMES + "MobileAds#initialize")
    }
    val calls = onMethodCall.instructions()
    val loadRefs = calls.indices.mapNotNull { index ->
        val ref = calls[index].methodRef()
        if (ref?.definingClass != manager || ref.name != track.name || ref.params() != track.params()) return@mapNotNull null
        val ad = calls[index].registers()[1]
        val next = (index + 1 until calls.size).first { calls[it].methodRef() != null }
        val load = calls[next].methodRef()!!
        assertTrue(
            load.params().isEmpty() && load.returnType == "V" && calls[next].registers() == listOf(ad),
            "trackAd at $index is not followed by a no-arg load on the ad; found $load",
        )
        load
    }
    assertEquals(LOAD_METHOD_NAMES.size, loadRefs.size, "trackAd + load calls in onMethodCall")

    fun chain(type: String) = generateSequence(type) { byType[it]?.superclass }.takeWhile { it != flutterAd }
    fun isFlutterAd(type: String) = generateSequence(type) { byType[it]?.superclass }.any { it == flutterAd }
    val loads = loadRefs.map { ref ->
        val owner = chain(ref.definingClass).first { type ->
            byType.getValue(type).methods.any { it.name == ref.name && it.parameterTypes.isEmpty() && it.implementation != null }
        }
        owner to byType.getValue(owner).method(ref.name, emptyList())
    }.distinctBy { it.first }.toMap()
    val adClasses = classes.filter { it.type != flutterAd && isFlutterAd(it.type) && !AccessFlags.ABSTRACT.isSet(it.accessFlags) }
    assertEquals(LOAD_METHOD_NAMES.size, adClasses.size, "concrete FlutterAd classes: ${adClasses.map { it.type }}")
    // Like the patch: the first class in each ad's chain (itself first) that defines a concrete
    // load must be a patched owner, so no subclass overrides a patched load.
    val loadNames = loads.values.map { it.name }.toSet()
    adClasses.forEach { ad ->
        val resolved = loadNames.mapNotNull { name ->
            chain(ad.type).firstOrNull { type ->
                byType.getValue(type).methods.any { it.name == name && it.parameterTypes.isEmpty() && it.implementation != null }
            }?.let { it to name }
        }
        assertTrue(
            resolved.isNotEmpty() && resolved.all { (owner, name) -> loads[owner]?.name == name },
            "${ad.type} does not run a patched load method; its loads resolve to $resolved",
        )
    }

    // Every load: iget-object manager; if-eqz -> return-void; iget adId; manager.morpheFailLoad(adId); return-void.
    loads.forEach { (owner, load) ->
        val label = "$owner->${load.name}()V"
        val insns = load.instructions()
        val getManager = insns.getOrNull(0)
        assertTrue(
            getManager?.opcode == Opcode.IGET_OBJECT && getManager.fieldRef()?.type == manager &&
                (getManager as TwoRegisterInstruction).registerB == load.p(0),
            "$label does not read its $manager field first; found ${getManager?.opcode} ${getManager?.fieldRef()}",
        )
        val managerRegister = (getManager as OneRegisterInstruction).registerA
        val guard = insns.getOrNull(1)
        assertTrue(
            guard?.opcode == Opcode.IF_EQZ && (guard as OneRegisterInstruction).registerA == managerRegister,
            "$label does not null-check the manager; found ${guard?.opcode}",
        )
        val getId = insns.getOrNull(2)
        assertTrue(
            getId?.opcode == Opcode.IGET && getId.fieldRef()?.let { it.definingClass == flutterAd && it.name == adIdField.name } == true &&
                (getId as TwoRegisterInstruction).registerB == load.p(0),
            "$label does not read FlutterAd's adId; found ${getId?.opcode} ${getId?.fieldRef()}",
        )
        val callHelper = insns.getOrNull(3)
        assertTrue(
            callHelper?.opcode == Opcode.INVOKE_VIRTUAL &&
                callHelper.methodRef()?.let { it.definingClass == manager && it.name == FAIL_LOAD_HELPER } == true &&
                callHelper.registers() == listOf(managerRegister, (getId as OneRegisterInstruction).registerA),
            "$label does not call $FAIL_LOAD_HELPER(adId) on the manager; found ${callHelper?.opcode} ${callHelper?.methodRef()}",
        )
        assertTrue(insns.getOrNull(4)?.opcode == Opcode.RETURN_VOID, "$label does not return after failing the ad")
        val addresses = insns.runningFold(0) { address, insn -> address + insn.codeUnits }
        assertTrue(
            addresses[1] + (guard as OffsetInstruction).codeOffset == addresses[4],
            "$label: the null-manager branch does not jump to the prologue's return-void",
        )
    }
    return loads
}
