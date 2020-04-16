package com.mjrcompany.eventplannerservice.core

import arrow.core.Either
import com.mjrcompany.eventplannerservice.ResponseErrorException
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

fun ApplicationCall.getParamSubIdAsInt(): Int {
    return getIdAsInt(this, "subId")
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
            put("/{id}/$subResourceName/{subId}") {
                val dto = call.receive<T>()
                val id = getId(call)
                val subId = getSubId(call, "subId")
                call.withValidRequest(dto) { HttpStatusCode.Accepted to resource.update(subId, id, it) }
            }
        }
    }

}

class CrudResource<T, ID>(
    val create: (T) -> AnyServiceResult,
    val update: (ID, T) -> AnyServiceResult,
    val get: (ID) -> AnyServiceResult
)

class CrudSubResource<T, ID, IDS>(
    val create: (ID, T) -> Either<ResponseErrorException, Any>,
    val update: (IDS, ID, T) -> Either<ResponseErrorException, Any>,
    val get: (IDS, ID) -> Either<ResponseErrorException, Any>,
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

suspend inline fun <reified T : Validable<T>> fromValidRequest(
    call: ApplicationCall,
    crossinline block: (dto: T) -> ResponseDataFromService
) {
    val dto = call.receive<T>()

    val (status, response) = validRequest(dto) { block(dto) }
    call.respond(status, response)
}

//FIXME : This class was set public because of fromValidRequest method which is inline for the reinfied
// used for call.receive
fun <T : Validable<T>> validRequest(
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

private typealias ResponseDataFromService = Pair<HttpStatusCode, AnyServiceResult>


