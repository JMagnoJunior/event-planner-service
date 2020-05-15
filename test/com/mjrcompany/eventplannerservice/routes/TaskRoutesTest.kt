package com.mjrcompany.eventplannerservice.routes

import com.google.gson.reflect.TypeToken
import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.EventDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
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
import kotlin.test.assertNull


@KtorExperimentalAPI
class TaskRoutesTest : RootTestDefinition() {


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

                val taskId = response.content?.toInt()!!
                val task = TestDatabaseHelper.queryTaskById(taskId)
                assertEquals(task.eventId, eventId)
                assertNull(task.owner)
                assertEquals(task.details, newTask.details)

            }
        }
    }

    @Test
    fun `it should list all tasks for the event when any user get all tasks`() {

        val (eventId, _) = getEventWritableForTest()
        val totalIasks = 5
        (1..totalIasks).toList().map { TestDatabaseHelper.generateTask(eventId) }

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/events/$eventId/tasks").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val result: Page<TaskDomain.Task> = gson.fromJson(
                    response.content,
                    object : TypeToken<Page<TaskDomain.Task?>?>() {}.type
                )

                assertEquals(totalIasks, result.items.size)
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

}
