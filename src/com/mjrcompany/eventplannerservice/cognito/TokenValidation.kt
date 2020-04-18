package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito

import arrow.core.Either
import arrow.core.flatMap
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.IdTokenPayload
import com.typesafe.config.ConfigFactory
import java.net.URL
import java.security.interfaces.RSAPublicKey

fun validateIdToken(idToken: String): Either<JWTVerificationException, IdTokenPayload> {
    val config = ConfigFactory.load()
    val kidIdToken = config.getString("cognito.jwt-validation.kidIdToken")
        ?: throw RuntimeException("idToken not found. check the config file!")

    val jwt = validateToken(kidIdToken, idToken)

    return jwt.flatMap {
        val email = it.getClaim("email").asString() ?: throw RuntimeException("Invalid token. Email is missing")
        val name = it.getClaim("name").asString() ?: throw RuntimeException("Invalid token. Email is missing")
        Either.right(IdTokenPayload(name, email))
    }
}

fun validateAccessToken(token: String): Either<JWTVerificationException, DecodedJWT> {
    val config = ConfigFactory.load()
    val kidAccessToken = config.getString("cognito.jwt-validation.kidAccessToken")
        ?: throw RuntimeException("accessToken not found. check the config file!")

    return validateToken(kidAccessToken, token)
}

private fun validateToken(
    kidToken: String,
    token: String
): Either<JWTVerificationException, DecodedJWT> {
    val config = ConfigFactory.load()
    val issuer = config.getString("cognito.jwt-validation.issuer")

    val algorithm = getAlgorithmFromJWK(kidToken)
    val jwt = try {
        makeJwtVerifier(issuer, algorithm)
            .verify(token)

    } catch (e: JWTVerificationException) {
        return Either.left(e)
    }
    return Either.right(jwt)
}

fun getAlgorithmFromJWK(kid: String): Algorithm {

    val config = ConfigFactory.load()
    val jwtProvider = config.getString("cognito.jwt-validation.jwtProvider")

    val provider =
        UrlJwkProvider(URL(jwtProvider))
    val jwtAccessToken = provider.get(kid)
    return Algorithm.RSA256(jwtAccessToken?.getPublicKey() as RSAPublicKey?, null)
}

fun makeJwtVerifier(issuer: String, algorithm: Algorithm): JWTVerifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .acceptLeeway(5)
    .build()


