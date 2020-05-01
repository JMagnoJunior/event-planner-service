package com.mjrcompany.eventplannerservice.routes

import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.EventWritable
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals


@KtorExperimentalAPI
class EventRoutesTest : RootTestDefinition() {

    @Test
    fun `it should add a new event when creating an event and user is authenticated`() {

        val eventTitle = "event title"
        val eventHost =
            TestDatabaseHelper.generateUser("test@mail.com")
        val eventSubject =
            TestDatabaseHelper.generateSubject(UUID.randomUUID())
        val eventDate = LocalDateTime.now()
        val eventAddress = "somewhere"
        val eventMaxNumberGuest = 10
        val eventTotalCost = BigDecimal.TEN.setScale(2)
        val eventAdditionalInfo = "something"
        val newEvent = EventDTO(
            title = eventTitle,
            subject = eventSubject,
            date = eventDate,
            address = eventAddress,
            maxNumberGuest = eventMaxNumberGuest,
            totalCost = eventTotalCost,
            additionalInfo = eventAdditionalInfo
        )

        val host = TestDatabaseHelper.queryUserByEmail(eventHost)

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/events") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                buildXIdToken(this, host.email, host.name)
                setBody(gson.toJson(newEvent))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())

                println(response.content)
                val id = gson.fromJson(
                    response.content,
                    UUID::class.java
                )

                val eventCreated =
                    TestDatabaseHelper.queryEventWithoutTasks(id)

                assertEquals(eventTitle, eventCreated.title)
                assertEquals(eventHost, eventCreated.host.email)
                assertEquals(eventSubject, eventCreated.subject.id)
                assertEquals(
                    gson.toJson(eventDate),
                    gson.toJson(eventCreated.date)
                )
                assertEquals(eventAddress, eventCreated.address)
                assertEquals(eventMaxNumberGuest, eventCreated.maxNumberGuest)
                assertEquals(eventTotalCost, eventCreated.totalCost)
                assertEquals(eventAdditionalInfo, eventCreated.additionalInfo)
            }
        }

    }

    @Test
    fun `it should return an event when get by id`() {

        val (id, eventWritable) = getEventWritableForTest()

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/events/$id").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val result = gson.fromJson(
                    response.content,
                    Event::class.java
                )

                assertEquals(eventWritable.title, result.title)
                assertEquals(eventWritable.host, result.host.id)
                assertEquals(eventWritable.subject, result.subject.id)
                assertEquals(
                    gson.toJson(eventWritable.date),
                    gson.toJson(result.date)
                )
                assertEquals(eventWritable.address, result.address)
                assertEquals(eventWritable.maxNumberGuest, result.maxNumberGuest)
                assertEquals(eventWritable.totalCost, result.totalCost)
                assertEquals(eventWritable.additionalInfo, result.additionalInfo)

            }
        }
    }

    @Test
    fun `it should modify event when updating the event and user provided is the host of the event`() {

        val (id, event) = getEventWritableForTest()

        val host = TestDatabaseHelper.queryUserById(event.host)

        val modifiedEvent = EventDTO(
            title = "New title",
            subject = event.subject,
            date = event.date,
            address = event.address,
            maxNumberGuest = event.maxNumberGuest,
            totalCost = event.totalCost,
            additionalInfo = event.additionalInfo
        )

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Put, "/events/$id") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                buildXIdToken(this, host.email, host.name)
                setBody(gson.toJson(modifiedEvent))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
        }

        val eventUpdated =
            TestDatabaseHelper.queryEventWithoutTasks(id)

        assertEquals(modifiedEvent.title, eventUpdated.title)
        assertEquals(modifiedEvent.subject, eventUpdated.subject.id)
        assertEquals(gson.toJson(modifiedEvent.date), gson.toJson(eventUpdated.date))
        assertEquals(modifiedEvent.address, eventUpdated.address)
        assertEquals(modifiedEvent.maxNumberGuest, eventUpdated.maxNumberGuest)
        assertEquals(modifiedEvent.totalCost, eventUpdated.totalCost)
        assertEquals(modifiedEvent.additionalInfo, eventUpdated.additionalInfo)
    }

    private fun getEventWritableForTest(): Pair<UUID, EventWritable> {
        val event = EventWritable(
            title = "event title",
            host = TestDatabaseHelper.generateUser(UUID.randomUUID()),
            subject = TestDatabaseHelper.generateSubject(UUID.randomUUID()),
            date = LocalDateTime.now(),
            address = "somewhere",
            maxNumberGuest = 10,
            totalCost = BigDecimal.TEN.setScale(2),
            additionalInfo = "something"
        )
        val id = TestDatabaseHelper.generateEvent(
            UUID.randomUUID(),
            event
        )
        return id to event
    }

}
