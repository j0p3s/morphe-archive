package app.template.patches.hyperion.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

internal const val HYPERION_APP = "Lprojekt/launcher/App;"
internal const val BOOLEAN = "Ljava/lang/Boolean;"

// Hyperion's app code is R8-renamed into hype/* and its key strings ("HmacSHA256", the
// Launcher Key package, intent and permission) are encrypted, so the premium holder is found by
// shape: the app's only Mac user is its token signer, a static (Context, String, String, String)
// String that builds a SecretKeySpec, runs Mac.getInstance and Base64-encodes the result. The
// same class owns the premium state, a static MutableStateFlow<Boolean> that its <clinit>
// creates from Boolean.FALSE.
internal object PremiumTokenSignerFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
    ),
    filters = listOf(
        methodCall(definingClass = "Ljavax/crypto/spec/SecretKeySpec;", name = "<init>"),
        methodCall(definingClass = "Ljavax/crypto/Mac;", name = "getInstance"),
        methodCall(definingClass = "Landroid/util/Base64;", name = "encodeToString"),
    ),
    custom = { _, classDef ->
        classDef.methods.any { method ->
            method.name == "<clinit>" &&
                method.implementation?.instructions?.any { it.isBooleanConstant("FALSE") } == true
        }
    },
)

// App.onCreate: on every cold start it re-signs the stored token and writes the result into
// the premium flow, overwriting the <clinit> value. The Application class keeps its name
// (it is declared in the manifest).
internal object AppOnCreateFingerprint : Fingerprint(
    definingClass = HYPERION_APP,
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
)

internal fun Instruction.isBooleanConstant(name: String): Boolean {
    if (opcode != Opcode.SGET_OBJECT) return false
    val field = (this as ReferenceInstruction).reference as FieldReference
    return field.definingClass == BOOLEAN && field.name == name && field.type == BOOLEAN
}
