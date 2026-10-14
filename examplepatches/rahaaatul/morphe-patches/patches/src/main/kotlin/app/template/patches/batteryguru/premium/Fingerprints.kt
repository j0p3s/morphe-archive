package app.template.patches.batteryguru.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lgp5;",
    name = "c",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("one_week_subscription"),
    ),
)

object To0HasPurchaseFingerprint : Fingerprint(
    definingClass = "Lto0;",
    name = "l",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        fieldAccess(
            definingClass = "Lto0;",
            name = "a",
            type = "Ljava/util/ArrayList;",
            opcode = Opcode.IGET_OBJECT,
        ),
    ),
)
