package com.mjrcompany.eventplannerservice

import arrow.core.firstOrNone
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.Meeting
import com.mjrcompany.eventplannerservice.domain.MeetingWritable
import com.mjrcompany.eventplannerservice.domain.Task
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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
            meetingId = Meetings.insert {
                it[id] = uuid
                it[description] = "test"
                it[host] = hostId
                it[dish] = dishId
                it[date] = LocalDate.now()
                it[place] = "somwhere"
                it[maxNumberFriends] = 10
            } get Meetings.id
        }
        return meetingId
    }


    fun addFriendsInMeeting(friendId: UUID, meetingId: UUID) {
        transaction {
            FriendsInMeetings.insert {
                it[meeting] = meetingId
                it[friend] = friendId
            }
        }
    }

    fun queryMeetingWithoutTasks(id: UUID): Meeting {
        lateinit var meetingRow: DataMapper.MeetingRow
        transaction {
            meetingRow = Meetings
                .join(Users, JoinType.INNER, additionalConstraint = { Meetings.host eq Users.id })
                .join(Dishes, JoinType.INNER, additionalConstraint = { Meetings.dish eq Dishes.id })
                .select { Meetings.id eq id }
                .map { DataMapper.mapToMeetingRow(it) }
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
    fun getDefaultCreateMeetingDTO(hostId: UUID, dishId: UUID): MeetingWritable {
        return MeetingWritable(
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
                it[meeting] = meetingId
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
            Dishes.insert {
                it[id] = uuid
                it[name] = "test"
            }
        }
        return uuid
    }


    fun addDish(uuid: UUID, dishName: String): UUID {
        transaction {
            Dishes.insert {
                it[id] = uuid
                it[name] = dishName
            }
        }
        return uuid
    }

    fun queryDishById(id: UUID): Dish {
        lateinit var dish: Dish
        transaction {
            dish = Dishes
                .select { Dishes.id eq id }
                .map {
                    DataMapper.mapToDish(it)
                }
                .firstOrNone()
                .getOrElse { throw RuntimeException("Error querying com.mjrcompany.eventplannerservice.dishes") }
        }
        return dish
    }

}
