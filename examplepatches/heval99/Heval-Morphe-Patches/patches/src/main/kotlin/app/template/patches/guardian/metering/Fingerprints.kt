package app.template.patches.guardian.metering

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

internal const val OBJECT = "Ljava/lang/Object;"
internal const val STRING = "Ljava/lang/String;"

// The metering Retrofit service keeps its name (Retrofit needs the interface and its methods).
internal const val METERING_RETROFIT_API = "Lcom/theguardian/metering/api/RetrofitApi;"

// Kotlin suspend methods take the continuation last. R8 may type it as ContinuationImpl (6.238)
// or as Continuation.
internal val CONTINUATIONS = setOf(
    "Lkotlin/coroutines/jvm/internal/ContinuationImpl;",
    "Lkotlin/coroutines/Continuation;",
)

// R8 renames the app's own classes since 6.238 (the metering use cases are
// com/theguardian/metering/a, b, c there), so every anchor below is a string, the kept
// Retrofit interface or a method shape.

// Every method that calls RetrofitApi.message, the metering "which message do I show" request.
// GetMeterApiState (6.238: com/theguardian/metering/a.a(String, ContinuationImpl)) must be the
// only one. It turns the reply into the metering state: a wall, the warm-up hurdle, or
// None(shouldTrackArticleView).
internal object MeteringMessageCallFingerprint : Fingerprint(
    filters = listOf(methodCall(definingClass = METERING_RETROFIT_API, name = "message")),
)

// The metering "no wall" state (6.238: gi8), found through its data-class toString(). Its
// <init>(Z) takes shouldTrackArticleView; TrackMeteredArticleView only posts an article view to
// the metering API when it is true.
internal object MeteringNoneToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = STRING,
    parameters = listOf(),
    filters = listOf(string("None(shouldTrackArticleView=", StringComparisonType.STARTS_WITH)),
)

// The offline wall state object (6.238: hi8), a singleton whose toString() is its name.
internal object OfflineWallToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = STRING,
    parameters = listOf(),
    filters = listOf(string("OfflineWall", StringComparisonType.EQUALS)),
)

// The free sign-in gate state object (6.238: ii8), a singleton whose toString() is its name.
internal object SignInWallToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = STRING,
    parameters = listOf(),
    filters = listOf(string("SignInWall", StringComparisonType.EQUALS)),
)
