/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.castle.misc.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.secshell.editSecShellPayload
import app.morphe.util.getReference
import app.morphe.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val USER_CLASS = "Lcom/gxgx/base/bean/User;"
private const val VIDEO_CLASS = "Lcom/gxgx/daqiandy/bean/VideoBean;"
private const val DOWNLOAD_PLAYLIST_TASK_CLASS = "Lcom/arialyy/aria/m3u8/M3U8InfoTask;"
private const val PLAYLIST_HEADER_ERROR = "Input does not start with the #EXTM3U header."
private const val EXTENSION_PACKAGE = "Lapp/hxreborn/extension/castle/"
private const val PREVIEW_PLAYLIST_CLASS = "${EXTENSION_PACKAGE}PreviewPlaylist;"

private const val PREMIUM_VIP_INFO = """
    new-instance v0, Lcom/gxgx/base/bean/VipInfo;
    const-string v1, "4102358400000"
    const-string v2, "Premium"
    const/4 v3, 0x2
    invoke-static { v3 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v3
    const/4 v4, 0x0
    invoke-static { v4 }, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v4
    invoke-direct { v0, v1, v2, v3, v4 }, Lcom/gxgx/base/bean/VipInfo;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;)V
    return-object v0
"""

private const val NOT_PREVIEW = """
    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
    return-object v0
"""

private const val EXPAND_PREVIEW_PLAYLIST = """
    invoke-static { p1, p2 }, $PREVIEW_PLAYLIST_CLASS->expand(Landroid/net/Uri;Ljava/io/InputStream;)Ljava/io/InputStream;
    move-result-object p2
"""

@Suppress("unused")
val unlockPremiumPatch = rawResourcePatch(
    name = "Unlock premium",
    description = "Unlocks member-only titles and shows the account as Premium. " +
        "Requires a signed-in account.",
) {
    compatibleWith(AppCompatibilities.CASTLE)

    execute {
        editSecShellPayload {
            replaceBody(USER_CLASS, "getVipInfos", registers = 6, smali = PREMIUM_VIP_INFO)
            replaceBody(VIDEO_CLASS, "isPreview", registers = 2, smali = NOT_PREVIEW)
            val playlistParser = prependToMethodUsingString(
                PLAYLIST_HEADER_ERROR,
                firstParameter = "Landroid/net/Uri;",
                smali = EXPAND_PREVIEW_PLAYLIST,
            )
            addExtensionPackage(EXTENSION_PACKAGE, besideClass = playlistParser)

            method(DOWNLOAD_PLAYLIST_TASK_CLASS, "handleConnect").apply {
                val streamIndex = indexOfFirstInstructionOrThrow {
                    opcode == Opcode.INVOKE_STATIC && getReference<MethodReference>()?.name == "convertInputStream"
                } + 1
                val stream = getInstruction<OneRegisterInstruction>(streamIndex).registerA
                addInstructions(
                    streamIndex + 1,
                    """
                        invoke-static { p1, v$stream }, $PREVIEW_PLAYLIST_CLASS->expand(Ljava/lang/String;Ljava/io/InputStream;)Ljava/io/InputStream;
                        move-result-object v$stream
                    """,
                )
            }
        }
    }
}
