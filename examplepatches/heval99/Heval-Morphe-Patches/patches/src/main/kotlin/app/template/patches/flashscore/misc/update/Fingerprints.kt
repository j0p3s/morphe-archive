package app.template.patches.flashscore.misc.update

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// The unsupported-version reporter (R8-renamed, acn in 26.10.2). Its constructor opens the
// "unssupported_version_reporter" preferences (the typo is the app's own).
internal object UnsupportedVersionReporterFingerprint : Fingerprint(
    name = "<init>",
    strings = listOf("unssupported_version_reporter"),
)

// Builds and shows the SOFT ("update available") or HARD ("update required", not cancelable)
// dialog, throttled through the "last_notify_<level>" preference. Its only Dialog.show() call.
internal object UpdateDialogFingerprint : Fingerprint(
    classFingerprint = UnsupportedVersionReporterFingerprint,
    returnType = "V",
    strings = listOf("last_notify_"),
    filters = listOf(methodCall(definingClass = "Landroid/app/Dialog;", name = "show")),
)
