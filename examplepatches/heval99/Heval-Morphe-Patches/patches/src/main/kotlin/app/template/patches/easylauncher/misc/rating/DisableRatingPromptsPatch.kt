package app.template.patches.easylauncher.misc.rating

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.util.returnEarly
import app.template.patches.shared.Constants.COMPATIBILITY_EASYLAUNCHER

@Suppress("unused")
val disableRatingPromptsPatch = bytecodePatch(
    name = "Disable rating prompts",
    description = "Stops the app's feedback (sentiment) dialog and the Google Play review request.",
) {
    compatibleWith(COMPATIBILITY_EASYLAUNCHER)

    execute {
        // 1. The lifecycle observer that schedules the sentiment prompt and the review request.
        ReviewObserverOnResumeFingerprint.method.returnEarly()

        // 2. The review manager never considers the user primed or due for a review, for any
        // other caller.
        IsUserPrimedForReviewFingerprint.method.returnEarly(false)
        ShouldRequestAppReviewFingerprint.method.returnEarly(false)
    }
}
