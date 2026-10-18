package app.template.patches.digisport.betting

import app.morphe.patcher.Fingerprint

// UIResultsMatch.getBet(): the bookmaker odds (1 / X / 2 and the bet link) of a soccer match row.
// Its only readers are in RowResultsMatchBindingImpl: executeBindings sets the odds block
// (@id/nas) GONE when it is null, and the block's click callback returns early on null.
internal object UIResultsMatchGetBetFingerprint : Fingerprint(
    definingClass = "Lro/rcsrds/digisport/data/model/ui/articles/UIResultsMatch;",
    name = "getBet",
    returnType = "Lro/rcsrds/digisport/data/model/ui/articles/UIResultsMatchBet;",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)
