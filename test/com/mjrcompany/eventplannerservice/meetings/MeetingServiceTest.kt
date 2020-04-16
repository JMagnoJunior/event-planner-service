package com.mjrcompany.eventplannerservice.meetings

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class MeetingServiceTest : RootTestDefinition() {

    @Test
    fun `A host should should create a lunch`() {
        val hostId = TestDatabaseHelper.addHost(UUID.randomUUID())
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID())
        val createMeetingDTO = TestDatabaseHelper.getDefaultCreateMeetingDTO(hostId, dishId)

        val id = MeetingService.createMeeting(createMeetingDTO)
            .toOption()
            .getOrElse { throw RuntimeException("Erro creating lunch") }

        val meeting = TestDatabaseHelper.queryMeetingWithoutTasks(id)

        assertEquals(hostId, meeting.host.id)
        assertEquals(dishId, meeting.dish?.id)
        assertEquals(createMeetingDTO.date, meeting.date)
        assertEquals(createMeetingDTO.description, meeting.description)
        assertEquals(createMeetingDTO.maxNumberFriends, meeting.maxNumberFriend)

    }
}

