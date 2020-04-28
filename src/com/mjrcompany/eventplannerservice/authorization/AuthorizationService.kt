package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.UnauthorizedException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateCognitoIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateEventPlannerIdToken
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.Application
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import java.util.*


data class IdTokenPayload(val name: String, val email: String)


@KtorExperimentalAPI
val withFriendInEventRequestPermission =
    fun(
        application: Application,
        meetingId: UUID,
        idToken: String,
        block: () -> Pair<HttpStatusCode, Any>
    ): Pair<HttpStatusCode, Any> {
        return AuthorizationService(application).checkFriendPermissionToAccessMeeting(meetingId, idToken).fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { block() }
        )
    }

@KtorExperimentalAPI
val withHostRequestPermission =
    fun(
        application: Application,
        meetingId: UUID,
        idToken: String,
        block: () -> Pair<HttpStatusCode, Any>
    ): Pair<HttpStatusCode, Any> {
        return AuthorizationService(application).checkHostPermission(meetingId, idToken).fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { block() }
        )
    }

class AuthorizationService(val application: Application) {

    @KtorExperimentalAPI
    val checkHostPermission = fun(meetingId: UUID, idToken: String): ServiceResult<Unit> {

        return application.validateEventPlannerIdToken(idToken)
            .fold(
                { Either.left(UnauthorizedException(it.message ?: "", it.toString())) },
                {
                    UserService.getUserByEmail(it.email).flatMap {
                        when (it) {
                            is Some -> {
                                Either.right(it.t)
                            }
                            is None -> {
                                Either.left(NotFoundException("User not found"))
                            }
                        }
                    }
                }
            ).flatMap { user ->
                EventService.getEvent(meetingId)
                    .flatMap { meeting ->
                        when (meeting) {
                            is Some -> {
                                if (meeting.t.host.id == user.id) {
                                    Either.right(Unit)
                                } else {
                                    Either.left(UnauthorizedException("The user is not in this meeting", ""))
                                }
                            }
                            is None -> {
                                Either.left(NotFoundException("Meeting not found"))
                            }
                        }
                    }
            }


    }


    @KtorExperimentalAPI
    val checkFriendPermissionToAccessMeeting = fun(meetingId: UUID, idToken: String): ServiceResult<Unit> {

        return application.validateEventPlannerIdToken(idToken)
            .fold(
                { Either.left(UnauthorizedException(it.message ?: "", it.toString())) },
                {
                    UserService.getUserByEmail(it.email).flatMap {
                        when (it) {
                            is Some -> {
                                Either.right(it.t)
                            }
                            is None -> {
                                Either.left(NotFoundException("User not found"))
                            }
                        }

                    }
                }
            ).flatMap { user ->
                EventService.getEvent(meetingId)
                    .flatMap { meeting ->
                        when (meeting) {
                            is Some -> {
                                Either.right(meeting.t)
                            }
                            is None -> {
                                Either.left(NotFoundException("Meeting not found"))
                            }
                        }
                    }.flatMap { m ->
                        if (m.guestInEvents.map { it.id }.contains(user.id)) {
                            Either.right(Unit)
                        } else {
                            Either.left(UnauthorizedException("The user is not in this meeting", ""))
                        }

                    }
            }

    }

}
