package app.template.patches.digisport.layout

import app.morphe.patcher.Fingerprint

internal const val API_ARTICLE = "Lro/rcsrds/digisport/data/model/api/article/ApiArticle;"
internal const val ARTICLE_SECTION = "Lro/rcsrds/digisport/data/model/ui/articles/ArticleSection;"
internal const val PARSING_EXTENSIONS = "Lro/rcsrds/digisport/tools/helpers/parsing/ParsingExtensionsKt;"
private const val ARTICLE_WIDGETS =
    "Lro/rcsrds/digisport/tools/helpers/parsing/articleTextAndLive/GetArticleDetailsWidgets;"

// GetListingDetailsArticles.parseListingArticles(article): adds one listing article (with its
// separator lines) to a section listing. Called once per article from GetListingDetails.
internal object ParseListingArticlesFingerprint : Fingerprint(
    definingClass = "Lro/rcsrds/digisport/tools/helpers/parsing/listing/GetListingDetailsArticles;",
    name = "parseListingArticles",
    returnType = "V",
    parameters = listOf(API_ARTICLE),
    custom = { method, _ -> method.implementation != null },
)

// GetArticleDetailsWidgets.addArticleWidget(isCross, article): one article card in the widgets under
// an article. isCross is true for a "widgetCross" widget (articles from sister RCS&RDS sites). Its
// only caller adds the result to the section list unconditionally, so it must not return null.
internal object AddArticleWidgetFingerprint : Fingerprint(
    definingClass = ARTICLE_WIDGETS,
    name = "addArticleWidget",
    returnType = ARTICLE_SECTION,
    parameters = listOf("Z", API_ARTICLE),
    custom = { method, _ -> method.implementation != null },
)

// GetArticleDetailsWidgets.widgetTitle(sections, widget): the heading row of a widget.
internal object WidgetTitleFingerprint : Fingerprint(
    definingClass = ARTICLE_WIDGETS,
    name = "widgetTitle",
    returnType = "V",
    parameters = listOf("Ljava/util/ArrayList;", "Lro/rcsrds/digisport/data/model/api/article/ApiArticleLayout;"),
    custom = { method, _ -> method.implementation != null },
)

// HomeViewModel.getExclusiveVideo(): fills mHasExclusivePromotion / mNrExclusivePromotion from the
// EPG's exclusive-video count. HomeFragment calls it once; the Home header binding shows the
// "EXCLUSIVE ONLINE · LIVE" bar only when mHasExclusivePromotion is true (it starts false).
internal object GetExclusiveVideoFingerprint : Fingerprint(
    definingClass = "Lro/rcsrds/digisport/ui/main/home/HomeViewModel;",
    name = "getExclusiveVideo",
    returnType = "V",
    parameters = listOf(),
    custom = { method, _ -> method.implementation != null },
)
