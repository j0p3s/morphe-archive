package app.template.patches.privacykit.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import app.template.patches.shared.Constants
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object ProUnlockFingerprint : Fingerprint(
    definingClass = "Leb1;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljc3;"),
    filters = listOf(
        methodCall(name = "c", returnType = "Z"),
        fieldAccess(
            definingClass = "Leb1;",
            name = "a",
            type = "Z",
            opcode = Opcode.IGET_BOOLEAN,
        )
    )
)

@Suppress("unused")
val proUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Pro features in Privacy Kit",
) {
    compatibleWith(Constants.PRIVACYKIT_COMPATIBILITY)

    execute {
        ProUnlockFingerprint.method.apply {
            returnEarly(true)
        }
    }
}
