package com.mjrcompany.eventplannerservice.tasks

import arrow.core.*
import com.mjrcompany.eventplannerservice.FriendNotInMeetingException
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.event.EventService
import org.slf4j.LoggerFactory
import java.util.*


object TaskService {
    private var log = LoggerFactory.getLogger(TaskService::class.java)


    val createTask = fun(meetingId: UUID, task: TaskDomain.TaskWritable): ServiceResult<Int> {
        log.info("will create the task: $task")
        val result = withDatabaseErrorTreatment {
            TaskRepository.createTask(
                meetingId,
                task
            )
        }

        if (result.isLeft()) {
            log.error("error creating task: $task")
        }

        return result
    }

    val updateTask = fun(id: Int, meetingId: UUID, task: TaskDomain.TaskWritable): ServiceResult<Unit> {
        log.info("will update the task: $task")
        val result = withDatabaseErrorTreatment {
            TaskRepository.updateTask(
                id,
                meetingId,
                task
            )
        }

        if (result.isLeft()) {
            log.error("error creating task: $task")
        }

        return result
    }

    val getTask = fun(id: Int, meetingId: UUID): ServiceResult<Option<TaskDomain.Task>> {

        log.debug("Querying the task: $id")
        val result = withDatabaseErrorTreatment {
            TaskRepository.getTaskById(id, meetingId)
        }
        result.map { if (it.isEmpty()) log.info("task not found") }
        return result
    }

    val getAllTasksOnEvent = fun(meetingId: UUID, pagination: Pagination): ServiceResult<Page<TaskDomain.Task>> {
        return withDatabaseErrorTreatment {
            TaskRepository.getAllTasksInMeeting(
                meetingId, pagination
            )
        }
    }

    val acceptTask = fun(taskId: Int, meetingId: UUID, taskOwner: TaskDomain.TaskOwnerWritable): ServiceResult<Unit> {
        log.info("will accept the task. taskIdk $taskId , task owner: $taskOwner")
        val meeting = EventService.getEvent(meetingId)

        if (meeting.isLeft()) {
            Either.left(FriendNotInMeetingException("The friends has to be added to the meeting before accept this task"))
        }

        return meeting.flatMap {
            when (it) {
                is None -> Either.left(NotFoundException("Meeting not found!"))
                is Some -> {
                    if (it.t.guestInEvents.map { friend -> friend.id }.contains(taskOwner.friendId)) {
                        Either.right(Unit)
                    } else {
                        Either.left(FriendNotInMeetingException("The friends has to be added to the meeting before accept this task"))
                    }

                }
            }.flatMap {
                withDatabaseErrorTreatment {
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


