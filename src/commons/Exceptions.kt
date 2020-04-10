package com.magnojr.foodwithfriends.commons

import io.ktor.http.HttpStatusCode


interface ResponseErrorException {
    val errorResponse: ErrorResponse
}

data class ErrorResponse(val message: String, val statusCode: HttpStatusCode)


abstract class ResponseException(override val message: String) : RuntimeException(message), ResponseErrorException

class NotFoundException(override val message: String) : ResponseException(message) {
    override val errorResponse = ErrorResponse(message, HttpStatusCode.NotFound)
}

