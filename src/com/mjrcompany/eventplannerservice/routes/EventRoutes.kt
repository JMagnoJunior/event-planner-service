package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.EventDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.core.getParamIdAsUUID
import com.mjrcompany.eventplannerservice.core.getParamSubIdAsInt
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import com.mjrcompany.eventplannerservice.core.withValidRequest
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.features.BadRequestException
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import org.jetbrains.exposed.sql.SortOrder
import java.util.*


@KtorExperimentalAPI
fun Route.events() {

    route("/events") {

        get("/{id}") {
            val id = call.getParamIdAsUUID()
            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to EventService.getEvent(id).flatMap {
                    it.fold(
                        { Either.left(NotFoundException("Event not found")) },
                        { resource -> Either.right(resource) }
                    )
                }

            }
            call.respond(status, body)
        }

        get("/") {
            val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
            val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT
            val hostId =
                call.parameters["hostId"] ?: throw BadRequestException("Provide a valid host id")

            val pagination = Pagination(page, totalItems, OrderBy(EventOrderBy.CreateDate, SortOrder.DESC))

            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to EventService.getAllEventsFromUser(UUID.fromString(hostId), pagination)
            }
            call.respond(status, body)
        }

        // Create event does not follow the patter for a regular CRUD resource,
        // because of that the POST and PUT method are not provided by CRUD Api
        authenticate {
            withIdToken {
                post("/") {
                    val event = call.receive<EventDomain.EventValidatable>()
                    val userEmail = call.attributes.get(UserEmailAttributeKey)

                    val (status, body) = withValidRequest(event) {
                        HttpStatusCode.Created to
                                EventService.createEvent(
                                    userEmail,
                                    event
                                )
                    }

                    call.respond(status, body)
                }

                withHostRequestPermission {
                    put("/{id}") {
                        val dto = call.receive<EventDomain.EventValidatable>()
                        val id = call.getParamIdAsUUID()
                        val userEmail = call.attributes.get(UserEmailAttributeKey)

                        val (status, body) = withValidRequest(dto) { eventDTO ->
                            HttpStatusCode.Accepted to EventService.updateEvent(userEmail, id, eventDTO)
                        }

                        call.respond(status, body)
                    }
                }
            }

            post("{id}/subscribe") {
                val dto = call.receive<EventDomain.EventSubscriberWritable>()
                val meetingId = call.getParamIdAsUUID()
                val (status, body) = withValidRequest(dto) {
                    HttpStatusCode.Accepted to EventService.subscribeEvent(meetingId, it)
                }
                call.respond(status, body)
            }

            withHostRequestPermission {
                post("{id}/accept-guest") {
                    val dto = call.receive<EventDomain.AcceptGuestInEventWritable>()
                    val eventId = call.getParamIdAsUUID()

                    val (status, body) = withValidRequest(dto) {
                        HttpStatusCode.Accepted to EventService.acceptGuest(eventId, it)
                    }
                    call.respond(status, body)
                }
            }
        }

        // sub resources: /tasks
        withFriendInEventRequestPermission {
            route("{id}/tasks") {
                authenticate {
                    post("/{subId}/accept") {
                        val dto = call.receive<TaskDomain.TaskOwnerWritable>()
                        val eventId = call.getParamIdAsUUID()
                        val taskId = call.getParamSubIdAsInt()

                        val (status, body) = withValidRequest(dto) {
                            HttpStatusCode.Accepted to TaskService.acceptTask(taskId, eventId, it)
                        }
                        call.respond(status, body)
                    }
                }
            }
        }
    }
}
