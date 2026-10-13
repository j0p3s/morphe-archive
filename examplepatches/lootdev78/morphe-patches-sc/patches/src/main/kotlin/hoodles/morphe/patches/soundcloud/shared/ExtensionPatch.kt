/**
 * Copyright 2026 Hoo-dles
 * https://github.com/hoo-dles/morphe-patches
 */

package hoodles.morphe.patches.soundcloud.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private object RootOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/architecture/view/RootActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    filters = listOf(
        methodCall(smali = "Ldagger/android/AndroidInjection;->a(Landroid/app/Activity;)V"),
        methodCall(smali = "Landroidx/fragment/app/FragmentActivity;->onCreate(Landroid/os/Bundle;)V")
    )
)

private object RootOnResumeFingerprint : Fingerprint(
    definingClass = "Lcom/soundcloud/android/architecture/view/RootActivity;",
    name = "onResume",
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        methodCall(smali = "Landroidx/fragment/app/FragmentActivity;->onResume()V")
    )
)

internal val soundcloudSettingsManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element
            val androidNamespace = "http://schemas.android.com/apk/res/android"
            val activities = document.getElementsByTagName("activity")
            var activityExists = false

            for (index in 0 until activities.length) {
                val activity = activities.item(index) as Element
                if (
                    activity.getAttributeNS(androidNamespace, "name") ==
                    "app.morphe.extension.soundcloud.MorpheSettingsActivity"
                ) {
                    activityExists = true
                    break
                }
            }

            if (!activityExists) {
                val activity = document.createElement("activity")
                activity.setAttributeNS(
                    androidNamespace,
                    "android:name",
                    "app.morphe.extension.soundcloud.MorpheSettingsActivity"
                )
                activity.setAttributeNS(androidNamespace, "android:exported", "false")
                activity.setAttributeNS(
                    androidNamespace,
                    "android:theme",
                    "@style/SoundcloudAppTheme.NoActionBar"
                )
                activity.setAttributeNS(
                    androidNamespace,
                    "android:windowSoftInputMode",
                    "adjustResize"
                )
                application.appendChild(activity)
            }
        }
    }
}

internal val soundcloudCorePatch = bytecodePatch {
    compatibleWith(Constants.COMPATIBILITY)

    extendWith("extensions/soundcloud-morphe-payload.mpe")

    execute {
        RootOnCreateFingerprint.let { fingerprint ->
            val beforeInjection = fingerprint.instructionMatches[0].index
            fingerprint.method.addInstructions(
                beforeInjection,
                "invoke-static {p0}, Lapp/morphe/extension/shared/Utils;->setContext(Landroid/content/Context;)V"
            )

            // The first insertion shifts the second original match by one instruction.
            val afterSuper = fingerprint.instructionMatches[1].index + 2
            fingerprint.method.addInstructions(
                afterSuper,
                "invoke-static {p0}, Lapp/morphe/extension/shared/Utils;->setContext(Landroid/content/Context;)V"
            )
        }

        RootOnResumeFingerprint.let { fingerprint ->
            val afterSuper = fingerprint.instructionMatches[0].index + 1
            fingerprint.method.addInstructions(
                afterSuper,
                "invoke-static {p0}, Lapp/morphe/extension/shared/Utils;->setContext(Landroid/content/Context;)V"
            )
        }
    }
}
