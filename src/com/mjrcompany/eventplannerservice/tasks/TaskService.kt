package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.mjrcompany.eventplannerservice.CreateEntityException
import com.mjrcompany.eventplannerservice.DatabaseAccessException
import com.mjrcompany.eventplannerservice.FriendNotInMeetingException
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.core.CrudSubResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.Task
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWriterDTO
import com.mjrcompany.eventplannerservice.domain.TaskWriterDTO
import com.mjrcompany.eventplannerservice.meetings.MeetingService
import java.util.*


object TaskService {
    val createTask = fun(meetingId: UUID, taskDTO: TaskWriterDTO): ServiceResult<Int> {
        try {
            return Either.right(
                TaskRepository.createTask(
                    meetingId,
                    taskDTO
                )
            )
        } catch (e: Exception) {
            return Either.left(
                CreateEntityException(
                    "Error creating task",
                    e.message
                )
            )
        }

    }

    val updateTask = fun(id: Int, meetingId: UUID, taskDTO: TaskWriterDTO): ServiceResult<Unit> {
        return Either.right(
            TaskRepository.updateTask(
                id,
                meetingId,
                taskDTO
            )
        )
    }

    val getTask = fun(id: Int, meetingId: UUID): ServiceResult<Task> {
        val result =
            TaskRepository.getTaskById(id, meetingId)
        return when (result) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("Task not Found!"))
        }
    }

    val getTasksInMeeting = fun(meetingId: UUID): ServiceResult<List<Task>> {
        return Either.right(
            TaskRepository.getAllTasksInMeeting(
                meetingId
            )
        )
    }

    val acceptTask = fun(taskId: Int, meetingId: UUID, taskOwner: TaskOwnerWriterDTO): ServiceResult<Unit> {

        val eitherMeeting = MeetingService.getMeeting(meetingId)

        return fromTask(taskId, meetingId) {
            withValidMeeting(eitherMeeting) {
                ifFriendInMeeting(taskOwner.friendId, it) {
                    updateTaskOwner(
                        taskId,
                        meetingId,
                        taskOwner
                    )
                }
            }
        }
    }

    val crudResources = CrudSubResource(
        createTask,
        updateTask,
        getTask,
        getTasksInMeeting
    )

}

private fun updateTaskOwner(taskId: Int, meetingId: UUID, taskOwner: TaskOwnerWriterDTO): ServiceResult<Unit> {

    return try {
        Either.right(
            TaskRepository.updateTaskOwner(
                taskId,
                meetingId,
                taskOwner
            )
        )
    } catch (e: Exception) {
        Either.left(
            DatabaseAccessException(
                e.message ?: "", e.toString()
            )
        )
    }
}


private fun <T> fromTask(taskId: Int, meetingId: UUID, block: () -> ServiceResult<T>): ServiceResult<T> {

    return when (val eitherTasks =
        TaskService.getTask(taskId, meetingId)) {
        is Either.Left -> Either.left(eitherTasks.a)
        is Either.Right -> block()
    }
}

private fun <T> withValidMeeting(
    meeting: ServiceResult<Meeting>,
    block: (Meeting) -> ServiceResult<T>
): ServiceResult<T> {
    return meeting.fold(
        { Either.left(it) },
        { m -> block(m) }
    )
}

private fun <T> ifFriendInMeeting(
    friendId: UUID,
    meeting: Meeting,
    block: () -> ServiceResult<T>
): ServiceResult<T> {
    return when (friendId in meeting.friends.map { friend -> friend.id }) {
        false -> Either.left(FriendNotInMeetingException("The friends has to be added to the meeting before accept this task"))
        true -> block()
    }
}


