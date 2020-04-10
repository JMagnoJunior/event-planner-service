package com.magnojr.foodwithfriends.meetings

import arrow.core.Either
import com.magnojr.foodwithfriends.commons.Meeting
import com.magnojr.foodwithfriends.commons.MeetingWriterDTO
import com.magnojr.foodwithfriends.commons.ResponseErrorException
import com.magnojr.foodwithfriends.core.CrudResource
import java.util.*

object MeetingService {
    val createMeeting = fun(meetingDTO: MeetingWriterDTO): Either<ResponseErrorException, UUID> {
        return Either.right(MeetingRepository.createMeeting(meetingDTO))
    }

    val updateMeeting = fun(id: UUID, meetingDTO: MeetingWriterDTO): Either<ResponseErrorException, Unit> {
        return Either.right(MeetingRepository.updateMeeting(id, meetingDTO))
    }

    val getMeeting = fun(id: UUID): Either<ResponseErrorException, Meeting> {
        return Either.right(MeetingRepository.getMeetingById(id))
    }

    val crudResources = CrudResource(createMeeting, updateMeeting, getMeeting)
}


