package app.morphe.extension.hoichoi.interceptors

import android.net.Uri
import app.morphe.extension.hoichoi.repos.ContentRepo
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
        if (
            !(request.method == "POST" &&
                uri.host == "prod-api.hoichoi.dev" &&
                uri.pathSegments == listOf("contents", "api", "v1", "entitlement") &&
                response.code == 401)
        )
            return response

        response.closeQuietly()
        val contentId = uri.getQueryParameter("contentId")!!
        Utils.showToastShort("contentId: $contentId")

        val action = uri.getQueryParameter("action")
        Logger.printDebug { "action: $action" }

        val language = uri.getQueryParameter("language")
        Logger.printDebug { "language: $language" }

        val anonymousToken = request.header("Anonymous-Token")
        Logger.printDebug { "anonymousToken: $anonymousToken" }

        try {
            ContentRepo(chain, contentId, action, language, anonymousToken).also {
                Logger.printInfo { "videoDetails: ${it.videoDetails}" }
                Logger.printInfo { "freeEntitlementInfo: ${it.freeEntitlementInfo}" }
                return it.getResponse(response)
            }
        } catch (e: Exception) {
            Logger.printException({ "contentRepo failure" }, e)
            return response
        }
    }
}
