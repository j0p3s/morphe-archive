package app.aeong.patches.twitch.stuff

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess

internal const val DISCOVERY_FEED_PAGE_TYPE =
    "Ltv/twitch/android/models/feed/DiscoveryFeedPage;"

internal const val FOLLOWING_PAGE_TYPE =
    "Ltv/twitch/android/models/feed/DiscoveryFeedPage\$FollowingPage;"

internal object DefaultHomePageFingerprint : Fingerprint(
    returnType = DISCOVERY_FEED_PAGE_TYPE,
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            smali = "$FOLLOWING_PAGE_TYPE->INSTANCE:$FOLLOWING_PAGE_TYPE"
        )
    )
)