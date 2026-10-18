package app.template.patches.digisport.misc.consent

import app.morphe.patcher.Fingerprint

internal const val GOOGLE_CONSENT_UMP = "Lro/rcsrds/digisport/tools/helpers/googleConsent/GoogleConsentUmp;"
internal const val GOOGLE_CONSENT_STATUS = "Lro/rcsrds/digisport/tools/helpers/googleConsent/GoogleConsentUmp\$STATUS;"

internal const val USER_DATA_ADS_BINDING = "Lro/rcsrds/digisport/databinding/IncludeSettingsUserDataAdsBindingImpl;"

// The generated data binding of include_settings_user_data_ads: the "Publicitate (Google Ad
// Manager)" heading, its paragraph and the "Actualizare date" consent button, included as
// mIncludeAds in the confidentiality section of Settings and of the GDPR details screen (Google and
// Huawei variants). executeBindings only sets the button's visibility and click listener; nothing
// (generated or app code) ever sets the root's visibility.
internal object UserDataAdsExecuteBindingsFingerprint : Fingerprint(
    definingClass = USER_DATA_ADS_BINDING,
    name = "executeBindings",
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)

// GoogleConsentUmp.loadConsentInfo(requestUpdate): the app's only path to Google UMP's
// requestConsentInfoUpdate and loadAndShowConsentFormIfRequired. MainActivity, the GDPR onboarding
// (GdprActivityBase), its details screen and Settings call it and then observe mStatus.
internal object LoadConsentInfoFingerprint : Fingerprint(
    definingClass = GOOGLE_CONSENT_UMP,
    name = "loadConsentInfo",
    returnType = GOOGLE_CONSENT_UMP,
    parameters = listOf("Z"),
    custom = { method, _ -> method.implementation != null },
)
