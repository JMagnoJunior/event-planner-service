package com.mjrcompany.eventplannerservice.core

import arrow.core.Either
import arrow.core.Option
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.AuthorizationService
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import java.util.*

val getDefaultIdAsUUID: (ApplicationCall) -> UUID = { UUID.fromString(it.parameters["id"]) }
val getIdAsUUID: (ApplicationCall, String) -> UUID = { call, paramName -> UUID.fromString(call.parameters[paramName]) }
val getIdAsInt: (ApplicationCall, String) -> Int = { call, paramName -> call.parameters[paramName]?.toInt() ?: 0 }

fun ApplicationCall.getParamIdAsUUID(): UUID {
    return getIdAsUUID(this, "id")
}

fun ApplicationCall.getParamSubIdAsInt(): Int {
    return getIdAsInt(this, "subId")
}

object CrudRestApi {
    inline fun <reified T : Validable<T>, ID> createResource(
        r: Route,
        crossinline getId: (ApplicationCall) -> ID,
        resource: CrudResource<T, ID>,
        crossinline withPermissionToModify: (ApplicationCall, ID, String, suspend () -> Unit) -> Any = { _, _, _, f -> suspend { f() } }
    ) {
        r {
            authenticate {
                post("/") {
                    val dto = call.receive<T>()
                    call.withValidRequest(dto) { HttpStatusCode.Created to resource.create(it) }
                }
            }
            get("/{id}") {
                val id = getId(call)
                call withErrorTreatment {
                    resource.get(id).flatMap {
                        it.fold(
                            { Either.left(NotFoundException("Resource not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
                }
            }
            authenticate {
                put("/{id}") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    val headers = call.request.headers
                    val idToken = headers["X-id-token"] ?: " "
                    withPermissionToModify(call, id, idToken) {
                        call.withValidRequest(dto) { HttpStatusCode.Accepted to resource.update(id, it) }
                    }
                }
            }
        }
    }

    inline fun <reified T : Validable<T>, ID, IDS> createSubResource(
        r: Route,
        subResourceName: String,
        crossinline getId: (ApplicationCall) -> ID,
        crossinline getSubId: (ApplicationCall, String) -> IDS,
        resource: CrudSubResource<T, ID, IDS>,
        crossinline withPermissionToModify: (ApplicationCall, ID, String, suspend () -> Unit) -> Any = { _, _, _, f -> suspend { f() } }
    ) {
        r {
            authenticate {
                post("/{id}/$subResourceName") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    call.withValidRequest(dto) { HttpStatusCode.Created to resource.create(id, it) }
                }
            }
            get("/{id}/$subResourceName/{subId}") {
                val id = getId(call)
                val subId = getSubId(call, "subId")
                call withErrorTreatment {
                    resource.get(subId, id).flatMap {
                        it.fold(
                            { Either.left(NotFoundException("Resource not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
                }
            }
            get("/{id}/$subResourceName") {
                val id = getId(call)
                call withErrorTreatment { resource.getAll(id) }
            }
            authenticate {
                put("/{id}/$subResourceName/{subId}") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    val subId = getSubId(call, "subId")

                    val headers = call.request.headers
                    val idToken = headers["X-Id-Token"] ?: " "

                    withPermissionToModify(call, id, idToken) {

                        call.withValidRequest(dto) {
                            HttpStatusCode.Accepted to resource.update(
                                subId,
                                id,
                                it
                            )
                        }

                    }
                }
            }
        }
    }

}

class CrudResource<T, ID>(
    val create: (T) -> AnyServiceResult,
    val update: (ID, T) -> AnyServiceResult,
    val get: (ID) -> AnyOptionServiceResult
)

class CrudSubResource<T, ID, IDS>(
    val create: (ID, T) -> Either<ResponseErrorException, Any>,
    val update: (IDS, ID, T) -> Either<ResponseErrorException, Any>,
    val get: (IDS, ID) -> Either<ResponseErrorException, Option<Any>>,
    val getAll: (ID) -> Either<ResponseErrorException, Any>
)

suspend infix fun ApplicationCall.withErrorTreatment(block: () -> AnyServiceResult) {
    val (status, response) = convertingServiceResultToResponseData(
        HttpStatusCode.OK,
        block
    )
    return this.respond(status, response)
}

suspend fun <T : Validable<T>> ApplicationCall.withValidRequest(
    dto: T,
    block: (T) -> ResponseDataFromService
) {
    val (status, response) = validRequest(dto) { block(dto) }
    this.respond(status, response)
}

val withHostInMeetingPermissionToModify =
    fun(call: ApplicationCall, meetingId: UUID, idToken: String, block: suspend () -> Unit) {
        AuthorizationService.checkHostPermission(meetingId, idToken).fold(
            { suspend { call.respond(it.errorResponse.statusCode, it.errorResponse) } },
            { suspend { block() } }
        )
    }

private fun <T : Validable<T>> validRequest(
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

private fun convertingServiceResultToResponseData(
    successStatus: HttpStatusCode,
    block: () -> AnyServiceResult
): ResponseData {
    return block()
        .fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { successStatus to it }
        )
}


typealias ServiceResult<R> = Either<ResponseErrorException, R>

private typealias ResponseData = Pair<HttpStatusCode, Any>

private typealias AnyServiceResult = ServiceResult<Any>
private typealias AnyOptionServiceResult = ServiceResult<Option<Any>>

private typealias ResponseDataFromService = Pair<HttpStatusCode, AnyServiceResult>
