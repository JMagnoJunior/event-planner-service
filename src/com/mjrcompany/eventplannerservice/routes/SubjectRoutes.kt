package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.UserIdAttributeKey
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.core.getParamIdAsUUID
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import com.mjrcompany.eventplannerservice.core.withValidRequest
import com.mjrcompany.eventplannerservice.domain.SubjectValidatable
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import com.mjrcompany.eventplannerservice.subjects.SubjectService.createSubject
import com.mjrcompany.eventplannerservice.withIdToken
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.SortOrder
import java.util.*


@KtorExperimentalAPI
fun Route.subjects() {
    route("/subjects") {
        authenticate {
            withIdToken {
                post("/") {
                    val subject = call.receive<SubjectValidatable>()
                    val userId = call.attributes.get(UserIdAttributeKey)

                    val (status, body) = withValidRequest(subject) {
                        val subjectWritable = SubjectWritable(
                            subject.name,
                            subject.details,
                            userId,
                            subject.imageUrl
                        )
                        HttpStatusCode.Created to createSubject(subjectWritable)
                    }

                    call.respond(status, body)

                }
            }
        }

        get("/") {
            val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
            val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT
            val userId =
                call.parameters["userId"] ?: throw BadRequestException("Provide a valid host id")

            val pagination = Pagination(page, totalItems, OrderBy(SubjectOrderBy.Name, SortOrder.ASC))

            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to SubjectService.getAll(UUID.fromString(userId), pagination)
            }
            call.respond(status, body)
        }

        get("/{id}") {

            val id = call.getParamIdAsUUID("id")

            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to SubjectService.getSubject(id)
                    .flatMap {
                        it.fold(
                            { Either.left(NotFoundException("Subject not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
            }
            call.respond(status, body)
        }
    }
}


