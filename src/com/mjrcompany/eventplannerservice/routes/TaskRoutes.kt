package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes

import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.core.getParamIdAsUUID
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import com.mjrcompany.eventplannerservice.core.withValidRequest
import com.mjrcompany.eventplannerservice.tasks.TaskService
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
fun Route.tasks() {

    route("/events/{eventId}/tasks") {
        get("/") {
            val eventId = call.getParamIdAsUUID("eventId")
            val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
            val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT

            val pagination = Pagination(page, totalItems, OrderBy(TaskOrderBy.Id, SortOrder.ASC))

            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to TaskService.getAllTasksOnEvent(eventId, pagination)
            }
            call.respond(status, body)
        }

        authenticate {
            withIdToken {
                post("/") {
                    val newTask = call.receive<TaskDomain.TaskWritable>()
                    val eventId = call.getParamIdAsUUID("eventId")
                    val (status, body) = withValidRequest(newTask) {
                        HttpStatusCode.Created to TaskService.createTask(eventId, newTask)
                    }
                    call.respond(status, body)
                }
            }
        }


    }


}