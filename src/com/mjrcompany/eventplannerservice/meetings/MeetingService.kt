package com.mjrcompany.eventplannerservice.meetings

import arrow.core.Either
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.MeetingSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.MeetingWritable
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import java.util.*

object MeetingService {
    val createMeeting = fun(meeting: MeetingWritable): ServiceResult<UUID> {
        return Either.right(
            MeetingRepository.createMeeting(
                meeting
            )
        )
    }

    val updateMeeting = fun(id: UUID, meeting: MeetingWritable): ServiceResult<Unit> {
        return Either.right(
            MeetingRepository.updateMeeting(
                id,
                meeting
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

    val subscribeMeeting = fun(id: UUID, meetingSubscriber: MeetingSubscriberWritable): ServiceResult<Unit> {
        return Either.right(
            MeetingRepository.insertFriendInMeeting(
                id,
                meetingSubscriber
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


