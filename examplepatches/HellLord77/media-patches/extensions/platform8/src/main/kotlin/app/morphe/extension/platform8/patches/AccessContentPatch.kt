@file:JvmName("AccessContentPatch")

package app.morphe.extension.platform8.patches

import android.net.Uri
import app.morphe.extension.platform8.repos.ContentRepo
import app.morphe.extension.shared.Logger
import app.morphe.extension.shared.Utils
import com.android.volley.Network
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.VolleyError
import org.json.JSONObject

@Suppress("unused")
@Throws(VolleyError::class)
fun performRequest(self: Network, request: Request<*>): NetworkResponse {
    Logger.printDebug { "request: $request" }
    val response = self.performRequest(request)
    Logger.printDebug { "response: $response" }

    val uri = Uri.parse(request.url)
    if (
        !(request.method == Request.Method.GET &&
            uri.host == "apps.digivive.com" &&
            uri.pathSegments == listOf("platform8_dynamic", "geturlv4") &&
            response.statusCode == 200 &&
            (JSONObject(response.data.decodeToString())["error_code"] as String).toInt() != 200)
    )
        return response

    val chargeCode = uri.getQueryParameter("charge_code")!!
    Utils.showToastShort("chargeCode: $chargeCode")

    val catalogue = uri.getQueryParameter("catlogue")
    Logger.printDebug { "catalogue: $catalogue" }

    val authorization = request.headers["authorization"]
    Logger.printDebug { "authorization: $authorization" }

    try {
        ContentRepo(self, chargeCode, catalogue, authorization).also {
            Logger.printInfo { "assetData: ${it.assetData}" }
            Logger.printInfo { "cloudFront: ${it.cloudFront}" }
            Logger.printInfo { "seasonData: ${it.seasonData}" }
            return it.response
        }
    } catch (e: Exception) {
        Logger.printException({ "contentRepo failure" }, e)
        return response
    }
}
