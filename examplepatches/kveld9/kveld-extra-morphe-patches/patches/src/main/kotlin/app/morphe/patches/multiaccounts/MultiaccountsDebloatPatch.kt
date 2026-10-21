package app.morphe.patches.multiaccounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.shared.ANDROID_XML_NAMESPACE
import app.morphe.patches.shared.Constants.COMPATIBILITY_MULTIACCOUNTS
import app.morphe.patches.shared.childrenNamed
import app.morphe.patches.shared.disableComponentsWhere
import app.morphe.patches.shared.removeChildren
import app.morphe.patches.shared.replaceWithReturnNull
import app.morphe.patches.shared.stripPermissionsWhere
import org.w3c.dom.Element

// Universal Coverage Matrix for Multiple Accounts (com.excelliance.multiaccounts 5.8.2):
// - Remove Extra Languages: UNIVERSAL_OK (standard res/values-* string tables, handled by universal LocaleResourceSlimmerPatch).
// - Remove Unused Screen Sizes: UNIVERSAL_OK (standard nodpi base + drawable-* density buckets, handled by universal DpiResourceSlimmerPatch).
// - ApkJunkCleaner: UNIVERSAL_OK (DebugProbesKt.bin pruned by universal ApkJunkCleanerPatch; kotlin builtins
//   are load-bearing runtime descriptors for kotlin-reflect, loaded via lg.a.q:(Lwf/c;)Ljava/lang/String; in classes8.dex
//   and strictly preserved as functional).
// - Stop Background Wakeups: DEDICATED_NEEDED.
//   Failure reason of universal BackgroundSyncPurgePatch:
//     1. Universal patch does not remove androidx.work.WorkManagerInitializer meta-data from
//        androidx.startup.InitializationProvider in AndroidManifest.xml (line 5916).
//     2. Universal patch does not stub Landroidx/work/WorkManagerInitializer;->create in Dalvik bytecode,
//        leaving orphaned initialization trying to run against disabled services.
//     3. Clone container virtualization lifecycles (TrampolineService, SmtServService, :lbcore) and WAKE_LOCK
//        must be strictly preserved (PRESERVED_FUNCTIONAL) to protect cloned-app launch, notifications, and IPC.
//     4. WorkManager hosts exclusively ad-SDK schedulers (OfflineNotificationPoster, OfflinePingSender, UniversalRequestJob).
// - Shrink PNG Images: UNIVERSAL_OK (standard PNG raster drawables, handled by universal PngOptimizerPatch).
// - Shrink WebP Images: UNIVERSAL_OK (standard WebP images, handled by universal UniversalWebpOptimizerPatch).
// - Remove Tracking Code: NOT_APPLICABLE (all 13 .so files in lib/arm64-v8a have >=1 load-bearing reference in DEX;
//   zeroing is strictly forbidden to prevent UnsatisfiedLinkError crashes).
// - Tutorial Videos / Stickers: NOT_APPLICABLE (no media assets present in res/raw or assets/; supplierconfig.json is 136 bytes negligible).

private val BLOCKED_SERVICES = setOf(
    "androidx.work.impl.background.systemalarm.SystemAlarmService",
    "androidx.work.impl.background.systemjob.SystemJobService",
    "androidx.work.impl.foreground.SystemForegroundService",
)

private val BLOCKED_RECEIVERS = setOf(
    "androidx.work.impl.utils.ForceStopRunnable\$BroadcastReceiver",
    "androidx.work.impl.background.systemalarm.ConstraintProxy\$BatteryChargingProxy",
    "androidx.work.impl.background.systemalarm.ConstraintProxy\$BatteryNotLowProxy",
    "androidx.work.impl.background.systemalarm.ConstraintProxy\$StorageNotLowProxy",
    "androidx.work.impl.background.systemalarm.ConstraintProxy\$NetworkStateProxy",
    "androidx.work.impl.background.systemalarm.RescheduleReceiver",
    "androidx.work.impl.background.systemalarm.ConstraintProxyUpdateReceiver",
    "androidx.work.impl.diagnostics.DiagnosticsReceiver",
)

private val BLOCKED_PERMISSIONS = setOf(
    "android.permission.RECEIVE_BOOT_COMPLETED",
)

@Suppress("unused")
val multiaccountsDebloatManifestPatch = resourcePatch(
    name = "Multiaccounts Debloat (Setup)",
    description = "Disables non-functional ad-network WorkManager services and receivers, removes WorkManagerInitializer startup metadata, and strips RECEIVE_BOOT_COMPLETED in AndroidManifest.xml while strictly preserving clone container services.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)

    execute {
        val manifestFile = get("AndroidManifest.xml")
        if (!manifestFile.exists()) {
            println("[Multiaccounts Debloat] Skipped: AndroidManifest.xml not found.")
            return@execute
        }

        var removedPermissions = 0
        var disabledServices = 0
        var disabledReceivers = 0
        var prunedInitializers = 0

        document(manifestFile.absolutePath).use { doc ->
            val root = doc.documentElement
            val application = root.getElementsByTagName("application").item(0) as? Element

            removedPermissions = root.stripPermissionsWhere { it in BLOCKED_PERMISSIONS }.size

            if (application != null) {
                disabledServices = application.disableComponentsWhere("service") { it in BLOCKED_SERVICES }
                disabledReceivers = application.disableComponentsWhere("receiver") { it in BLOCKED_RECEIVERS }

                val providers = application.childrenNamed("provider")
                for (provider in providers) {
                    val metaNodes = provider.childrenNamed("meta-data")
                    val toRemove = metaNodes.filter {
                        val name = it.getAttribute("android:name").ifBlank { it.getAttributeNS(ANDROID_XML_NAMESPACE, "name") }
                        name == "androidx.work.WorkManagerInitializer"
                    }
                    if (toRemove.isNotEmpty()) {
                        provider.removeChildren(toRemove)
                        prunedInitializers += toRemove.size
                    }
                }
            }
        }

        println("[Multiaccounts Debloat] Stripped $removedPermissions permission(s), disabled $disabledServices service(s), $disabledReceivers receiver(s), and pruned $prunedInitializers startup initializer(s).")
    }
}

// --- Bytecode Patch Contracts ---

private val workManagerInitializerCreateFingerprint = Fingerprint(
    definingClass = "Landroidx/work/WorkManagerInitializer;",
    name = "create",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Landroidx/work/WorkManager;",
)

private val workManagerInitializerBridgeFingerprint = Fingerprint(
    definingClass = "Landroidx/work/WorkManagerInitializer;",
    name = "create",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Ljava/lang/Object;",
)

@Suppress("unused")
val multiaccountsDebloatPatch = bytecodePatch(
    name = "Stop Background Wakeups",
    description = "Stops ad-SDK WorkManager background tasks to eliminate background wakeups and battery drain. Container virtualization processes, push notifications, and clone keep-alive services are strictly preserved.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)
    dependsOn(multiaccountsDebloatManifestPatch)

    execute {
        var patched = 0

        workManagerInitializerCreateFingerprint.method.apply {
            replaceWithReturnNull()
            patched++
        }

        workManagerInitializerBridgeFingerprint.method.apply {
            replaceWithReturnNull()
            patched++
        }

        println("[Multiaccounts Debloat] Successfully neutralized $patched WorkManager entrypoint(s).")
    }
}
