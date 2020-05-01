package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.AuthorizationService
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withFriendInEventRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withHostRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.EventOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.TaskOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.domain.AcceptGuestInEventWritable
import com.mjrcompany.eventplannerservice.domain.EventSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.put
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI


@KtorExperimentalAPI
fun Route.events() {

    route("/events") {

        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID,
            EventService.crudResources,
            EventOrderBy.orderBy
        )

        authenticate {
            post("/") {
                val event = call.receive<EventDTO>()
                val headers = call.request.headers
                val idToken = headers["X-Id-Token"] ?: " "


                val (status, body) = withValidRequest(event) {
                    HttpStatusCode.Created to AuthorizationService(application).getIdTokenPayload(idToken)
                        .flatMap {
                            EventService.createEvent(
                                it.email,
                                event
                            )
                        }
                }
                call.respond(status, body)
            }

            put("/{id}") {
                val dto = call.receive<EventDTO>()
                val id = call.getParamIdAsUUID()
                val headers = call.request.headers
                val idToken = headers["X-Id-Token"] ?: " "
                val (status, body) = withHostRequestPermission(application, id, idToken) {
                    withValidRequest(dto) { eventDTO ->
                        HttpStatusCode.Accepted to AuthorizationService(application).getIdTokenPayload(idToken)
                            .flatMap { idTokenPayload ->
                                EventService.updateEvent(idTokenPayload.email, id, eventDTO)
                            }
                    }
                }
                call.respond(status, body)
            }
        }

        CrudRestApi.createSubResource(
            this, "/tasks",
            getDefaultIdAsUUID,
            getIdAsInt,
            TaskService.crudResources,
            TaskOrderBy.orderBy,
            withPermissionToModify = withHostRequestPermission
        )

        authenticate {
            post("{id}/subscribe") {
                val dto = call.receive<EventSubscriberWritable>()
                val meetingId = call.getParamIdAsUUID()
                val (status, body) = withValidRequest(dto) {
                    HttpStatusCode.Accepted to EventService.subscribeEvent(meetingId, it)
                }
                call.respond(status, body)
            }

            post("{id}/accept-guest") {
                val dto = call.receive<AcceptGuestInEventWritable>()
                val eventId = call.getParamIdAsUUID()
                val headers = call.request.headers
                val idToken = headers["X-Id-Token"] ?: ""

                if (idToken.isBlank()) {
                    call.respond("provide an id-token")
                }

                val (status, body) =
                    withHostRequestPermission(this.application, eventId, idToken) {
                        withValidRequest(dto) {
                            HttpStatusCode.Accepted to EventService.acceptGuest(eventId, it)
                        }
                    }
                call.respond(status, body)
            }
        }

        route("{id}/tasks") {
            authenticate {
                post("/{subId}/accept") {
                    val dto = call.receive<TaskOwnerWritable>()
                    val eventgId = call.getParamIdAsUUID()
                    val taskId = call.getParamSubIdAsInt()
                    val headers = call.request.headers
                    val idToken = headers["X-Id-Token"] ?: " "

                    if (idToken.isBlank()) {
                        call.respond("provide an id-token")
                    }

                    val (status, body) = withFriendInEventRequestPermission(this.application, eventgId, idToken) {
                        withValidRequest(dto) {
                            HttpStatusCode.Accepted to TaskService.acceptTask(taskId, eventgId, it)
                        }
                    }
                    call.respond(status, body)

                }
            }
        }
    }
}



