package com.mjrcompany.eventplannerservice.core

import arrow.core.Either
import arrow.core.Option
import arrow.core.Some
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put
import org.jetbrains.exposed.sql.SortOrder
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
        crossinline orderBy: (ApplicationCall) -> Option<Pair<Sortable, SortOrder>> = { Option.empty() },
        crossinline withPermissionToModify: (Application, ID, String, () -> Pair<HttpStatusCode, Any>) -> Pair<HttpStatusCode, Any> = { _, _, _, f -> f() }
    ) {
        r {
            authenticate {
                post("/") {
                    val dto = call.receive<T>()
                    val (status, body) = withValidRequest(dto) { HttpStatusCode.Created to resource.create(it) }
                    call.respond(status, body)
                }
            }
            get("/{id}") {
                val id = getId(call)
                val (status, body) = withErrorTreatment {
                    HttpStatusCode.OK to resource.get(id).flatMap {
                        it.fold(
                            { Either.left(NotFoundException("Resource not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
                }
                call.respond(status, body)
            }


            get("/") {
                val (status, body) = withErrorTreatment {
                    val pagination = orderBy(call).map { (field, sortOrder) ->

                        val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
                        val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT

                        Pagination(page, totalItems, OrderBy(field, sortOrder))
                    }
                    if (pagination is Some) {
                        HttpStatusCode.OK to resource.getAll(pagination.t)
                    } else {
                        throw NotImplementedError("Get all not imeplemtend for this resource")
                    }

                }
                call.respond(status, body)
            }

            authenticate {
                put("/{id}") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    val headers = call.request.headers
                    val idToken = headers["X-id-token"] ?: " "
                    val (status, body) = withPermissionToModify(this.application, id, idToken) {
                        withValidRequest(dto) { HttpStatusCode.Accepted to resource.update(id, it) }
                    }
                    call.respond(status, body)
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
        crossinline orderBy: (ApplicationCall) -> Option<Pair<Sortable, SortOrder>> = { Option.empty() },
        crossinline withPermissionToModify: (Application, ID, String, () -> Pair<HttpStatusCode, Any>) -> Pair<HttpStatusCode, Any> = { _, _, _, f -> f() }
    ) {
        r {
            authenticate {
                post("/{id}/$subResourceName") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    val response = withValidRequest(dto) {
                        HttpStatusCode.Created to resource.create(id, it)
                    }
                    call.respond(response.first, response.second)
                }
            }
            get("/{id}/$subResourceName/{subId}") {
                val id = getId(call)
                val subId = getSubId(call, "subId")
                val (status, body) = withErrorTreatment {
                    HttpStatusCode.OK to resource.get(subId, id).flatMap {
                        it.fold(
                            { Either.left(NotFoundException("Resource not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
                }
                call.respond(status, body)
            }
            get("/{id}/$subResourceName") {
                val id = getId(call)
                val (status, body) = withErrorTreatment {
                    val pagination = orderBy(call).map { (field, sortOrder) ->

                        val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
                        val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT

                        Pagination(page, totalItems, OrderBy(field, sortOrder))
                    }
                    if (pagination is Some) {
                        HttpStatusCode.OK to resource.getAll(id, pagination.t)
                    } else {
                        throw NotImplementedError("Get all not imeplemtend for this resource")
                    }

                }
                call.respond(status, body)

            }
            authenticate {
                put("/{id}/$subResourceName/{subId}") {
                    val dto = call.receive<T>()
                    val id = getId(call)
                    val subId = getSubId(call, "subId")

                    val headers = call.request.headers
                    val idToken = headers["X-Id-Token"] ?: " "
                    val (status, body) = withPermissionToModify(this.application, id, idToken) {
                        withValidRequest(dto) {
                            HttpStatusCode.Accepted to resource.update(subId, id, it)
                        }
                    }
                    call.respond(status, body)
                }
            }
        }
    }

}

class CrudResource<T, ID>(
    val create: (T) -> ServiceResult<Any>,
    val update: (ID, T) -> ServiceResult<Any>,
    val get: (ID) -> ServiceResult<Option<Any>>,
    val getAll: (Pagination) -> Either<ResponseErrorException, Any>
)

class CrudSubResource<T, ID, IDS>(
    val create: (ID, T) -> Either<ResponseErrorException, Any>,
    val update: (IDS, ID, T) -> Either<ResponseErrorException, Any>,
    val get: (IDS, ID) -> Either<ResponseErrorException, Option<Any>>,
    val getAll: (ID, Pagination) -> Either<ResponseErrorException, Any>
)

fun <T : Validable<T>> withValidRequest(
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
