package com.mjrcompany.eventplannerservice

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.getAlgorithmFromJWK
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.makeJwtVerifier
import com.mjrcompany.eventplannerservice.util.LocalDateAdapter
import com.typesafe.config.ConfigFactory
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.*
import io.ktor.gson.gson
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Routing
import org.slf4j.event.Level
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.time.LocalDate


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(StatusPages) {
        exception<Throwable> { cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    cause.toString(),
                    cause.message
                )
            )
            throw cause
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    install(ContentNegotiation) {
        gson {
            registerTypeAdapter(
                LocalDate::class.java,
                LocalDateAdapter
            )
        }
    }

    if (!testing) {
        DatabaseSetup.initHikariDatasource()
    }

    install(Authentication) {

        val config = ConfigFactory.load()
        val issuer = config.getString("cognito.jwt-validation.issuer")
        val kidAccessToken = config.getString("cognito.jwt-validation.kidAccessToken")
        val algorithm = getAlgorithmFromJWK(kidAccessToken)

        jwt {
            realm = "jwt realm"
            verifier(makeJwtVerifier(issuer, algorithm))
            validate { credential -> JWTPrincipal(credential.payload) }
        }

    }

    install(Routing) {
        meeting()
        dishes()
        users()
        auth()
    }

}

//fun getAlgorithmFromJWK(kidAccessToken: String): Algorithm {
//    val provider =
//        UrlJwkProvider(URL("https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_tUDwHXns5/.well-known/jwks.json"))
//    val jwtAccessToken = provider.get(kidAccessToken)
//    return Algorithm.RSA256(jwtAccessToken?.getPublicKey() as RSAPublicKey?, null);
//}
//
//private fun makeJwtVerifier(issuer: String, algorithm: Algorithm): JWTVerifier = JWT
//    .require(algorithm)
//    .withIssuer(issuer)
//    .build()