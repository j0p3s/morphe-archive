package app.template.patches.bleacherreport.betting

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.StringComparisonType
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// StatsItemType names (an enum: Enum.name() is backed by its <clinit> strings, which R8 keeps)
// for the game page's betting and odds blocks.
internal val BETTING_STATS_ITEMS = listOf(
    "StatsBetting",
    "StatsLiveOdds",
    "StatsOdds",
    "StatsMoneyline",
    "StatsTotalPoints",
    "StatsSpread",
    "StatsDrawNoBet",
    "StatsThreeWayMoneyline",
    "StatsPlayerProp",
)

// ChannelItemType.Stats(type: StatsItemType) (11.12.0.33: domain.common.channel.c$h).
internal object StatsItemToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(string("Stats(type=", StringComparisonType.STARTS_WITH)),
)

private fun staticTwoArgIn(sourceFile: String) = Fingerprint(
    custom = { method, classDef ->
        classDef.sourceFile == sourceFile && AccessFlags.STATIC.isSet(method.accessFlags) &&
            method.returnType.startsWith("L") &&
            method.implementation != null && method.parameterTypes.size == 2 &&
            method.parameterTypes.all { it.startsWith("L") }
    },
)

// OddsStateMapper's StandaloneModule.Odds -> odds module state (11.12.0.33:
// ui.common.contentmodules.mapper.odds.c.b). Its other method takes (model, Z).
internal val OddsModuleMapperFingerprint = staticTwoArgIn("OddsStateMapper.kt")

// TrendingBetsStateMapper's PackageModule.TrendingBets -> trending bets module state
// (11.12.0.33: ui.common.contentmodules.mapper.z.b). Its other method takes one argument.
internal val TrendingBetsModuleMapperFingerprint = staticTwoArgIn("TrendingBetsStateMapper.kt")

// The Scores screen model (11.12.0.33: domain.interactor.scores.api.models.q). Its toString
// labels the betting link and disclaimer fields.
internal object ScoresToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    parameters = listOf(),
    filters = listOf(
        string("Scores(calendarNavigation=", StringComparisonType.STARTS_WITH),
        string(", bettingLink=", StringComparisonType.EQUALS),
        string(", bettingDisclaimer=", StringComparisonType.EQUALS),
    ),
)
