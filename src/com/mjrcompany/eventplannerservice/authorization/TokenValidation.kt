package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization

import arrow.core.Either
import arrow.core.flatMap
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.mjrcompany.eventplannerservice.domain.User
import io.ktor.application.Application
import io.ktor.util.KtorExperimentalAPI
import java.net.URL
import java.security.interfaces.RSAPublicKey
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*


@KtorExperimentalAPI
fun Application.validateCognitoIdToken(idToken: String): Either<JWTVerificationException, IdTokenPayload> {
    val config = this.environment.config
    val kidIdToken = config.property("cognito.jwt-validation.kidIdToken").getString()
    val jwt = validateCognitoToken(kidIdToken, idToken)

    return jwt.flatMap {
        val email = it.getClaim("email").asString() ?: throw RuntimeException("Invalid token. Email is missing")
        val name = it.getClaim("name").asString() ?: throw RuntimeException("Invalid token. Email is missing")
        Either.right(IdTokenPayload(name, email))
    }
}

@KtorExperimentalAPI
fun Application.validateEventPlannerIdToken(idToken: String): Either<JWTVerificationException, IdTokenPayload> {
    val jwt = validateEventPlannerToken(idToken)
    return jwt.flatMap {
        val email = it.getClaim("email").asString() ?: throw RuntimeException("Invalid token. Email is missing")
        val name = it.getClaim("name").asString() ?: throw RuntimeException("Invalid token. Name is missing")
        Either.right(IdTokenPayload(name, email))
    }
}

@KtorExperimentalAPI
fun Application.validateAccessToken(token: String): Either<JWTVerificationException, DecodedJWT> {
    val config = this.environment.config
    val kidAccessToken = config.property("cognito.jwt-validation.kidAccessToken").getString()

    return validateCognitoToken(kidAccessToken, token)
}

@KtorExperimentalAPI
private fun Application.validateCognitoToken(
    kidToken: String,
    token: String
): Either<JWTVerificationException, DecodedJWT> {
    val config = this.environment.config
    val issuer = config.property("cognito.jwt-validation.issuer").getString()
    val algorithm = getAlgorithmFromJWK(kidToken)

    val jwt = try {
        makeJwtVerifier(
            issuer,
            algorithm
        )
            .verify(token)

    } catch (e: JWTVerificationException) {
        return Either.left(e)
    }
    return Either.right(jwt)
}

@KtorExperimentalAPI
private fun Application.validateEventPlannerToken(token: String): Either<JWTVerificationException, DecodedJWT> {
    val config = this.environment.config
    val issuer = config.property("event-planner.jwt.issue").getString()
    val algorithm = Algorithm.HMAC256(config.property("event-planner.jwt.secret").getString())

    val jwt = try {
        makeJwtVerifier(
            issuer,
            algorithm
        )
            .verify(token)

    } catch (e: JWTVerificationException) {
        return Either.left(e)
    }
    return Either.right(jwt)
}

@KtorExperimentalAPI
fun Application.getAlgorithmFromJWK(kid: String): Algorithm {

    val config = this.environment.config
    val jwtProvider = config.property("cognito.jwt-validation.jwtProvider").getString()

    return if (jwtProvider.isNotBlank()) {
        val provider = UrlJwkProvider(URL(jwtProvider))
        val jwtAccessToken = provider.get(kid)
        Algorithm.RSA256(jwtAccessToken?.getPublicKey() as RSAPublicKey?, null)
    } else {
        Algorithm.HMAC256(config.property("test.jwt-validation.secret").getString())
    }
}

@KtorExperimentalAPI
fun Application.generateEventPlannerIdToken(user: User): String {
    val config = this.environment.config


    val zone = ZoneId.of("Europe/Berlin")
    val expireTime = ZonedDateTime.now(zone).plusHours(4 )

    return JWT.create()
        .withClaim("id", user.id.toString())
        .withClaim("email", user.email)
        .withClaim("name", user.name)
        .withIssuer(config.property("event-planner.jwt.issue").getString())
        .withExpiresAt(Date.from(expireTime.toInstant()))
        .sign(Algorithm.HMAC256(config.property("event-planner.jwt.secret").getString()))
}

fun makeJwtVerifier(issuer: String, algorithm: Algorithm): JWTVerifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .acceptLeeway(5)
    .build()


