package app.morphe.extension.hoichoi.interceptors

import android.net.Uri
import app.morphe.extension.hoichoi.repos.buildEntitlementInfo
import app.morphe.extension.hoichoi.repos.getEntitlementInfo
import app.morphe.extension.hoichoi.repos.getVideoDetails
import app.morphe.extension.shared.Logger
import app.morphe.extension.shared.Utils
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.closeQuietly
import java.io.IOException

@Suppress("unused")
class EntitlementInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Logger.printDebug { "request: $request" }

        val response = chain.proceed(request)
        Logger.printDebug { "response: $response" }

        val uri = Uri.parse(request.url.toString())
        if (!(request.method == "POST" && uri.host == "prod-api.hoichoi.dev" && uri.path == "/contents/api/v1/entitlement" && response.code == 401)) {
            return response
        }

        response.closeQuietly()
        val contentId = uri.getQueryParameter("contentId")!!
        Utils.showToastShort("contentId: $contentId")
        Logger.printInfo { "contentId: $contentId" }

        val action = uri.getQueryParameter("action")
        Logger.printDebug { "action: $action" }

        val language = uri.getQueryParameter("language")
        Logger.printDebug { "language: $language" }

        val anonymousToken = request.header("Anonymous-Token")
        Logger.printDebug { "anonymousToken: $anonymousToken" }

        val entitlementInfo = getEntitlementInfo(chain, action, language, anonymousToken)
        Logger.printDebug { "entitlementInfo: $entitlementInfo" }

        val videoDetails = getVideoDetails(chain, contentId, language)
        Logger.printDebug { "videoDetails: $videoDetails" }

        return buildEntitlementInfo(response, entitlementInfo, videoDetails)
    }

    internal companion object {
        const val CONTENT_ID = "58f40f48-99de-4413-b0f4-87e9ce67bd5d"
    }
}
