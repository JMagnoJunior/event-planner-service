package com.magnojr.foodwithfriends.meetings

import com.magnojr.foodwithfriends.commons.*
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


object MeetingRepository {

    fun getMeetingById(id: UUID): Meeting {

        var maybeMeetingRow: DataMapper.MeetingRow? = null
        var tasks: MutableList<Task> = mutableListOf()

        transaction {
            Meetings
                .join(Users, JoinType.INNER, additionalConstraint = { Meetings.host eq Users.id })
                .join(Dishes, JoinType.INNER, additionalConstraint = { Meetings.dish eq Dishes.id })
                .join(Tasks, JoinType.LEFT, additionalConstraint = { Meetings.id eq Tasks.meeting })
                .select { Meetings.id eq id }
                .map {
                    maybeMeetingRow = maybeMeetingRow ?: DataMapper.mapToMeetingRow(it)
                    if (it[Tasks.id] != null) {
                        tasks.add(DataMapper.mapToTask(it))
                    }
                }
        }

        var meetingRow = maybeMeetingRow ?: throw NotFoundException("Meeting not found")

        return DataMapper.mapToMeeting(meetingRow, tasks)
    }

    fun createMeeting(meetingDTO: MeetingWriterDTO): UUID {
        lateinit var meetingId: UUID
        transaction {
            meetingId = Meetings.insert {
                writeAttributes(it, UUID.randomUUID(), meetingDTO)
            } get Meetings.id
        }
        return meetingId
    }

    fun updateMeeting(id: UUID, meetingDTO: MeetingWriterDTO) {
        transaction {
            Meetings.update({ Meetings.id eq id })
            {
                writeAttributes(it, meetingDTO)
            }
        }
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, meetingDTO: MeetingWriterDTO) {
        it[Meetings.description] = meetingDTO.description
        it[Meetings.host] = meetingDTO.host
        it[Meetings.dish] = meetingDTO.dish
        it[Meetings.date] = meetingDTO.date
        it[Meetings.place] = meetingDTO.place
        it[Meetings.maxNumberFriends] = meetingDTO.maxNumberFriends
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, meetingDTO: MeetingWriterDTO) {
        it[Meetings.id] = id
        writeAttributes(it, meetingDTO)
    }

}
