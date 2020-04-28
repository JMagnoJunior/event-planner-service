package com.mjrcompany.eventplannerservice.event

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.withPagination
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.*
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
                .firstOrNone()
                .map {
                    val tasks = Tasks.select { Tasks.event eq id }
                        .map { DataMapper.mapToTask(it) }

                    val guests = UsersInEvents
                        .join(Users, JoinType.INNER, additionalConstraint = { UsersInEvents.user eq Users.id })
                        .select { UsersInEvents.event eq id }
                        .map { DataMapper.mapToGuest(it) }
                    DataMapper.mapToEvent(it, tasks, guests)
                }

        }

    }

    fun getAllEvents(pagination: Pagination): Page<Event> {
        return transaction {
            Events
                .join(Users, JoinType.INNER, additionalConstraint = { Events.host eq Users.id })
                .join(Subjects, JoinType.INNER, additionalConstraint = { Events.subject eq Subjects.id })
                .selectAll()
                .withPagination(pagination) {
                    val tasks = Tasks.select { Tasks.event eq it[Events.id] }
                        .map { DataMapper.mapToTask(it) }

                    val guests = UsersInEvents
                        .join(Users, JoinType.INNER, additionalConstraint = { UsersInEvents.user eq Users.id })
                        .select { UsersInEvents.event eq it[Events.id] }
                        .map { DataMapper.mapToGuest(it) }

                    DataMapper.mapToEvent(it, tasks, guests)
                }
        }
    }

    fun createEvent(event: EventWritable): UUID {

        return transaction {
            Events.insert {
                writeAttributesOnCreateOnly(
                    it,
                    UUID.randomUUID(),
                    event
                )
            } get Events.id
        }
    }

    fun updateMeeting(id: UUID, event: EventWritable) {
        transaction {
            Events.update({ Events.id eq id })
            {
                writeAttributesOnUpdate(
                    it,
                    event
                )
            }
        }
    }

    fun insertFriendInEvent(id: UUID, eventSubscriberDTO: EventSubscriberWritable) {
        transaction {
            UsersInEvents.insert {
                it[event] = id
                it[user] = eventSubscriberDTO.guestId
                it[status] = UserInEventStatus.Pending
            }
        }
    }

    fun updateGuestStatus(id: UUID, acceptGuestInEventWritable: AcceptGuestInEventWritable) {
        transaction {
            UsersInEvents.update({
                (UsersInEvents.user eq acceptGuestInEventWritable.guestId) and
                        (UsersInEvents.event eq id)
            }) {
                it[status] = acceptGuestInEventWritable.status
            }
        }
    }

    private fun writeAttributesOnUpdate(it: UpdateBuilder<Any>, event: EventWritable) {
        it[Events.title] = event.title
        it[Events.subject] = event.subject
        it[Events.date] = event.date
        it[Events.address] = event.address
        it[Events.maxNumberGuests] = event.maxNumberGuest
        it[Events.totalCost] = event.totalCost
        it[Events.additionalInfo] = event.additionalInfo
    }

    private fun writeAttributesOnCreateOnly(it: UpdateBuilder<Any>, id: UUID, event: EventWritable) {
        it[Events.id] = id
        it[Events.createDate] = Instant.now()
        it[Events.host] = event.host
        it[Events.status] = EventStatus.Open
        writeAttributesOnUpdate(it, event)
    }

}
