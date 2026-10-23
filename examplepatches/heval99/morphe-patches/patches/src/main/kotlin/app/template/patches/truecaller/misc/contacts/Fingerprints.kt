package app.template.patches.truecaller.misc.contacts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal const val CONTENT_RESOLVER = "Landroid/content/ContentResolver;"

// The selection both background jobs use to read every saved mobile number from the app's own
// contacts data table (projection {"DISTINCT(data1)"}, args {"4", "MOBILE"}). In 26.40.5 these
// are the FetchImContactsWorkAction branch of the work action switch (jc.a; the numbers go in
// batches of 50 to the messenger GetUsers lookup) and the ContactsPremiumStatusFetchWorkAction
// body (z5s.p; a Premium presence lookup).
internal const val PHONEBOOK_MOBILE_SELECTION = "data_type = ? AND data_phonebook_id NOT NULL AND data8 = ?"

// The same selection with an always-false term, so the query returns an empty cursor. The two
// arguments still bind to the two placeholders.
internal const val EMPTY_PHONEBOOK_MOBILE_SELECTION = "$PHONEBOOK_MOBILE_SELECTION AND 0"

// query(Uri, String[], String selection, String[], String) on the resolver: the receiver, then
// the selection as the third parameter.
internal const val QUERY_SELECTION_ARGUMENT = 3

// The number of methods that read the mobile numbers this way in 26.40.5.
internal const val EXPECTED_PHONEBOOK_MOBILE_QUERIES = 2

// Toggle of the Call Assistant whitelist sync (CallAssistantNumberSyncWorkAction): its
// should-sync check (wv7.d) reads it through the resolver before the up to 20000 contact numbers
// are sent as a SetWhitelistNumbersRequestDto to api/v1/users/setWhitelistNumbers.
internal const val CALL_ASSISTANT_NUMBER_SYNC_TOGGLE = "featureCallAssistantNumberSync"
internal const val SET_WHITELIST_NUMBERS_REQUEST = "Lcom/truecaller/call_assistant/core/data/SetWhitelistNumbersRequestDto;"

// Toggle of the contact editor's "suggest this name" checkbox. It becomes the editor state's
// NameSuggestion.isVisible; the save paths enqueue the name suggestion upload only when it is
// visible and checked, and the editor hides the checkbox when it is not visible.
internal const val CONTACT_EDITOR_NAME_SUGGESTION_TOGGLE = "featureContactEditorNameSuggestion"

// Every method that loads the selection and later calls ContentResolver.query. The patch checks
// that the selection really is the query's selection argument before changing it.
internal object PhonebookMobileNumbersQueryFingerprint : Fingerprint(
    filters = listOf(
        string(PHONEBOOK_MOBILE_SELECTION),
        methodCall(definingClass = CONTENT_RESOLVER, name = "query"),
    ),
)
