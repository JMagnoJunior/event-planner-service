package com.magnojr.foodwithfriends.commons

import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDate
import java.util.*

object DataMapper {

    fun mapToDish(it: ResultRow): Dish {
        return Dish(
            it[Dishes.id],
            it[Dishes.name],
            it[Dishes.details]
        )
    }

    fun mapToUser(it: ResultRow): User {
        return User(
            it[Users.id],
            it[Users.name],
            it[Users.email]
        )
    }

    data class MeetingRow(
        val id: UUID,
        val description: String,
        val user: User?,
        val dish: Dish?,
        val date: LocalDate,
        val place: String?,
        val maxNumberFriends: Int,
        val resultRow: ResultRow
    )

    fun mapToMeetingRow(it: ResultRow): MeetingRow {
        return MeetingRow(
            it[Meetings.id],
            it[Meetings.description],
            mapToUser(it),
            mapToDish(it),
            it[Meetings.date],
            it[Meetings.place],
            it[Meetings.maxNumberFriends],
            it
        )
    }

    fun mapToMeeting(it: MeetingRow, tasks: List<Task>?): Meeting {
        return Meeting(
            it.id,
            it.description,
            mapToUser(it.resultRow),
            mapToDish(it.resultRow),
            it.date,
            it.place,
            it.maxNumberFriends,
            tasks
        )
    }

    fun mapToTask(it: ResultRow): Task {
        return Task(
            it[Tasks.id].value,
            it[Tasks.details],
            it[Tasks.meeting]
        )
    }

}

