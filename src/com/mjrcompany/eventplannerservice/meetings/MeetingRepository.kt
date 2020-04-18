package com.mjrcompany.eventplannerservice.meetings

import arrow.core.Option
import arrow.core.extensions.list.foldable.firstOrNone
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.database.*
import com.mjrcompany.eventplannerservice.domain.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


object MeetingRepository {

    fun getMeetingById(id: UUID): Option<Meeting> {

        lateinit var maybeMeetingRow: Option<DataMapper.MeetingRow>
        var tasks: List<Task> = emptyList()
        var friends: List<User> = emptyList()

        transaction {
            maybeMeetingRow = Meetings
                .join(Users, JoinType.INNER, additionalConstraint = { Meetings.host eq Users.id })
                .join(Dishes, JoinType.INNER, additionalConstraint = { Meetings.dish eq Dishes.id })
                .select { Meetings.id eq id }
                .map {
                    DataMapper.mapToMeetingRow(it)
                }.firstOrNone()

            tasks = Tasks.select { Tasks.meeting eq id }
                .map { DataMapper.mapToTask(it) }

            friends = FriendsInMeetings
                .join(Users, JoinType.INNER, additionalConstraint = { FriendsInMeetings.friend eq Users.id })
                .select { FriendsInMeetings.meeting eq id }
                .map { DataMapper.mapToUser(it) }

        }

        val meetingRow = maybeMeetingRow
            .getOrElse { throw NotFoundException("Meeting not found") }

        return Option.just(DataMapper.mapToMeeting(meetingRow, tasks, friends))
    }

    fun createMeeting(meetingDTO: MeetingWritable): UUID {
        lateinit var meetingId: UUID
        transaction {
            meetingId = Meetings.insert {
                writeAttributes(
                    it,
                    UUID.randomUUID(),
                    meetingDTO
                )
            } get Meetings.id
        }
        return meetingId
    }

    fun updateMeeting(id: UUID, meetingDTO: MeetingWritable) {
        transaction {
            Meetings.update({ Meetings.id eq id })
            {
                writeAttributes(
                    it,
                    meetingDTO
                )
            }
        }
    }

    fun deleteMeeting(id: UUID) {
        transaction {
            Meetings.deleteWhere { Meetings.id eq id }
        }
    }

    fun insertFriendInMeeting(id: UUID, meetingSubscriberDTO: MeetingSubscriberWritable) {
        transaction {
            FriendsInMeetings.insert {
                it[meeting] = id
                it[friend] = meetingSubscriberDTO.friendId
            }
        }
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, meetingDTO: MeetingWritable) {
        it[Meetings.description] = meetingDTO.description
        it[Meetings.host] = meetingDTO.host
        it[Meetings.dish] = meetingDTO.dish
        it[Meetings.date] = meetingDTO.date
        it[Meetings.place] = meetingDTO.place
        it[Meetings.maxNumberFriends] = meetingDTO.maxNumberFriends
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, meetingDTO: MeetingWritable) {
        it[Meetings.id] = id
        writeAttributes(it, meetingDTO)
    }

}
