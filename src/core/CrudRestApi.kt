package com.magnojr.foodwithfriends.core

import arrow.core.Either
import com.magnojr.foodwithfriends.commons.ResponseErrorException
import io.ktor.application.ApplicationCall
import io.ktor.application.call
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

fun ApplicationCall.getParamIdAsInt(paramName: String): Int {
    return getIdAsInt(this, paramName)
}

object CrudRestApi {
    inline fun <reified T : Validable<T>, ID> createResource(
        r: Route,
        crossinline getId: (ApplicationCall) -> ID,
        resource: CrudResource<T, ID>
    ) {
        r {
            post("/") {
                val dto = call.receive<T>()
                call.withValidRequest(dto) { HttpStatusCode.Created to resource.create(it) }
            }
            get("/{id}") {
                val id = getId(call)
                call withErrorTreatment { resource.get(id) }
            }
            put("/{id}") {
                val dto = call.receive<T>()
                call.withValidRequest(dto) { HttpStatusCode.Accepted to resource.update(getId(call), it) }
            }
        }
    }

    inline fun <reified T : Validable<T>, ID, IDS> createSubResource(
        r: Route,
        subResourceName: String,
        crossinline getId: (ApplicationCall) -> ID,
        crossinline getSubId: (ApplicationCall, String) -> IDS,
        resource: CrudSubResource<T, ID, IDS>
    ) {
        r {
            post("/{id}/$subResourceName") {
                val dto = call.receive<T>()
                val id = getId(call)
                call.withValidRequest(dto) { HttpStatusCode.Created to resource.create(id, it) }
            }
            get("/{id}/$subResourceName/{subId}") {
                val id = getId(call)
                val subId = getSubId(call, "subId")
                call withErrorTreatment { resource.get(subId, id) }
            }
            get("/{id}/$subResourceName") {
                val id = getId(call)
                call withErrorTreatment { resource.getAll(id) }
            }
            put("/{id}/$subResourceName") {
                val dto = call.receive<T>()
                val id = getId(call)
                val subId = getSubId(call, "subId")
                call.withValidRequest(dto) { HttpStatusCode.Accepted to resource.update(subId, id, it) }
            }
        }
    }

}

typealias ResponseData = Pair<HttpStatusCode, Any>

typealias ServiceResult = Either<ResponseErrorException, Any>

typealias ResponseDataFromService = Pair<HttpStatusCode, ServiceResult>

class CrudResource<T, ID>(
    val create: (T) -> ServiceResult,
    val update: (ID, T) -> ServiceResult,
    val get: (ID) -> ServiceResult
)

class CrudSubResource<T, ID, IDS>(
    val create: (ID, T) -> Either<ResponseErrorException, Any>,
    val update: (IDS, ID, T) -> Either<ResponseErrorException, Any>,
    val get: (IDS, ID) -> Either<ResponseErrorException, Any>,
    val getAll: (ID) -> Either<ResponseErrorException, Any>
)


inline fun <T : Validable<T>> validRequest(
    dto: T,
    crossinline block: (dto: T) -> ResponseDataFromService
): Pair<HttpStatusCode, Any> {

    val (customStatus, f) = block(dto)
    val (_, response) = dto.validation()
        .fold(
            { HttpStatusCode.BadRequest to it },
            { errorTreatment { f } }
        )
    return customStatus to response
}

fun errorTreatment(block: () -> ServiceResult): ResponseData {

    return block().fold(
        { it.errorResponse.statusCode to it.errorResponse },
        { HttpStatusCode.OK to it }
    )
}

suspend infix fun ApplicationCall.withErrorTreatment(block: () -> ServiceResult) {
    val (status, response) = errorTreatment(block)
    return this.respond(status, response)
}

suspend infix fun ApplicationCall.with(block: () -> ResponseData) {
    val (status, response) = block()
    this.respond(status, response)
}

suspend fun <T : Validable<T>> ApplicationCall.withValidRequest(
    dto: T,
    block: (T) -> ResponseDataFromService
) {
    val (status, response) = validRequest(dto) { block(dto) }
    this.respond(status, response)
}

suspend inline fun <reified T : Validable<T>> fromValidRequest(
    call: ApplicationCall,
    crossinline block: (dto: T) -> ResponseDataFromService
) {
    val dto = call.receive<T>()
    val (status, response) = validRequest(dto) { block(dto) }
    call.respond(status, response)
}

