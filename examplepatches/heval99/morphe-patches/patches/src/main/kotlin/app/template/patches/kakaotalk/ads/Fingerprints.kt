// Ported from AmpleReVanced/revanced-patches (GPL-3.0), patches/kakaotalk/ad/fingerprints, written
// for KakaoTalk 26.7.2 on morphe-patcher 1.11.0. Changes for 26.8.3 on morphe-patcher 1.8.0: the
// hasMethodCall() custom checks became methodCall filters, the NativeAdLoader request overload was
// added, and the FriendListChipBizBoard fingerprint was dropped (its opcode pattern no longer matches).

package app.template.patches.kakaotalk.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// The app classes are R8-renamed but keep their Kotlin source file names, which the
// fingerprints below use. The AdFit SDK (com.kakao.adfit.ads.*) keeps its public API names.

internal const val NATIVE_AD_LOADER = "Lcom/kakao/adfit/ads/media/NativeAdLoader;"
internal const val FEED_AD_LAYOUT = "Lcom/kakao/adfit/ads/feed/FeedAdLayout;"
internal const val BIZ_BOARD_FRAME_LAYOUT = "Lcom/kakao/talk/widget/ad/BizBoardFrameLayout;"

// AdFit native ads: NativeAdLoader.load(count, listener) and its request-based overload, which
// the chat list, OpenChat and the app's AdFitNativeLoader use.
internal object LoadNativeAdFingerprint : Fingerprint(
    name = "load",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("I", "Lcom/kakao/adfit/ads/media/NativeAdLoader\$AdLoadListener;"),
    returnType = "Z",
    strings = listOf(
        " owner is destroyed.",
        " loading is already started.",
        "Request Native AD",
        "Native ad is cached. [id = ",
    ),
)

internal object LoadNativeAdWithRequestFingerprint : Fingerprint(
    definingClass = NATIVE_AD_LOADER,
    name = "load",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/kakao/adfit/ads/media/NativeAdRequest;",
        "Lcom/kakao/adfit/ads/media/NativeAdLoader\$AdLoadWithRequestListener;",
    ),
    returnType = "Z",
)

// AdFit focus (full-screen) ads: the FocusAdLoader implementation's loadAd(listener).
internal object LoadFocusAdFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Lcom/kakao/adfit/ads/focus/FocusAdLoader\$OnAdLoadListener;"),
    returnType = "Z",
    strings = listOf(
        " owner is destroyed.",
        " loading is already started.",
        "Request Focus AD",
        " loading is started.",
        "Focus ad is cached. [id = ",
    ),
)

// OpenChat room BizBoard: the controller's load method, which checks its enabled flag and then
// calls BizBoardAdView.loadAd().
internal object OpenLinkChatAdControllerLoadFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT,
        Opcode.IF_EQZ,
    ) + methodCall(definingClass = "Lcom/kakao/adfit/ads/bizboard/BizBoardAdView;", name = "loadAd", returnType = "V"),
    custom = { _, classDef -> classDef.sourceFile == "OpenLinkChatAdController.kt" },
)

// Chat list ads: the portrait-only native ad check and the suspend global-region ad check.
internal object ChatListAdHelperEnabledFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    filters = listOf(
        methodCall("Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
        fieldAccess("Landroid/content/res/Configuration;->orientation:I", location = MatchAfterImmediately()),
        opcode(Opcode.CONST_4, location = MatchAfterImmediately()),
        opcode(Opcode.IF_NE, location = MatchAfterImmediately()),
        opcode(Opcode.SGET_OBJECT, location = MatchAfterImmediately()),
        opcode(Opcode.INVOKE_VIRTUAL, location = MatchAfterImmediately()),
    ),
    custom = { _, classDef -> classDef.sourceFile == "ChatListAdHelper.kt" },
)

internal object ChatListGlobalAdEnabledFingerprint : Fingerprint(
    classFingerprint = ChatListAdHelperEnabledFingerprint,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "Z", "Lkotlin/coroutines/Continuation;"),
    returnType = "Ljava/lang/Object;",
    filters = listOf(
        methodCall("Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;"),
        opcode(Opcode.MOVE_RESULT_OBJECT, location = MatchAfterImmediately()),
        fieldAccess("Landroid/content/res/Configuration;->orientation:I"),
    ),
    custom = { _, classDef -> classDef.sourceFile == "ChatListAdHelper.kt" },
)

// OpenChat tab feed ad: the view holder's load (FeedAdLoader.loadAd) and render
// (FeedAdLayout.setFeedAd) methods.
internal object OpenChatFeedAdLoadFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.SGET_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
    ) + methodCall(definingClass = "Lcom/kakao/adfit/ads/feed/FeedAdLoader;", name = "loadAd", returnType = "Z"),
    custom = { method, classDef ->
        classDef.sourceFile == "OpenChatTabFeedAdViewHolder.kt" && method.parameterTypes.size == 2
    },
)

internal object OpenChatFeedAdRenderFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_VIRTUAL,
        Opcode.MOVE_RESULT_OBJECT,
        Opcode.CHECK_CAST,
        Opcode.IGET_OBJECT,
    ) + methodCall(definingClass = FEED_AD_LAYOUT, name = "setFeedAd", returnType = "V"),
    custom = { method, classDef ->
        classDef.sourceFile == "OpenChatTabFeedAdViewHolder.kt" && method.parameterTypes.size == 3
    },
)

// KakaoPay banner: the banner state's refresh(Boolean) and the AdFit banner composable.
internal object PayBannerLoadFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Z"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.XOR_INT_LIT8,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    ),
    custom = { _, classDef -> classDef.sourceFile == "PayAdPlatformBannerState.kt" },
)

internal object PayBannerRenderFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Ljava/lang/String;",
        "Lcom/kakao/adfit/ads/media/NativeAdBinder;",
        "L",
        "Lkotlin/jvm/functions/Function1;",
        "Lkotlin/jvm/functions/Function0;",
        "Lkotlin/jvm/functions/Function1;",
        "Lkotlin/jvm/functions/Function0;",
        "L",
        "I",
        "I",
    ),
    returnType = "V",
    custom = { _, classDef -> classDef.sourceFile == "PayAdPlatformAdFit.kt" },
)

// Short-form (shortform viewer) ad page: SftAdFragment's ad load and FeedAd render methods.
internal object ShortFormAdLoadFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf(),
    returnType = "V",
    strings = listOf("feedAdLoader"),
    filters = listOf(methodCall("Lcom/kakao/adfit/ads/feed/FeedAdLoader;->isLoading()Z")),
    custom = { _, classDef -> classDef.sourceFile == "SftAdFragment.kt" },
)

internal object ShortFormAdRenderFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    strings = listOf("feedAdLayout"),
    filters = listOf(methodCall("Lcom/kakao/adfit/ads/feed/FeedAdLayout;->setFeedAd(Lcom/kakao/adfit/ads/feed/FeedAd;)V")),
    custom = { method, classDef -> classDef.sourceFile == "SftAdFragment.kt" && method.parameterTypes.size == 2 },
)

// BizBoard banner (chat list / friends / more tab): the app's frame layout around it, whose
// onMeasure sizes the banner from the width (getSize * ratio, capped at bizBoardMaxHeight).
internal object MeasuringBizBoardFingerprint : Fingerprint(
    definingClass = BIZ_BOARD_FRAME_LAYOUT,
    name = "onMeasure",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("I", "I"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.INT_TO_FLOAT,
        Opcode.CONST,
        Opcode.MUL_FLOAT_2ADDR,
        Opcode.FLOAT_TO_INT,
        Opcode.IGET,
        Opcode.IF_LE,
        Opcode.MOVE,
        Opcode.CONST_HIGH16,
        Opcode.INVOKE_STATIC,
        Opcode.MOVE_RESULT,
        Opcode.INVOKE_SUPER,
        Opcode.RETURN_VOID,
    ),
)

// AdFit feed ad view (friend feed, OpenChat and short-form feed ads all subclass it). Every other
// constructor ends up in this one.
internal object FeedAdLayoutConstructorFingerprint : Fingerprint(
    definingClass = FEED_AD_LAYOUT,
    name = "<init>",
    parameters = listOf("Landroid/content/Context;", "Landroid/util/AttributeSet;", "I"),
    returnType = "V",
)

// Friends tab: the birthday-friends BizBoard view model's binder setter, and the global-region
// friend tab ad model builder (a combine lambda that returns the ad model or null).
internal object BirthdayFriendsBizBoardBindFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/kakao/adfit/ads/media/NativeAdBinder;"),
    returnType = "V",
    custom = { _, classDef -> classDef.sourceFile == "FriendTabBirthdayFriendsBizBoardAdViewModel.kt" },
)

internal object FriendTabGlobalAdModelFingerprint : Fingerprint(
    name = "invokeSuspend",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    returnType = "Ljava/lang/Object;",
    filters = OpcodesFilter.opcodesToFilters(Opcode.NEW_INSTANCE),
    custom = { _, classDef -> classDef.sourceFile == "FriendTabGlobalAdViewModel.kt" },
)
