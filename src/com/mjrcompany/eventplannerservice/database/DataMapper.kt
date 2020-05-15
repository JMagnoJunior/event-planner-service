package com.mjrcompany.eventplannerservice.database

import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.EventDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.subjects.SubjectDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDateTime
import java.time.ZoneOffset

object DataMapper {

    fun mapToSubject(it: ResultRow): SubjectDomain.Subject {
        return SubjectDomain.Subject(
            it[Subjects.id],
            it[Subjects.name],
            it[Subjects.details],
            it[Subjects.createdBy],
            it[Subjects.imageUrl]
        )
    }

    fun mapToUser(it: ResultRow): UserDomain.User {
        return UserDomain.User(
            it[Users.id],
            it[Users.name],
            it[Users.email]
        )
    }

    fun mapToGuest(it: ResultRow): UserDomain.GuestInEvent {
        return UserDomain.GuestInEvent(
            it[Users.id],
            it[Users.name],
            it[Users.email],
            it[UsersInEvents.status]
        )
    }

    fun mapToEvent(
        it: ResultRow,
        tasks: List<TaskDomain.Task>,
        guestInEvents: List<UserDomain.GuestInEvent>
    ): EventDomain.Event {
        return EventDomain.Event(
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

    fun mapToTask(it: ResultRow): TaskDomain.Task {
        return TaskDomain.Task(
            it[Tasks.id].value,
            it[Tasks.details],
            it[Tasks.event],
            it[Tasks.owner]
        )
    }

}

