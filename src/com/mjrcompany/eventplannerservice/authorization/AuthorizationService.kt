package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.UnauthorizedException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateIdToken
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.meetings.MeetingService
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.ApplicationCall
import io.ktor.response.respond
import java.util.*


data class IdTokenPayload(val name: String, val email: String)

suspend fun ApplicationCall.withFriendInMeetingPermission(meetingId: UUID, idToken: String, block: suspend () -> Unit) {
    AuthorizationService.checkFriendPermissionToAccessMeeting(meetingId, idToken).fold(
        { this.respond(it.errorResponse.statusCode, it.errorResponse) },
        { block() }
    )
}

suspend fun ApplicationCall.withHostPermission(meetingId: UUID, idToken: String, block: () -> Unit) {
    AuthorizationService.checkHostPermission(meetingId, idToken).fold(
        { this.respond(it.errorResponse.statusCode, it.errorResponse) },
        { block() }
    )
}

object AuthorizationService {

    val checkHostPermission = fun(meetingId: UUID, idToken: String): ServiceResult<Unit> {


        return validateIdToken(idToken)
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
                MeetingService.getMeeting(meetingId)
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


    val checkFriendPermissionToAccessMeeting = fun(meetingId: UUID, idToken: String): ServiceResult<Unit> {

        return validateIdToken(idToken)
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
                MeetingService.getMeeting(meetingId)
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
                        if (m.friends.contains(user)) {
                            Either.right(Unit)
                        } else {
                            Either.left(UnauthorizedException("The user is not in this meeting", ""))
                        }

                    }
            }

    }

}
