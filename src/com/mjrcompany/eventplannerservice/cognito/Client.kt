package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito

import com.google.gson.Gson
import com.typesafe.config.ConfigFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.coroutines.delay

data class JwtTokensResponse(
    val access_token: String, val refreshToken: String,
    val id_token: String, val token_type: String, val expires_in: Int
)


suspend fun exchangeAuthCodeForJWTTokens(authCode: String): JwtTokensResponse {

    val config = ConfigFactory.load()

    val clientId = config.getString("cognito.client_id")
    val basicAuth = config.getString("cognito.basic_auth")
    val callbackUrl = config.getString("cognito.callback_url")
    val apiUrl = config.getString("cognito.apiUrl")


    val client = HttpClient(Apache) {

        defaultRequest { // this: HttpRequestBuilder ->
            header(
                "Authorization",
                "Basic ${basicAuth}"
            )
        }
    }

    val response = client.post<String>("${apiUrl}/oauth2/token/")
    {
        body = FormDataContent(Parameters.build {
            append("grant_type", "authorization_code")
            append("client_id", clientId)
            append("client_id", clientId)
            append("code", authCode)
            append("redirect_uri", callbackUrl)
        })
    }

    client.close()
    return Gson().fromJson(response, JwtTokensResponse::class.java)

}
