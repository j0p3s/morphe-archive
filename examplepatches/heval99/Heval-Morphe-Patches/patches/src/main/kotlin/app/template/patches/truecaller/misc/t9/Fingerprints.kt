package app.template.patches.truecaller.misc.t9

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal const val REBUILD_ALL_KEY = "RefreshT9MappingWorker.RebuildAll"

// The static helper (R8-renamed) that enqueues RefreshT9MappingWorker as a one-off unique job with
// input "RefreshT9MappingWorker.RebuildAll" = TRUE. Only settings migrations and the QA menu call
// it. The worker's own invokeSuspend also reads the key, but it is an (Object)Object method.
// Static is checked on its own, so a change in the other modifiers (public final) still matches.
internal object RefreshT9MappingFingerprint : Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf(REBUILD_ALL_KEY, "RefreshT9MappingWorker"),
    custom = { method, _ -> AccessFlags.STATIC.isSet(method.accessFlags) },
)
