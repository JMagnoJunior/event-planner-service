package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.generateEventPlannerIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.validateAccessToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.validateCognitoIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.exchangeAuthCodeForJWTTokens
import com.mjrcompany.eventplannerservice.users.UserService
import com.typesafe.config.ConfigFactory
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI

@KtorExperimentalAPI
fun Route.auth() {
    route("/v1/auth") {
        get("/") {

            val log = this.application.log
            val config = ConfigFactory.load()
            val appUrl = config.getString("app.url")

            val authCode = call.request.queryParameters["code"] ?: ""

            val jwtTokens = exchangeAuthCodeForJWTTokens(authCode)

            log.info("access token: ${jwtTokens.access_token}")
            log.info("id token: ${jwtTokens.id_token}")

            val accessTokenValidationResult = this.application.validateAccessToken(jwtTokens.access_token)
            if (accessTokenValidationResult is Either.Left) {
                call.respondText(accessTokenValidationResult.a.toString())
            }

            val idTokenResult = this.application.validateCognitoIdToken(jwtTokens.id_token)
                .flatMap { UserService.upsertUserFromIdPayload(it) }


            when (idTokenResult) {
                is Either.Left -> call.respondText(idTokenResult.a.toString())
                is Either.Right -> {
                    call.respondRedirect(
                        "${appUrl}?access_token=${jwtTokens.access_token}&id_token=${application.generateEventPlannerIdToken(
                            idTokenResult.b
                        )}&refresh_token=${jwtTokens.refreshToken}",
                        permanent = false
                    )
                }
            }

        }
    }
}

