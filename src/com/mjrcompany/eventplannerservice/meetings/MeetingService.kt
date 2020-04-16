package com.mjrcompany.eventplannerservice.meetings

import arrow.core.Either
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.MeetingSubscriberWriterDTO
import com.mjrcompany.eventplannerservice.domain.MeetingWriterDTO
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import java.util.*

object MeetingService {
    val createMeeting = fun(meetingDTO: MeetingWriterDTO): ServiceResult<UUID> {
        return Either.right(
            MeetingRepository.createMeeting(
                meetingDTO
            )
        )
    }

    val updateMeeting = fun(id: UUID, meetingDTO: MeetingWriterDTO): ServiceResult<Unit> {
        return Either.right(
            MeetingRepository.updateMeeting(
                id,
                meetingDTO
            )
        )
    }

    val getMeeting = fun(id: UUID): ServiceResult<Meeting> {
        return Either.right(
            MeetingRepository.getMeetingById(
                id
            )
        )
    }

    val subscribeMeeting = fun(id: UUID, meetingSubscriberDTO: MeetingSubscriberWriterDTO): ServiceResult<Unit> {
        return Either.right(
            MeetingRepository.insertFriendInMeeting(
                id,
                meetingSubscriberDTO
            )
        )
    }


//    val deleteMeeting = fun(id: UUID): ServiceResult<Unit> {
//        // TODO : Only host can remove the meeting
//        return try {
//            Either.right(MeetingRepository.deleteMeeting(id))
//        } catch (e: Exception) {
//            Either.left(DatabaseAccessException(e.message ?: "", e.toString()))
//        }
//    }

    val crudResources = CrudResource(
        createMeeting,
        updateMeeting,
        getMeeting
    )

}


