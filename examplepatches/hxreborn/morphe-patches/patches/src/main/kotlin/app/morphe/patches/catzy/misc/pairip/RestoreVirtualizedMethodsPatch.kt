/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.catzy.misc.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.generated.replaceMethod
import app.morphe.patches.shared.misc.pairip.removePairipVirtualizationPatch
import app.morphe.util.inputStreamFromBundledResource
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val VM_RUNNER = "Lcom/pairip/VMRunner;"
private const val RESOURCE_FOLDER = "catzy"

private class RestoredMethod(val type: String, val name: String, val registers: Int) {
    val resourceName = "${type.removePrefix("L").removeSuffix(";").replace('/', '.')}.$name.smali"
}

private val RESTORED_METHODS = listOf(
    RestoredMethod("Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;", "onCreate", 8),
    RestoredMethod("Lcom/nieruo/healthapp/HealthApplication;", "onCreate", 26),
    RestoredMethod("Lcom/nieruo/healthapp/receiver/ScheduledNotificationBootReceiver;", "onReceive", 7),
    RestoredMethod("Lcom/nieruo/healthapp/receiver/ScheduledNotificationReceiver;", "onReceive", 22),
    RestoredMethod("Lcom/nieruo/healthapp/receiver/SleepReceiver;", "onReceive", 12),
    RestoredMethod("Lcom/nieruo/healthapp/service/FocusTimerService;", "onCreate", 18),
    RestoredMethod("Lcom/nieruo/healthapp/service/SleepService;", "onCreate", 6),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;", "onResume", 5),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/BreatheHomeActivity;", "onResume", 4),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;", "onResume", 6),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;", "onDestroy", 4),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;", "onResume", 6),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/HalloweenHomeActivity;", "onResume", 11),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;", "onPause", 4),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;", "onResume", 2),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/HomeActivity;", "onResume", 9),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/SplashActivity;", "onCreate", 10),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;", "onDestroy", 2),
    RestoredMethod("Lcom/nieruo/healthapp/ui/activity/WidgetSplashActivity;", "onCreate", 14),
    RestoredMethod("Lmi/a;", "onReceive", 6),
    RestoredMethod("Lcom/facebook/FacebookActivity;", "onCreate", 7),
    RestoredMethod("Lcom/android/billingclient/api/ProxyBillingActivity;", "onCreate", 14),
    RestoredMethod("Lcom/android/billingclient/api/ProxyBillingActivityV2;", "onCreate", 11),
    RestoredMethod("Ldb/b5;", "onReceive", 11),
    RestoredMethod("Lcom/facebook/CustomTabMainActivity;", "onCreate", 12),
    RestoredMethod("Lg6/k0;", "onReceive", 9),
    RestoredMethod("Lg6/m0;", "onReceive", 24),
)

private val UNRESTORED_STUB_TYPES = setOf(
    "Landroidx/profileinstaller/ProfileInstallReceiver;",
    "Landroidx/work/impl/background/systemalarm/ConstraintProxyUpdateReceiver;",
    "Landroidx/work/impl/background/systemalarm/RescheduleReceiver;",
    "Landroidx/work/impl/utils/ForceStopRunnable\$BroadcastReceiver;",
    "Lcn/thinkingdata/analytics/utils/broadcast/TDReceiver;",
    "Lcom/google/android/datatransport/runtime/scheduling/jobscheduling/AlarmManagerSchedulerBroadcastReceiver;",
    "Lcom/google/android/gms/measurement/AppMeasurementReceiver;",
    "Lcom/nieruo/healthapp/receiver/CancelScheduledReceiver;",
    "Ldb/e1;",
    "Lha/s;",
    "Loe/z;",
    "Lx8/c;",
    "Ly7/k0;",
)

private fun Method.callsVmRunner() = implementation?.instructions?.any { instruction ->
    ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.definingClass == VM_RUNNER
} == true

val restoreVirtualizedMethodsPatch = bytecodePatch {
    compatibleWith(AppCompatibilities.CATZY)

    dependsOn(removePairipVirtualizationPatch)

    execute {
        val first = RESTORED_METHODS.first()
        if (!classDefBy(first.type).methods.single { it.name == first.name }.callsVmRunner()) return@execute

        RESTORED_METHODS.forEach { restored ->
            mutableClassDefBy(restored.type).apply {
                check(methods.single { it.name == restored.name }.callsVmRunner()) {
                    "${restored.type}->${restored.name} is not a PairIP stub in this build"
                }
                val smali = inputStreamFromBundledResource(RESOURCE_FOLDER, restored.resourceName)
                    ?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: error("Missing $RESOURCE_FOLDER/${restored.resourceName}")
                replaceMethod(restored.name, restored.registers, smali)
            }
        }

        val leftover = mutableSetOf<String>()
        classDefForEach { classDef ->
            if (!classDef.type.startsWith("Lcom/pairip/") && classDef.methods.any { it.callsVmRunner() }) {
                leftover += classDef.type
            }
        }
        check(leftover == UNRESTORED_STUB_TYPES) {
            "PairIP stubs changed: unexpected ${leftover - UNRESTORED_STUB_TYPES}, missing ${UNRESTORED_STUB_TYPES - leftover}"
        }
    }
}
