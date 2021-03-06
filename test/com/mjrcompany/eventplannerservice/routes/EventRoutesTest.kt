package com.mjrcompany.eventplannerservice.routes

import arrow.core.getOrElse
import com.google.gson.reflect.TypeToken
import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.EventDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.UserInEventStatus
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


@KtorExperimentalAPI
class EventRoutesTest : RootTestDefinition() {

    @Test
    fun `it should add a new event when creating an event and user is authenticated`() {

        val eventTitle = getRandomString(10)
        val eventHost =
            TestDatabaseHelper.generateUser("test@mail.com")
        val eventSubject = TestDatabaseHelper.generateSubject(UUID.randomUUID())
        val eventDate = LocalDateTime.now()
        val eventAddress = getRandomString(10)
        val eventMaxNumberGuest = Random.nextInt(0, 10)
        val eventTotalCost = BigDecimal(Random.nextInt(1, 10)).setScale(2)
        val eventAdditionalInfo = getRandomString(10)
        val newEvent = EventDomain.EventValidatable(
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
                buildXIdToken(this, host.email, host.name, host.id)
                setBody(gson.toJson(newEvent))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())

                println(response.content)
                val id = gson.fromJson(
                    response.content,
                    UUID::class.java
                )

                val eventCreated =
                    TestDatabaseHelper.queryMiniEvent(id)

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
                    EventDomain.Event::class.java
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
    fun `it should return all host events when get all events by hostId`() {

        val userId = UUID.randomUUID()
        val listExepectedEvents: List<Pair<UUID, EventDomain.EventWritable>> =
            getEventWritableForTest(UserDomain.User(userId, getRandomString(10), "some@test.com"))
        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/events?hostId=$userId&pageSize=100").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val result: Page<EventDomain.Event> = gson.fromJson(
                    response.content,
                    object : TypeToken<Page<EventDomain.Event?>?>() {}.type
                ) as Page<EventDomain.Event>

                assertEquals(listExepectedEvents.size, result.items.size)

                result.items.forEach {
                    assertTrue("it contains all expected results") {
                        listExepectedEvents.map { it.first }.contains(it.id)
                    }
                }
            }
        }
    }

    @Test
    fun `it should modify event when updating the event and user provided is the host of the event`() {

        val (id, event) = getEventWritableForTest()

        val host = TestDatabaseHelper.queryUserById(event.host)

        val modifiedEvent = EventDomain.EventValidatable(
            title = getRandomString(10),
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
                buildXIdToken(this, host.email, host.name, host.id)
                setBody(gson.toJson(modifiedEvent))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
        }

        val eventUpdated =
            TestDatabaseHelper.queryMiniEvent(id)

        assertEquals(modifiedEvent.title, eventUpdated.title)
        assertEquals(modifiedEvent.subject, eventUpdated.subject.id)
        assertEquals(gson.toJson(modifiedEvent.date), gson.toJson(eventUpdated.date))
        assertEquals(modifiedEvent.address, eventUpdated.address)
        assertEquals(modifiedEvent.maxNumberGuest, eventUpdated.maxNumberGuest)
        assertEquals(modifiedEvent.totalCost, eventUpdated.totalCost)
        assertEquals(modifiedEvent.additionalInfo, eventUpdated.additionalInfo)
    }

    @Test
    fun `it should add user as a guest when user accept to be a guest of the event`() {

        val (eventId, _) = getEventWritableForTest()
        val guestId = TestDatabaseHelper.generateUser(UUID.randomUUID())
        val guest = TestDatabaseHelper.queryUserById(guestId)
        TestDatabaseHelper.addGuestIntoEvent(eventId, guestId)

        val acceptGuestInEvent = EventDomain.AcceptGuestInEventWritable(guest.id, UserInEventStatus.Accept)
        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/events/$eventId/accept-guest") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                buildXIdToken(this, guest.email, guest.name, guest.id)
                setBody(gson.toJson(acceptGuestInEvent))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }
        }

        val eventUpdated = TestDatabaseHelper.queryEvent(eventId).getOrElse { fail("it should get the event") }
        assertEquals(eventUpdated.guestInEvents[0].status, UserInEventStatus.Accept)

    }

    @Test
    fun `it should create add a task on event when the host creates a task for an event`() {
        val (eventId, event) = getEventWritableForTest()
        val host = TestDatabaseHelper.queryUserById(event.host)

        val newTask = TaskDomain.TaskWritable(getRandomString(10))

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/events/$eventId/tasks") {
                addHeader("Content-Type", "application/json")
                buildXIdToken(this, host.email, host.name, host.id)
                addAuthenticationHeader(this)
                setBody(gson.toJson(newTask))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
        }
    }

    private fun getEventWritableForTest(): Pair<UUID, EventDomain.EventWritable> {
        val event = EventDomain.EventWritable(
            title = getRandomString(10),
            host = TestDatabaseHelper.generateUser(UUID.randomUUID()),
            subject = TestDatabaseHelper.generateSubject(UUID.randomUUID()),
            date = LocalDateTime.now(),
            address = getRandomString(10),
            maxNumberGuest = Random.nextInt(0, 10),
            totalCost = BigDecimal(Random.nextInt(1, 10)).setScale(2),
            additionalInfo = getRandomString(10)
        )
        val id = TestDatabaseHelper.generateEvent(
            UUID.randomUUID(),
            event
        )
        return id to event
    }

    private fun getEventWritableForTest(use: UserDomain.User): List<Pair<UUID, EventDomain.EventWritable>> {

        val host = TestDatabaseHelper.generateUser(use.id, use.name, use.email)

        return (0..9).toList().map {
            EventDomain.EventWritable(
                title = getRandomString(10),
                host = host,
                subject = TestDatabaseHelper.generateSubject(UUID.randomUUID()),
                date = LocalDateTime.now(),
                address = getRandomString(10),
                maxNumberGuest = Random.nextInt(0, 10),
                totalCost = BigDecimal(Random.nextInt(1, 10)).setScale(2),
                additionalInfo = getRandomString(10)
            )
        }.map {
            TestDatabaseHelper.generateEvent(
                UUID.randomUUID(),
                it
            ) to it
        }
    }


}
