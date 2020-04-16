package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.database.Tasks
import com.mjrcompany.eventplannerservice.database.Users
import com.mjrcompany.eventplannerservice.domain.Task
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWriterDTO
import com.mjrcompany.eventplannerservice.domain.TaskWriterDTO
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object TaskRepository {

    fun createTask(meetingId: UUID, taskDTO: TaskWriterDTO): Int {

        lateinit var taskId: EntityID<Int>

        transaction {
            taskId = Tasks.insert {
                writeAttributes(
                    it,
                    meetingId,
                    taskDTO
                )
            } get Tasks.id
        }
        return taskId.value
    }

    fun updateTask(id: Int, meetingId: UUID, taskDTO: TaskWriterDTO) {
        transaction {
            Tasks.update({ Tasks.id eq id })
            {
                writeAttributes(
                    it,
                    meetingId,
                    taskDTO
                )
            }
        }
    }

    fun updateTaskOwner(id: Int, meetingId: UUID, taskOwnerDTO: TaskOwnerWriterDTO): Unit {
        transaction {
            Tasks.update({ (Tasks.id eq id) and (Tasks.meeting eq meetingId) })
            {
                it[owner] = taskOwnerDTO.friendId
            }
        }
    }

    fun getTaskById(id: Int, meetingId: UUID): Option<Task> {

        lateinit var result: Option<Task>
        transaction {
            result = Tasks
                .join(Users, JoinType.INNER, additionalConstraint = { Tasks.owner eq Users.id })
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


