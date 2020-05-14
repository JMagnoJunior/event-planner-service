package com.mjrcompany.eventplannerservice.core

import arrow.core.Either
import com.mjrcompany.eventplannerservice.ResponseErrorException
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import java.util.*


val getIdAsUUID: (ApplicationCall, String) -> UUID = { call, paramName -> UUID.fromString(call.parameters[paramName]) }
val getIdAsInt: (ApplicationCall, String) -> Int = { call, paramName -> call.parameters[paramName]?.toInt() ?: 0 }

fun ApplicationCall.getParamIdAsUUID(): UUID {
    return getIdAsUUID(this, "id")
}

fun ApplicationCall.getParamIdAsUUID(paraName: String): UUID {
    return getIdAsUUID(this, paraName)
}

fun ApplicationCall.getParamSubIdAsInt(): Int {
    return getIdAsInt(this, "subId")
}

fun <T : Validatable<T>> withValidRequest(
    writable: T,
    block: (dto: T) -> ResponseDataFromService
): Pair<HttpStatusCode, Any> {

    val (successStatus, f) = block(writable)
    return writable.validation().fold(
        { HttpStatusCode.BadRequest to it },
        {
            convertingServiceResultToResponseData(
                successStatus
            ) { f }
        })
}

fun withErrorTreatment(
    block: () -> ResponseDataFromService
): Pair<HttpStatusCode, Any> {
    val (successStatus, f) = block()
    return convertingServiceResultToResponseData(
        successStatus
    ) { f }

}

private fun convertingServiceResultToResponseData(
    successStatus: HttpStatusCode,
    block: () -> ServiceResult<Any>
): ResponseData {
    return block()
        .fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { successStatus to it }
        )
}

typealias ServiceResult<R> = Either<ResponseErrorException, R>

private typealias ResponseData = Pair<HttpStatusCode, Any>

private typealias ResponseDataFromService = Pair<HttpStatusCode, ServiceResult<Any>>
