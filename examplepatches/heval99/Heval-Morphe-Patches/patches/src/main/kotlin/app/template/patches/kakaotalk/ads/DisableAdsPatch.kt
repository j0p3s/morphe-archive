// Ported from AmpleReVanced/revanced-patches (GPL-3.0), patches/kakaotalk/ad: RemoveNativeAd,
// RemoveFocusAd, DisableChatRoomAdController, DisableChatRoomListAd, DisableOpenChatFeedAd,
// DisablePayBannerAd, DisableShortFormAd, RemoveBizBoard, RemoveFeedAd and part of
// DisableFriendListsAd, merged into one patch. Changes for 26.8.3: the NativeAdLoader request
// overload is patched too; the hand-written smali uses parameter registers instead of the free
// register provider; setFeedAd and the added onMeasure fail loudly instead of silently skipping;
// the friend tab global ad model returns null with plain smali. The FriendListChipBizBoard bind
// is not ported: its 26.7.2 opcode pattern does not match in 26.8.3.

package app.template.patches.kakaotalk.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter

@Suppress("unused")
val disableAdsPatch = bytecodePatch(
    name = "Disable ads",
    description = "Removes AdFit native, focus, feed and BizBoard ads from the chat list, friends, " +
        "OpenChat, short-form and KakaoPay screens.",
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    execute {
        // 1. AdFit native ads: both NativeAdLoader.load overloads report "loading started" and
        // never request an ad, so their listeners are never called.
        LoadNativeAdFingerprint.method.returnEarly(true)
        LoadNativeAdWithRequestFingerprint.method.returnEarly(true)

        // 2. AdFit focus ads: same for FocusAdLoader.loadAd.
        LoadFocusAdFingerprint.method.returnEarly(true)

        // 3. OpenChat room BizBoard controller never loads its ad.
        OpenLinkChatAdControllerLoadFingerprint.method.returnEarly()

        // 4. Chat list: the native ad slot is never enabled and the suspend global-region check
        // returns Boolean.FALSE as its (non-suspended) result.
        ChatListAdHelperEnabledFingerprint.method.returnEarly(false)
        ChatListGlobalAdEnabledFingerprint.method.addInstructions(
            0,
            """
                sget-object p0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object p0
            """,
        )

        // 5. OpenChat tab feed ad, KakaoPay banner and the short-form ad page: load and render.
        OpenChatFeedAdLoadFingerprint.method.returnEarly()
        OpenChatFeedAdRenderFingerprint.method.returnEarly()
        PayBannerLoadFingerprint.method.returnEarly()
        PayBannerRenderFingerprint.method.returnEarly()
        ShortFormAdLoadFingerprint.method.returnEarly()
        ShortFormAdRenderFingerprint.method.returnEarly()

        // 6. BizBoard: measure the frame as 0x0, hide it and skip the real measurement. The
        // method has only one local, so the parameter registers (free once it returns) are used.
        MeasuringBizBoardFingerprint.method.addInstructions(
            0,
            """
                const/16 p1, 0x0
                const/16 p2, 0x0
                invoke-virtual/range {p0 .. p2}, Landroid/view/View;->setMeasuredDimension(II)V
                const/16 p1, 0x8
                invoke-virtual/range {p0 .. p1}, Landroid/view/View;->setVisibility(I)V
                return-void
            """,
        )

        // 7. AdFit feed ad view: hide and zero-size it at the end of the constructor (p1 and p2
        // are no longer read there), never bind a FeedAd, and always measure as 0x0.
        val feedAdConstructor = FeedAdLayoutConstructorFingerprint.method
        val returnIndex = feedAdConstructor.instructions.lastIndex
        if (feedAdConstructor.instructions[returnIndex].opcode != Opcode.RETURN_VOID) {
            throw PatchException("$FEED_AD_LAYOUT constructor does not end with return-void")
        }
        feedAdConstructor.addInstructionsWithLabels(
            returnIndex,
            """
                const/16 p1, 0x8
                invoke-virtual/range {p0 .. p1}, Landroid/view/View;->setVisibility(I)V
                invoke-virtual/range {p0 .. p0}, Landroid/view/View;->getLayoutParams()Landroid/view/ViewGroup${'$'}LayoutParams;
                move-result-object p1
                if-eqz p1, :skip_size
                const/4 p2, 0x0
                iput p2, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->height:I
                iput p2, p1, Landroid/view/ViewGroup${'$'}LayoutParams;->width:I
                :skip_size
                invoke-virtual/range {p0 .. p0}, Landroid/view/View;->requestLayout()V
            """,
        )

        val feedAdLayout = FeedAdLayoutConstructorFingerprint.classDef
        val setFeedAd = feedAdLayout.methods.filter { it.name == "setFeedAd" && it.implementation != null }
        if (setFeedAd.isEmpty()) throw PatchException("$FEED_AD_LAYOUT has no setFeedAd")
        setFeedAd.forEach { it.returnEarly() }

        // FeedAdLayout does not override onMeasure in 26.8.3, so one is added; if a later build
        // adds its own, it is patched instead of duplicated.
        val zeroMeasure = """
            const/16 p1, 0x0
            const/16 p2, 0x0
            invoke-virtual/range {p0 .. p2}, Landroid/view/View;->setMeasuredDimension(II)V
            return-void
        """
        val existingOnMeasure = feedAdLayout.methods.firstOrNull {
            it.name == "onMeasure" && it.parameterTypes.map(CharSequence::toString) == listOf("I", "I")
        }
        if (existingOnMeasure != null) {
            existingOnMeasure.addInstructions(0, zeroMeasure)
        } else {
            feedAdLayout.methods.add(
                ImmutableMethod(
                    feedAdLayout.type,
                    "onMeasure",
                    listOf(ImmutableMethodParameter("I", null, null), ImmutableMethodParameter("I", null, null)),
                    "V",
                    AccessFlags.PROTECTED.value,
                    null,
                    null,
                    MutableMethodImplementation(3),
                ).toMutable().apply { addInstructions(0, zeroMeasure) },
            )
        }

        // 8. Friends tab: the birthday-friends BizBoard never receives an ad binder, and the
        // global-region ad model is always null (the lambda's own "no ad" result).
        BirthdayFriendsBizBoardBindFingerprint.method.returnEarly()
        FriendTabGlobalAdModelFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x0
                return-object p0
            """,
        )
    }
}
