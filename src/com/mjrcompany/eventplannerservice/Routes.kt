package com.mjrcompany.eventplannerservice


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withFriendInEventRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withHostRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.exchangeAuthCodeForJWTTokens
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateAccessToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.EventOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.TaskOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.UsersOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.s3.ImageUploadService
import com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.domain.AcceptGuestInEventWritable
import com.mjrcompany.eventplannerservice.domain.EventSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import com.mjrcompany.eventplannerservice.users.UserService
import com.typesafe.config.ConfigFactory
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route


fun Route.events() {

    route("/events") {


        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID,
            EventService.crudResources,
            EventOrderBy.orderBy,
            withHostRequestPermission
        )

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
                    withHostRequestPermission(eventId, idToken) {
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

                    val (status, body) = withFriendInEventRequestPermission(eventgId, idToken) {
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


fun Route.subjects() {
    route("/subjects") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, SubjectService.crudResources
        )
    }
}

fun Route.users() {
    route("/users") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, UserService.crudResources,
            UsersOrderBy.orderBy
        )

    }
}

fun Route.signedUrl() {
    route("/signed-url") {

        get("/get-image/{image-name}") {
            val imageName = call.parameters["image-name"].toString()
            val result = ImageUploadService.generateSignedGettURL(imageName)
            call.respond(result)
        }

        get("/put-image/{image-name}") {
            val imageName = call.parameters["image-name"].toString()
            val result = ImageUploadService.generateSignedPutURL(imageName)
            call.respond(result)
        }

    }


}

fun Route.auth() {
    route("/v1/auth") {
        get("/") {

            val log = this.application.log
            val config = ConfigFactory.load()
            val appUrl = config.getString("app.url")

            val authCode = call.request.queryParameters["code"] ?: ""

            val jwtTokens = exchangeAuthCodeForJWTTokens(authCode)

            log.info("access token: ${jwtTokens.access_token}")
            log.info("id token: ${jwtTokens.id_token}")

            val accessTokenValidationResult = validateAccessToken(jwtTokens.access_token)
            if (accessTokenValidationResult is Either.Left) {
                call.respondText(accessTokenValidationResult.a.toString())
            }

            val idTokenResult = validateIdToken(jwtTokens.id_token)
                .flatMap { UserService.upsertUserFromIdPayload(it) }

            if (idTokenResult is Either.Left) {
                call.respondText(idTokenResult.a.toString())
            }

            call.respondRedirect(

                "${appUrl}?access_token=${jwtTokens.access_token}&id_token=${jwtTokens.id_token}&refresh_token=${jwtTokens.refreshToken}",
                permanent = false
            )
        }
    }
}

