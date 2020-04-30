package com.mjrcompany.eventplannerservice

import io.ktor.http.HttpStatusCode


interface ResponseErrorException {
    val errorResponse: ErrorResponse
}

data class ErrorResponse(
    val message: String?,
    val messageException: String?,
    val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError
)


abstract class ResponseException(override val message: String) : RuntimeException(message),
    ResponseErrorException

class NotFoundException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.NotFound
    )
}

class CreateEntityException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.InternalServerError
    )
}

class FriendNotInMeetingException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.InternalServerError
    )
}

class DatabaseAccessException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.InternalServerError
    )
}

class UnauthorizedException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.Unauthorized
    )
}

class DuplicatedUserException(override val message: String, messageException: String? = null) :
    ResponseException(message) {
    override val errorResponse = ErrorResponse(
        message,
        messageException,
        HttpStatusCode.Conflict
    )
}