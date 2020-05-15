package com.mjrcompany.eventplannerservice.tasks

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.withPagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.database.Tasks
import com.mjrcompany.eventplannerservice.database.Users
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object TaskRepository {

    fun createTask(meetingId: UUID, taskDTO: TaskDomain.TaskWritable): Int {

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

    fun updateTask(id: Int, meetingId: UUID, taskDTO: TaskDomain.TaskWritable) {
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

    fun updateTaskOwner(id: Int, meetingId: UUID, taskOwnerDTO: TaskDomain.TaskOwnerWritable) {
        transaction {
            Tasks.update({ (Tasks.id eq id) and (Tasks.event eq meetingId) })
            {
                it[owner] = taskOwnerDTO.friendId
            }
        }
    }

    fun getTaskById(id: Int, meetingId: UUID): Option<TaskDomain.Task> {

        lateinit var result: Option<TaskDomain.Task>
        transaction {
            result = Tasks
                .join(Users, JoinType.LEFT, additionalConstraint = { Tasks.owner eq Users.id })
                .select { (Tasks.id eq id) and (Tasks.event eq meetingId) }
                .map {
                    DataMapper.mapToTask(it)
                }
                .firstOrNone()

        }
        return result
    }

    fun getAllTasksInMeeting(meetingId: UUID, pagination: Pagination): Page<TaskDomain.Task> {
        return transaction {
            Tasks
                .select { Tasks.event eq meetingId }
                .withPagination(pagination) {
                    DataMapper.mapToTask(it)
                }
        }
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, meetingId: UUID, taskDTO: TaskDomain.TaskWritable) {
        it[Tasks.details] = taskDTO.details
        it[Tasks.event] = meetingId
    }


}


