package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import com.mjrcompany.eventplannerservice.UserIdAttributeKey
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.SubjectDTO
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import com.mjrcompany.eventplannerservice.core.withValidRequest
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import com.mjrcompany.eventplannerservice.withIdToken
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.SortOrder


@KtorExperimentalAPI
fun Route.subjects() {
    route("/subjects") {
        authenticate {
            withIdToken {
                post("/") {
                    val subject = call.receive<SubjectDTO>()
                    val userId = call.attributes.get(UserIdAttributeKey)

                    val (status, body) = withValidRequest(subject) {
                        val subjectWritable = SubjectWritable(
                            subject.name,
                            subject.details,
                            userId,
                            subject.imageUrl
                        )
                        HttpStatusCode.Created to SubjectService.createSubject(subjectWritable)
                    }

                    call.respond(status, body)

                }

                get("/") {
                    val userId = call.attributes.get(UserIdAttributeKey)
                    val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
                    val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT

                    val pagination = Pagination(page, totalItems, OrderBy(SubjectOrderBy.Name, SortOrder.ASC))

                    val (status, body) = withErrorTreatment {
                        HttpStatusCode.OK to SubjectService.getAll(userId, pagination)
                    }
                    call.respond(status, body)
                }
            }
        }
    }
}


