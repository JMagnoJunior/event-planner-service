package com.mjrcompany.eventplannerservice

import arrow.core.firstOrNone
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.EventWritable
import com.mjrcompany.eventplannerservice.domain.Task
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.util.*


object TestDatabaseHelper {

    fun addMeeting(uuid: UUID): UUID {
        lateinit var meetingId: UUID
        val hostId =
            addUser(
                UUID.randomUUID()
            )
        val dishId =
            addDish(
                UUID.randomUUID()
            )
        transaction {
            meetingId = Events.insert {
                it[id] = uuid
                it[title] = "test"
                it[host] = hostId
                it[subject] = dishId
                it[date] = LocalDate.now()
                it[address] = "somwhere"
                it[maxNumberGuests] = 10
                it[createDate] = Instant.now()
            } get Events.id
        }
        return meetingId
    }


    fun addFriendsInMeeting(friendId: UUID, meetingId: UUID) {
        transaction {
            UsersInEvents.insert {
                it[event] = meetingId
                it[user] = friendId
            }
        }
    }

    fun queryMeetingWithoutTasks(id: UUID): Event {
        lateinit var meetingRow: DataMapper.EventRow
        transaction {
            meetingRow = Events
                .join(Users, JoinType.INNER, additionalConstraint = { Events.host eq Users.id })
                .join(Subjects, JoinType.INNER, additionalConstraint = { Events.subject eq Subjects.id })
                .select { Events.id eq id }
                .map { DataMapper.mapToEventRow(it) }
                .first()
        }

        return DataMapper.mapToMeeting(meetingRow, emptyList(), emptyList())
    }

    fun queryTaskById(id: Int): Task {
        return transaction {
            Tasks.select { Tasks.id eq id }
                .map { DataMapper.mapToTask(it) }
                .first()
        }
    }


    // FIXME move to another object
    fun getDefaultCreateMeetingDTO(hostId: UUID, dishId: UUID): EventWritable {
        return EventWritable(
            "test",
            hostId,
            dishId,
            LocalDate.now(),
            "here",
            10
        )
    }

    fun addTask(meetingId: UUID): Int {
        val taskId = transaction {
            Tasks.insert {
                it[details] = "test task"
                it[event] = meetingId
            } get Tasks.id
        }
        return taskId.value
    }

    fun addUser(uuid: UUID): UUID {
        transaction {
            Users.insert {
                it[id] = uuid
                it[name] = "test"
                it[email] = "test@email.com"
            }
        }
        return uuid
    }

    fun addUser(uuid: UUID, hostName: String, hostEmail: String): UUID {
        transaction {
            Users.insert {
                it[id] = uuid
                it[name] = hostName
                it[email] = hostEmail
            }
        }
        return uuid
    }


    fun addDish(uuid: UUID): UUID {
        transaction {
            Subjects.insert {
                it[id] = uuid
                it[name] = "test"
            }
        }
        return uuid
    }


    fun addDish(uuid: UUID, dishName: String): UUID {
        transaction {
            Subjects.insert {
                it[id] = uuid
                it[name] = dishName
            }
        }
        return uuid
    }

    fun queryDishById(id: UUID): Subject {
        lateinit var subject: Subject
        transaction {
            subject = Subjects
                .select { Subjects.id eq id }
                .map {
                    DataMapper.mapToDish(it)
                }
                .firstOrNone()
                .getOrElse { throw RuntimeException("Error querying com.mjrcompany.eventplannerservice.subjects") }
        }
        return subject
    }

}
