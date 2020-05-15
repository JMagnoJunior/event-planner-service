package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.UnauthorizedException
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.Application
import io.ktor.http.HttpStatusCode
import io.ktor.util.KtorExperimentalAPI
import java.util.*


data class IdTokenCognitoPayload(val name: String, val email: String)
data class IdTokenEventPlannerPayload(val name: String, val email: String, val id: UUID)

@KtorExperimentalAPI
val withFriendInEventRequestPermission =
    fun(
        application: Application,
        eventId: UUID,
        idToken: String,
        block: () -> Pair<HttpStatusCode, Any>
    ): Pair<HttpStatusCode, Any> {
        return AuthorizationService(application).checkFriendPermissionToAccessMeeting(eventId, idToken).fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { block() }
        )
    }

@KtorExperimentalAPI
val withHostRequestPermission =
    fun(
        application: Application,
        eventId: UUID,
        idToken: String,
        block: () -> Pair<HttpStatusCode, Any>
    ): Pair<HttpStatusCode, Any> {
        return AuthorizationService(application).checkHostPermission(eventId, idToken).fold(
            { it.errorResponse.statusCode to it.errorResponse },
            { block() }
        )
    }

@KtorExperimentalAPI
class AuthorizationService(val application: Application) {

    val getIdTokenCognitoPayload = fun(idToken: String): ServiceResult<IdTokenCognitoPayload> {
        return application.validateCognitoIdToken(idToken)
            .mapLeft { UnauthorizedException(it.message ?: "invalid id token provided", it.toString()) }
    }

    val  getIdTokenEventPlannerPayload = fun(idToken: String): ServiceResult<IdTokenEventPlannerPayload> {
        return application.validateEventPlannerIdToken(idToken).map {
            IdTokenEventPlannerPayload(it.name, it.email, it.id)
        }.mapLeft { UnauthorizedException(it.message ?: "invalid id token provided", it.toString()) }
    }


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
                                    Either.left(UnauthorizedException("The user is not in this event", ""))
                                }
                            }
                            is None -> {
                                Either.left(NotFoundException("Meeting not found"))
                            }
                        }
                    }
            }


    }


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
