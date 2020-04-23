package com.mjrcompany.eventplannerservice.event

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class MeetingServiceTest : RootTestDefinition() {

    @Test
    fun `it should should create a lunch`() {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        val hostId = TestDatabaseHelper.addUser(UUID.randomUUID())
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID())
        val createEvent = TestDatabaseHelper.getDefaultCreateMeetingDTO(hostId, dishId)


        val id = EventService.createEvent(createEvent)
            .toOption()
            .getOrElse { throw RuntimeException("Erro creating lunch") }

        val event = TestDatabaseHelper.queryMeetingWithoutTasks(id)

        assertEquals(hostId, event.host.id)
        assertEquals(dishId, event.subject?.id)
        assertEquals(createEvent.date.format(formatter), event.date.format(formatter))
        assertEquals(createEvent.title, event.title)
        assertEquals(createEvent.maxNumberGuest, event.maxNumberGuest)
    }
}

