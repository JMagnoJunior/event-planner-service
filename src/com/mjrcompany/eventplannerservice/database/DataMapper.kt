package com.mjrcompany.eventplannerservice.database

import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.Task
import com.mjrcompany.eventplannerservice.domain.User
import org.jetbrains.exposed.sql.ResultRow
import java.math.BigDecimal
import java.time.*
import java.util.*

object DataMapper {

    fun mapToDish(it: ResultRow): Subject {
        return Subject(
            it[Subjects.id],
            it[Subjects.name],
            it[Subjects.details]
        )
    }

    fun mapToUser(it: ResultRow): User {
        return User(
            it[Users.id],
            it[Users.name],
            it[Users.email]
        )
    }

    data class EventRow(
        val id: UUID,
        val description: String,
        val user: User?,
        val subject: Subject?,
        val date: LocalDate,
        val createDate: Instant,
        val place: String?,
        val maxNumberFriends: Int,
        val totalCost: BigDecimal,
        val resultRow: ResultRow
    )

    fun mapToEventRow(it: ResultRow): EventRow {
        return EventRow(
            it[Events.id],
            it[Events.title],
            mapToUser(it),
            mapToDish(it),
            it[Events.date],
            it[Events.createDate],
            it[Events.address],
            it[Events.maxNumberGuests],
            it[Events.totalCost],
            it
        )
    }

    fun mapToEvent(it: EventRow, tasks: List<Task>, friends: List<User>): Event {
        return Event(
            it.id,
            it.description,
            mapToUser(it.resultRow),
            mapToDish(it.resultRow),
            it.date,
            LocalDateTime.ofInstant(it.createDate, ZoneOffset.UTC),
            it.place,
            it.maxNumberFriends,
            tasks,
            friends,
            it.totalCost
        )
    }

    fun mapToTask(it: ResultRow): Task {
        return Task(
            it[Tasks.id].value,
            it[Tasks.details],
            it[Tasks.event],
            it[Tasks.owner]
        )
    }

}

