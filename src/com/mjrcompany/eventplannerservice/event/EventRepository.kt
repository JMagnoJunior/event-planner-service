package com.mjrcompany.eventplannerservice.event

import arrow.core.Option
import arrow.core.extensions.list.foldable.firstOrNone
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.EventSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.EventWritable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*


object EventRepository {

    fun getEventById(id: UUID): Option<Event> {


        return transaction {
            Events
                .join(Users, JoinType.INNER, additionalConstraint = { Events.host eq Users.id })
                .join(Subjects, JoinType.INNER, additionalConstraint = { Events.subject eq Subjects.id })
                .select { Events.id eq id }
                .map {
                    DataMapper.mapToEventRow(it)
                }.firstOrNone()
                .map {
                    val tasks = Tasks.select { Tasks.event eq id }
                        .map { DataMapper.mapToTask(it) }

                    val friends = UsersInEvents
                        .join(Users, JoinType.INNER, additionalConstraint = { UsersInEvents.user eq Users.id })
                        .select { UsersInEvents.event eq id }
                        .map { DataMapper.mapToUser(it) }
                    DataMapper.mapToEvent(it, tasks, friends)
                }

        }

    }

    fun getAllEvents(): List<Event> {
        return transaction {
            Events
                .join(Users, JoinType.INNER, additionalConstraint = { Events.host eq Users.id })
                .join(Subjects, JoinType.INNER, additionalConstraint = { Events.subject eq Subjects.id })
                .selectAll()
                .map {
                    val tasks = Tasks.select { Tasks.event eq it[Events.id]}
                        .map { DataMapper.mapToTask(it) }

                    val friends = UsersInEvents
                        .join(Users, JoinType.INNER, additionalConstraint = { UsersInEvents.user eq Users.id })
                        .select { UsersInEvents.event eq it[Events.id] }
                        .map { DataMapper.mapToUser(it) }

                    DataMapper.mapToEvent(DataMapper.mapToEventRow(it), tasks, friends)
                }
        }
    }

    fun createEvent(eventDTO: EventWritable): UUID {
        lateinit var meetingId: UUID
        transaction {
            meetingId = Events.insert {
                writeAttributesOnCreate(
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
                writeAttributesOnUpdate(
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

    private fun writeAttributesOnUpdate(it: UpdateBuilder<Any>, event: EventWritable) {
        it[Events.title] = event.description
        it[Events.subject] = event.subject
        it[Events.date] = event.date
        it[Events.address] = event.place
        it[Events.maxNumberGuests] = event.maxNumberGuest
        it[Events.totalCost] = event.totalCost
    }

    private fun writeAttributesOnCreate(it: UpdateBuilder<Any>, id: UUID, eventDTO: EventWritable) {
        it[Events.id] = id
        it[Events.createDate] = Instant.now()
        it[Events.host] = eventDTO.host
        writeAttributesOnUpdate(it, eventDTO)
    }

}
