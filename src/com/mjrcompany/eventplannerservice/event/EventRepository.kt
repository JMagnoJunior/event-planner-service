package com.mjrcompany.eventplannerservice.event

import arrow.core.Option
import arrow.core.extensions.list.foldable.firstOrNone
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.*
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.*


object EventRepository {

    fun getEventById(id: UUID): Option<Event> {

        lateinit var maybeEventRow: Option<DataMapper.EventRow>
        var tasks: List<Task> = emptyList()
        var friends: List<User> = emptyList()

        transaction {
            maybeEventRow = Events
                .join(Users, JoinType.INNER, additionalConstraint = { Events.host eq Users.id })
                .join(Subjects, JoinType.INNER, additionalConstraint = { Events.subject eq Subjects.id })
                .select { Events.id eq id }
                .map {
                    DataMapper.mapToEventRow(it)
                }.firstOrNone()

            tasks = Tasks.select { Tasks.event eq id }
                .map { DataMapper.mapToTask(it) }

            friends = UsersInEvents
                .join(Users, JoinType.INNER, additionalConstraint = { UsersInEvents.user eq Users.id })
                .select { UsersInEvents.event eq id }
                .map { DataMapper.mapToUser(it) }

        }

        val meetingRow = maybeEventRow
            .getOrElse { throw NotFoundException("Meeting not found") }

        return Option.just(DataMapper.mapToMeeting(meetingRow, tasks, friends))
    }

    fun createEvent(eventDTO: EventWritable): UUID {
        lateinit var meetingId: UUID
        transaction {
            meetingId = Events.insert {
                writeAttributes(
                    it,
                    UUID.randomUUID(),
                    eventDTO
                )
            } get Events.id
        }
        return meetingId
    }

    fun updateMeeting(id: UUID, eventDTO: EventWritable) {
        transaction {
            Events.update({ Events.id eq id })
            {
                writeAttributes(
                    it,
                    eventDTO
                )
            }
        }
    }

    fun insertFriendInEvent(id: UUID, eventSubscriberDTO: EventSubscriberWritable) {
        transaction {
            UsersInEvents.insert {
                it[event] = id
                it[user] = eventSubscriberDTO.friendId
            }
        }
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, eventDTO: EventWritable) {
        it[Events.title] = eventDTO.description
        it[Events.host] = eventDTO.host
        it[Events.subject] = eventDTO.subject
        it[Events.date] = eventDTO.date
        it[Events.address] = eventDTO.place
        it[Events.maxNumberGuests] = eventDTO.maxNumberFriends
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, eventDTO: EventWritable) {
        it[Events.id] = id
        it[Events.createDate] = Instant.now()
        writeAttributes(it, eventDTO)
    }

}
