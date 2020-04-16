package com.mjrcompany.eventplannerservice.meetings

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class MeetingServiceTest : RootTestDefinition() {

    @Test
    fun `it should should create a lunch`() {
        val hostId = TestDatabaseHelper.addUser(UUID.randomUUID())
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID())
        val createMeeting = TestDatabaseHelper.getDefaultCreateMeetingDTO(hostId, dishId)

        val id = MeetingService.createMeeting(createMeeting)
            .toOption()
            .getOrElse { throw RuntimeException("Erro creating lunch") }

        val meeting = TestDatabaseHelper.queryMeetingWithoutTasks(id)

        assertEquals(hostId, meeting.host.id)
        assertEquals(dishId, meeting.dish?.id)
        assertEquals(createMeeting.date, meeting.date)
        assertEquals(createMeeting.description, meeting.description)
        assertEquals(createMeeting.maxNumberFriends, meeting.maxNumberFriend)
    }
}

