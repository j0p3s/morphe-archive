package app.morphe.patches.multiaccounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.Constants.COMPATIBILITY_MULTIACCOUNTS
import app.morphe.patches.shared.clearTryBlocks
import app.morphe.patches.shared.ensureRegisterCount
import app.morphe.patches.shared.replaceWithReturnVoid
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * 128-character prefix of the official ZhuoMeng release signing certificate DER hex.
 * Extracted from META-INF/ZMGAME.RSA (Serial: 0x520332c2, Issuer/Subject: CN=ZhuoMeng, O=www.zhuomeng.com).
 * Required by BaseGameUtil.h (g4.h) to ensure re-signed APKs pass client-side self-verification.
 */
private const val ORIGINAL_SIGNATURE_HEX_128 =
    "30820271308201daa0030201020204520332c2300d06092a864886f70d0101050500307c310b300906035504061302434e3111300f060355040813085368616e"

/**
 * Targets package signature extraction in `com.excelliance.kxqp.util.g4.h(Context)`.
 * Queries `PackageManager.getPackageInfo(getPackageName(), 64).signatures[0].toCharsString()`.
 * Returns the official release signing certificate hex to ensure re-signed and repackaged
 * builds pass client self-verification without failing or reporting foreign test signatures.
 */
private object BaseGameUtilSignatureFingerprint : Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/util/g4;",
    name = "h",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Ljava/lang/String;",
)

/**
 * Targets the launch-time in-app update check in `com.excelliance.kxqp.util.jm.H(Activity)`.
 * Called during startup initialization by `ih.e(Activity)`.
 * Neutralizes the Play Core IAppUpdateService query so sideloaded installations
 * outside Google Play (e.g. APKMirror builds) never trigger update checks or forced exits.
 */
private object AppUpdateLaunchCheckFingerprint : Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/util/jm;",
    name = "H",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/app/Activity;"),
    returnType = "V",
)

/**
 * Targets the update dialog dispatcher in `com.excelliance.kxqp.util.jm.t(Activity, boolean)`.
 * Defense-in-depth: prevents displaying the modal update prompt or executing
 * force-exit flow (`activity.finish()`) if reached from any alternate path.
 */
private object AppUpdateDialogDispatchFingerprint : Fingerprint(
    definingClass = "Lcom/excelliance/kxqp/util/jm;",
    name = "t",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Landroid/app/Activity;", "Z"),
    returnType = "V",
)

@Suppress("unused")
val multiaccountsCompatBypassPatch = bytecodePatch(
    name = "Sideload Compatibility",
    description = "Ensures compatibility for sideloaded installations by spoofing the official APK signature certificate for self-verification and neutralizing launch-blocking Google Play in-app update checks.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)

    execute {
        var patched = 0

        // 1. Re-signing tolerance: return original ZhuoMeng signing certificate hex
        BaseGameUtilSignatureFingerprint.method.apply {
            clearTryBlocks()
            ensureRegisterCount(1)
            implementation?.let { removeInstructions(0, it.instructions.count()) }
            addInstructions(
                0,
                """
                    const-string v0, "$ORIGINAL_SIGNATURE_HEX_128"
                    return-object v0
                """.trimIndent(),
            )
        }
        patched++

        // 2. Play Core in-app update check suppression at launch
        AppUpdateLaunchCheckFingerprint.method.replaceWithReturnVoid()
        patched++

        // 3. Play Core in-app update dialog dispatch suppression (defense in depth)
        AppUpdateDialogDispatchFingerprint.method.replaceWithReturnVoid()
        patched++

        if (patched == 0) {
            error("[Multiaccounts Compat] No compatibility targets found - failing closed.")
        }

        println("[Multiaccounts Compat] Applied $patched hooks -> sideload and re-signing compatibility enabled.")
    }
}
