package com.mjrcompany.eventplannerservice.event

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class EventServiceTest : RootTestDefinition() {

    @Test
    fun `it should should create an event`() {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

        val hostId = TestDatabaseHelper.generateUser(UUID.randomUUID())

        val dishId = TestDatabaseHelper.generateSubject(UUID.randomUUID())
        val createEvent = getDefaultCreateEventDTO(hostId, dishId)


        val id = EventService.createEvent(createEvent)
            .toOption()
            .getOrElse { throw RuntimeException("Erro creating lunch") }

        val event = TestDatabaseHelper.queryEventWithoutTasks(id)

        assertEquals(hostId, event.host.id)
        assertEquals(dishId, event.subject?.id)
        assertEquals(createEvent.date.format(formatter), event.date.format(formatter))
        assertEquals(createEvent.title, event.title)
        assertEquals(createEvent.maxNumberGuest, event.maxNumberGuest)
    }

    // FIXME move to another object
    private fun getDefaultCreateEventDTO(hostId: UUID, dishId: UUID): EventDTO {

        val user = TestDatabaseHelper.queryUserById(hostId)

        return EventDTO(
            "test",
            user.email,
            dishId,
            LocalDateTime.now(),
            "here",
            10,
            BigDecimal(10),
            "something"
        )
    }

}

