package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Either
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.domain.TaskWritable
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.fail

class TaskServiceTest : RootTestDefinition() {


    @Test
    fun `it should create a task for a valid meeting`() {

        val meetingId = TestDatabaseHelper.addMeeting(UUID.randomUUID())
        val createTask = TaskWritable("test")
        val taskId = TaskService.createTask(meetingId, createTask)
            .toOption()
            .getOrElse { throw RuntimeException("Error creating task") }

        val task = TestDatabaseHelper.queryTaskById(taskId)
        assertEquals(task.details, createTask.details)
        assertEquals(task.id, taskId)
        assertEquals(task.meetingId, meetingId)
    }

    @Test
    fun `a user not in the the meeting should not accept a task`() {

        val meetingId = TestDatabaseHelper.addMeeting(UUID.randomUUID())
        val taskId = TestDatabaseHelper.addTask(meetingId)
        val taskOwner = TaskOwnerWritable(UUID.randomUUID())
        val result = TaskService.acceptTask(taskId, meetingId, taskOwner)

        if (result is Either.Left) {
            val error = result.a
            assertEquals(
                "The friends has to be added to the meeting before accept this task",
                error.errorResponse.message
            )
        } else {
            fail("It expect to have a friend is in the task!")
        }
    }

    @Test
    fun `a friend  should accept a task`() {

        val meetingId = TestDatabaseHelper.addMeeting(UUID.randomUUID())
        val taskId = TestDatabaseHelper.addTask(meetingId)
        val friendId = TestDatabaseHelper.addUser(UUID.randomUUID())
        val taskOwner = TaskOwnerWritable(friendId)

        TestDatabaseHelper.addFriendsInMeeting(friendId, meetingId)

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