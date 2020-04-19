package com.mjrcompany.eventplannerservice.meetings

import arrow.core.Option
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.MeetingSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.MeetingWritable
import org.slf4j.LoggerFactory
import java.util.*


object MeetingService {
    private val log = LoggerFactory.getLogger(MeetingService::class.java)

    val createMeeting = fun(meeting: MeetingWritable): ServiceResult<UUID> {
        val result = withDatabaseErrorTreatment {
            MeetingRepository.createMeeting(
                meeting
            )
        }

        if (result.isLeft()) {
            log.error("Error creating meeting $meeting")
        }

        return result
    }

    val updateMeeting = fun(id: UUID, meeting: MeetingWritable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            MeetingRepository.updateMeeting(
                id,
                meeting
            )
        }

        if (result.isLeft()) {
            log.error("Error updating meeting $meeting")
        }

        return result
    }

    val getMeeting = fun(id: UUID): ServiceResult<Option<Meeting>> {
        log.debug("Querying the meeting: $id")
        val result = withDatabaseErrorTreatment {
            MeetingRepository.getMeetingById(id)
        }

        result.map { if (it.isEmpty()) log.info("meeting not found") }
        return result
    }

    val subscribeMeeting = fun(id: UUID, meetingSubscriber: MeetingSubscriberWritable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            MeetingRepository.insertFriendInMeeting(
                id,
                meetingSubscriber
            )
        }
        if (result.isLeft()) {
            log.error("Error subscribing to meeting. meeting Id: $id , friend: $meetingSubscriber")
        }

        return result

    }

    val crudResources = CrudResource(
        createMeeting,
        updateMeeting,
        getMeeting
    )

}


