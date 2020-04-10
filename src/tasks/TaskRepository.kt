package com.magnojr.foodwithfriends.tasks

import arrow.core.Option
import arrow.core.firstOrNone
import com.magnojr.foodwithfriends.commons.DataMapper
import com.magnojr.foodwithfriends.commons.Task
import com.magnojr.foodwithfriends.commons.TaskWriterDTO
import com.magnojr.foodwithfriends.commons.Tasks
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


object TaskRepository {

    fun createTask(meetingId: UUID, taskDTO: TaskWriterDTO): Int {

        lateinit var taskId: EntityID<Int>

        transaction {
            taskId = Tasks.insert {
                writeAttributes(it, meetingId, taskDTO)
            } get Tasks.id
        }
        return taskId.value
    }

    fun updateTask(id: Int, meetingId: UUID, taskDTO: TaskWriterDTO) {
        transaction {
            Tasks.update({ Tasks.id eq id })
            {
                writeAttributes(it, meetingId, taskDTO)
            }
        }
    }

    fun getTaskById(id: Int, meetingId: UUID): Option<Task> {

        lateinit var result: Option<Task>
        transaction {
            result = Tasks
                .select { (Tasks.id eq id) and (Tasks.meeting eq meetingId) }
                .map {
                    DataMapper.mapToTask(it)
                }
                .firstOrNone()

        }
        return result
    }

    fun getAllTasksInMeeting(meetingId: UUID): List<Task> {
        lateinit var tasks: List<Task>
        transaction {
            tasks = Tasks
                .select { Tasks.meeting eq meetingId }
                .map {
                    DataMapper.mapToTask(it)
                }
        }
        return tasks
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, meetingId: UUID, taskDTO: TaskWriterDTO) {
        it[Tasks.details] = taskDTO.details
        it[Tasks.meeting] = meetingId
    }


}


