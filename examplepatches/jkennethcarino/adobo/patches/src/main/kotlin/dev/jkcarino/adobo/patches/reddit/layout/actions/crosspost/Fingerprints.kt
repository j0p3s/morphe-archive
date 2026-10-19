package dev.jkcarino.adobo.patches.reddit.layout.actions.crosspost

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.anyInstruction
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import dev.jkcarino.adobo.patches.reddit.shared.LinkToStringFingerprint

internal val toStringFingerprints =
    setOf(
        "PostActionScoreBarElement(linkId=" to ", isCrosspostable=",
        "PostContentFragment(__typename=" to ", isCrosspostable=",
    ).map { (dataClass, isCrosspostable) ->
        Fingerprint(
            returnType = "Ljava/lang/String;",
            parameters = listOf(),
            filters = listOf(
                string(dataClass),
                string(isCrosspostable),
                anyInstruction(
                    opcode(Opcode.INVOKE_VIRTUAL),
                    opcode(Opcode.IGET_OBJECT),
                    location = MatchAfterImmediately()
                ),
                opcode(Opcode.IGET_BOOLEAN)
            )
        )
    }

internal object RepostButtonFingerprint : Fingerprint(
    accessFlags = listOf(
        AccessFlags.PUBLIC,
        AccessFlags.STATIC,
        AccessFlags.FINAL
    ),
    returnType = "L",
    filters = listOf(
        literal(-0x1802fdd7),
    )
)

internal object ShouldAllowCrosspostsFingerprint : Fingerprint(
    classFingerprint = LinkToStringFingerprint,
    name = "shouldAllowCrossposts",
    returnType = "Z"
)
