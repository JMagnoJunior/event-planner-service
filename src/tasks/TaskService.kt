package com.magnojr.foodwithfriends.tasks

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.magnojr.foodwithfriends.commons.NotFoundException
import com.magnojr.foodwithfriends.commons.ResponseErrorException
import com.magnojr.foodwithfriends.commons.Task
import com.magnojr.foodwithfriends.commons.TaskWriterDTO
import com.magnojr.foodwithfriends.core.CrudSubResource
import java.util.*


object TaskService {
    val createTask = fun(meetingId: UUID, taskDTO: TaskWriterDTO): Either<ResponseErrorException, Int> {
        return Either.right(TaskRepository.createTask(meetingId, taskDTO))
    }

    val updateTask = fun(id: Int, meetingId: UUID, taskDTO: TaskWriterDTO): Either<ResponseErrorException, Unit> {
        return Either.right(TaskRepository.updateTask(id, meetingId, taskDTO))
    }

    val getTask = fun(id: Int, meetingId: UUID): Either<ResponseErrorException, Task> {
        val result = TaskRepository.getTaskById(id, meetingId)
        return when (result) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("Task not Found!"))
        }
    }

    val getTasksInMeeting = fun(meetingId: UUID): Either<ResponseErrorException, List<Task>> {
        return Either.right(TaskRepository.getAllTasksInMeeting(meetingId))
    }

    val crudResources = CrudSubResource(createTask, updateTask, getTask, getTasksInMeeting)

}