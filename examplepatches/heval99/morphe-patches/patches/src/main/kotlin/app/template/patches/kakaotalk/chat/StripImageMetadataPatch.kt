// Ported from AmpleReVanced/revanced-patches (GPL-3.0), interaction/media/StripImageMetadataPatch.
// Changes: always on (no settings toggle), and the extension class lives in extensions/kakaotalk.

package app.template.patches.kakaotalk.chat

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.addInstructionsAtControlFlowLabel
import app.template.patches.shared.Constants.COMPATIBILITY_KAKAOTALK
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

internal const val STRIP_IMAGE_METADATA = "Lapp/template/extension/kakaotalk/StripImageMetadata;"

@Suppress("unused")
val stripImageMetadataPatch = bytecodePatch(
    name = "Strip image metadata",
    description = "Removes GPS location and camera details from JPEG photos before they are sent " +
        "(best effort: other formats are sent unchanged).",
) {
    compatibleWith(COMPATIBILITY_KAKAOTALK)

    extendWith("extensions/kakaotalk.mpe")

    execute {
        // ImageItemSendHelper's resize step returns either the original file or the recompressed
        // copy (which keeps the original EXIF). Every returned file goes through the extension,
        // which returns a cleaned temp copy, or the same file when there is nothing to remove.
        ImageResizeFingerprint.method.apply {
            val returns = instructions.indices.filter { instructions[it].opcode == Opcode.RETURN_OBJECT }
            if (returns.isEmpty()) throw PatchException("$definingClass->$name has no return-object")
            // From the last return backwards so earlier indices stay valid. Branch labels on a
            // return move to the inserted call, so jumps to the return are sanitized too.
            returns.asReversed().forEach { index ->
                val register = (instructions[index] as OneRegisterInstruction).registerA
                addInstructionsAtControlFlowLabel(
                    index,
                    """
                        invoke-static/range {v$register .. v$register}, $STRIP_IMAGE_METADATA->sanitize(Ljava/io/File;)Ljava/io/File;
                        move-result-object v$register
                    """,
                )
            }
        }
    }
}
