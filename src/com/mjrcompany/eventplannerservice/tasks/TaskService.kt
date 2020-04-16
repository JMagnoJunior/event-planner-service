package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.mjrcompany.eventplannerservice.DatabaseAccessException
import com.mjrcompany.eventplannerservice.FriendNotInMeetingException
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.core.CrudSubResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.Task
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.domain.TaskWritable
import com.mjrcompany.eventplannerservice.meetings.MeetingService
import java.util.*


object TaskService {
    val createTask = fun(meetingId: UUID, task: TaskWritable): ServiceResult<Int> {

        return withErrorTreatment {
            TaskRepository.createTask(
                meetingId,
                task
            )
        }
    }

    val updateTask = fun(id: Int, meetingId: UUID, task: TaskWritable): ServiceResult<Unit> {
        return withErrorTreatment {
            TaskRepository.updateTask(
                id,
                meetingId,
                task
            )
        }
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
        return withErrorTreatment {
            TaskRepository.getAllTasksInMeeting(
                meetingId
            )
        }
    }

    val acceptTask = fun(taskId: Int, meetingId: UUID, taskOwner: TaskOwnerWritable): ServiceResult<Unit> {

        val eitherMeeting = MeetingService.getMeeting(meetingId)

        return fromTask(taskId, meetingId) {
            withValidMeeting(eitherMeeting) {
                whenFriendInMeeting(taskOwner.friendId, it) {
                    withErrorTreatment {
                        TaskRepository.updateTaskOwner(
                            taskId,
                            meetingId,
                            taskOwner
                        )
                    }
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

private fun <T> withErrorTreatment(block: () -> T): ServiceResult<T> {
    return try {
        Either.right(
            block()
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

private fun <T> whenFriendInMeeting(
    friendId: UUID,
    meeting: Meeting,
    block: () -> ServiceResult<T>
): ServiceResult<T> {
    return when (friendId in meeting.friends.map { friend -> friend.id }) {
        false -> Either.left(FriendNotInMeetingException("The friends has to be added to the meeting before accept this task"))
        true -> block()
    }
}


