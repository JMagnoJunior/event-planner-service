package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Either
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.fail

class TaskServiceTest : RootTestDefinition() {


    @Test
    fun `it should includes a task in an event when the host creates a new task`() {

        val eventId = TestDatabaseHelper.generateEvent()
        val newTask = TaskDomain.TaskWritable("new task")
        val taskId = TaskService.createTask(eventId, newTask)
            .toOption()
            .getOrElse { throw RuntimeException("Error creating task") }

        val task = TestDatabaseHelper.queryTaskById(taskId)

        assertEquals(newTask.details, task.details)
        assertEquals(eventId, task.eventId)
        assertEquals(task.id, taskId)

    }

    @Test
    fun `it should not allow a user accept a task when the user is not in the event`() {

        val eventId = TestDatabaseHelper.generateEvent()
        val taskId = TestDatabaseHelper.generateTask(eventId)
        val invalidTaskOwner = TaskDomain.TaskOwnerWritable(UUID.randomUUID())
        val result = TaskService.acceptTask(taskId, eventId, invalidTaskOwner)

        if (result is Either.Left) {
            val error = result.a
            assertEquals(
                "The friends has to be added to the meeting before accept this task",
                error.errorResponse.message
            )
        } else {
            fail("It expect an error when user accept a task and he is not on the event!")
        }
    }

    @Test
    fun `it should set a task to a guest when the guest accept the task and the guest is subscribed to the event`() {

        val meetingId = TestDatabaseHelper.generateEvent()
        val taskId = TestDatabaseHelper.generateTask(meetingId)
        val friendId = TestDatabaseHelper.generateUser(UUID.randomUUID())
        val taskOwner = TaskDomain.TaskOwnerWritable(friendId)

        TestDatabaseHelper.addGuestInEvent(friendId, meetingId)

        val result = TaskService.acceptTask(taskId, meetingId, taskOwner)

        if (result is Either.Right) {
            val task = TestDatabaseHelper.queryTaskById(taskId)
            assertEquals(taskId, task.id)
            assertEquals(taskOwner.friendId, task.owner)
        } else {
            fail("It does not expect to receive an exception")
        }

    }

}