package com.mjrcompany.eventplannerservice


import com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.dishes.DishService
import com.mjrcompany.eventplannerservice.domain.MeetingSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.meetings.MeetingService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondRedirect
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route


fun Route.meeting() {

    route("/meetings") {

        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, MeetingService.crudResources
        )
        CrudRestApi.createSubResource(
            this, "/tasks",
            getDefaultIdAsUUID,
            getIdAsInt, TaskService.crudResources
        )

        post("{id}/subscribe") {
            fromValidRequest<MeetingSubscriberWritable>(
                call
            ) {
                val meetingId = call.getParamIdAsUUID()
                HttpStatusCode.Accepted to MeetingService.subscribeMeeting(
                    meetingId,
                    it
                )
            }
        }

        route("{id}/tasks") {
            post("/{subId}/accept") {
                fromValidRequest<TaskOwnerWritable>(
                    call
                ) {
                    val meetingId = call.getParamIdAsUUID()
                    val taskId = call.getParamSubIdAsInt()
                    HttpStatusCode.Accepted to TaskService.acceptTask(
                        taskId,
                        meetingId,
                        it
                    )
                }
            }
        }

    }
}

fun Route.dishes() {
    route("/dishes") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, DishService.crudResources
        )
    }
}

fun Route.users() {
    route("/users") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, UserService.crudResources
        )
    }
}

fun Route.auth() {
    route("/auth") {
        get("/") {
            print("Aqui")
            call.respondRedirect("", permanent = false)
        }
    }
}
