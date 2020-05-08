package com.mjrcompany.eventplannerservice.database

import com.mjrcompany.eventplannerservice.domain.*
import org.jetbrains.exposed.sql.ResultRow
import java.time.*

object DataMapper {

    fun mapToSubject(it: ResultRow): Subject {
        return Subject(
            it[Subjects.id],
            it[Subjects.name],
            it[Subjects.details],
            it[Subjects.createdBy],
            it[Subjects.imageUrl]
        )
    }

    fun mapToUser(it: ResultRow): User {
        return User(
            it[Users.id],
            it[Users.name],
            it[Users.email]
        )
    }

    fun mapToGuest(it: ResultRow): GuestInEvent {
        return GuestInEvent(
            it[Users.id],
            it[Users.name],
            it[Users.email],
            it[UsersInEvents.status]
        )
    }

    fun mapToEvent(it: ResultRow, tasks: List<Task>, guestInEvents: List<GuestInEvent>): Event {
        return Event(
            it[Events.id],
            it[Events.title],
            mapToUser(it),
            mapToSubject(it),
            it[Events.date],
            LocalDateTime.ofInstant(it[Events.createDate], ZoneOffset.UTC),
            it[Events.address],
            it[Events.maxNumberGuests],
            tasks,
            guestInEvents,
            it[Events.totalCost],
            it[Events.additionalInfo],
            it[Events.status]
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

