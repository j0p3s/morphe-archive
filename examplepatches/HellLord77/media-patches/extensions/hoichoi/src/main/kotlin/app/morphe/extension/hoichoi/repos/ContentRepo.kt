package app.morphe.extension.hoichoi.repos

import android.net.Uri
import app.morphe.extension.shared.Logger
import app.morphe.extension.shared.Utils
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.util.Random

const val PAGE_ID = "f976deb6-79df-4c30-ae4a-6cd0648aaee9"

val RANDOM = Random()
val FREE_CONTENT_IDS = mutableSetOf<String>()

private fun getPageData(
    chain: Interceptor.Chain, @Suppress("SameParameterValue") pageId: String
): JSONObject {
    val uri = Uri.parse("https://prod-contents-api.hoichoi.dev/contents/api/v1/pages").buildUpon()
        .appendPath(pageId).build()
    val request = Request.Builder().url(uri.toString()).build()

    chain.proceed(request).use { response ->
        require(response.isSuccessful)
        return JSONObject(response.body.string())
    }
}

private fun isContentFree(content: JSONObject): Boolean {
    try {
        if (content.getString("monetizationModel") == "FREE") {
            val includedMonetizationPlans =
                content.getJSONObject("monetizationPlan").getJSONArray("includedMonetizationPlans")

            for (includedMonetizationPlanIndex in 0 until includedMonetizationPlans.length()) {
                if (!includedMonetizationPlans.getJSONObject(includedMonetizationPlanIndex)
                        .getBoolean("loginRequired")
                ) return true
            }
        }
    } catch (_: JSONException) {
    }

    return false
}

private fun getFreeContentId(chain: Interceptor.Chain): String {
    if (FREE_CONTENT_IDS.isEmpty()) {
        val pageData = getPageData(chain, PAGE_ID)
        val modules = pageData.getJSONArray("modules")
        for (moduleIndex in 0 until modules.length()) {
            val contents = modules.getJSONObject(moduleIndex).getJSONArray("contents")

            contentLoop@ for (contentIndex in 0 until contents.length()) {
                val content = contents.getJSONObject(contentIndex)
                when (content.getString("contentType")) {
                    "movie" -> if (isContentFree(content)) {
                        FREE_CONTENT_IDS.add(content.getString("contentId"))
                        continue@contentLoop
                    }

                    "series" -> {
                        val seasons = content.getJSONArray("seasons")
                        for (seasonIndex in 0 until seasons.length()) {
                            val season = seasons.getJSONObject(seasonIndex)
                            val episodes = season.getJSONArray("episodes")

                            for (episodeIndex in 0 until episodes.length()) {
                                val episode = episodes.getJSONObject(episodeIndex)
                                if (isContentFree(episode)) {
                                    FREE_CONTENT_IDS.add(episode.getString("contentId"))
                                    continue@contentLoop
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val index = RANDOM.nextInt(FREE_CONTENT_IDS.size)
    val iterator = FREE_CONTENT_IDS.iterator()
    var count = 0
    while (iterator.hasNext()) {
        val element = iterator.next()
        if (index == count++) {
            Logger.printInfo { "freeContentId: $element" }
            return element
        }
    }
    throw NullPointerException()
}

private fun getAnonymousToken(chain: Interceptor.Chain): String {
    val request =
        Request.Builder().url("https://prod-api.hoichoi.dev/core/api/v1/users/anonymous-token")
            .build()

    chain.proceed(request).use { response ->
        require(response.isSuccessful)
        val anonymousToken = response.body.string()
        Utils.showToastShort("anonymousToken: $anonymousToken")
        return anonymousToken
    }
}

internal fun getEntitlementInfo(
    chain: Interceptor.Chain,
    action: String?,
    language: String?,
    anonymousToken: String?,
): JSONObject {
    val contentId = getFreeContentId(chain)
    val uri = Uri.parse("https://prod-api.hoichoi.dev/contents/api/v1/entitlement").buildUpon()
        .appendQueryParameter("contentId", contentId).appendQueryParameter("action", action)
        .appendQueryParameter("language", language).build()
    val request = Request.Builder().url(uri.toString())
        .header("Anonymous-Token", anonymousToken ?: getAnonymousToken(chain))
        .post(RequestBody.EMPTY).build()

    chain.proceed(request).use { response ->
        require(response.isSuccessful)
        return JSONObject(response.body.string())
    }
}

internal fun getVideoDetails(
    chain: Interceptor.Chain,
    contentId: String,
    language: String?,
): JSONObject {
    val uri =
        Uri.parse("https://prod-contents-api.hoichoi.dev/contents/api/v1/contents").buildUpon()
            .appendQueryParameter("contentId", contentId).appendQueryParameter("language", language)
            .build()
    val request = Request.Builder().url(uri.toString()).build()

    chain.proceed(request).use { response ->
        require(response.isSuccessful)
        return JSONObject(response.body.string())
    }
}

internal fun buildEntitlementInfo(
    response: Response,
    entitlementInfo: JSONObject,
    videoDetails: JSONObject,
): Response {
    entitlementInfo.put("videoDetails", videoDetails)

    val body =
        entitlementInfo.toString().toByteArray().toResponseBody("application/json".toMediaType())
    return response.newBuilder().code(200).header("Content-Length", body.contentLength().toString())
        .body(body).build()
}