package com.mjrcompany.eventplannerservice


import arrow.core.Either
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withFriendInMeetingPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.exchangeAuthCodeForJWTTokens
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateAccessToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateIdToken
import com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.dishes.DishService
import com.mjrcompany.eventplannerservice.domain.MeetingSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.meetings.MeetingService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.*
import java.time.LocalTime


fun Route.meeting() {

    route("/meetings") {


        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID,
            MeetingService.crudResources,
            withHostInMeetingPermissionToModify
        )

        CrudRestApi.createSubResource(
            this, "/tasks",
            getDefaultIdAsUUID,
            getIdAsInt,
            TaskService.crudResources,
            withHostInMeetingPermissionToModify
        )

        authenticate {
            post("{id}/subscribe") {
                val dto = call.receive<MeetingSubscriberWritable>()
                val meetingId = call.getParamIdAsUUID()
                call.withValidRequest(dto) {
                    HttpStatusCode.Accepted to MeetingService.subscribeMeeting(
                        meetingId,
                        it
                    )
                }
            }
        }

        authenticate {

            route("{id}/tasks") {

                post("/{subId}/accept") {
                    val dto = call.receive<TaskOwnerWritable>()
                    val meetingId = call.getParamIdAsUUID()
                    val taskId = call.getParamSubIdAsInt()
                    val headers = call.request.headers
                    val idToken = headers["X-Id-Token"] ?: " "

                    call.withFriendInMeetingPermission(meetingId, idToken) {
                        call.withValidRequest(dto) {
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
    route("/api/v1/auth") {
        get("/") {

            val authCode = call.request.queryParameters["code"] ?: ""

            val jwtTokens = exchangeAuthCodeForJWTTokens(authCode)


            println("access token: ${jwtTokens.access_token}")
            println("id token: ${jwtTokens.id_token}")

            val accessTokenValidationResult = validateAccessToken(jwtTokens.access_token)
            if (accessTokenValidationResult is Either.Left) {
                call.respondText(accessTokenValidationResult.a.toString())
            }

            val result = when (val idTokenPayload = validateIdToken(jwtTokens.id_token)) {
                is Either.Left -> Either.left(Unit)
                is Either.Right -> UserService.upsertUserFromIdPayload(idTokenPayload.b)
            }

            if (result is Either.Left) {
                call.respondText(result.a.toString())
            }

            call.respondRedirect("test", permanent = false)
        }
    }
}

