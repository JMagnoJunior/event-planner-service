package com.magnojr.foodwithfriends.meetings

import com.magnojr.foodwithfriends.RootTestDefinition
import com.magnojr.foodwithfriends.commons.Dishes
import com.magnojr.foodwithfriends.commons.MeetingWriterDTO
import com.magnojr.foodwithfriends.commons.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.*
import kotlin.test.Test


class MeetingServiceTest : RootTestDefinition() {

    @Test
    fun `it should create a meeting for a valid host`() {
        val hostId = addHost(UUID.randomUUID())
        val dishId = addDish(UUID.randomUUID())
        val createMeetingDTO = getDefaultCreateMeetingDTO(hostId, dishId)

        val id = MeetingService.createMeeting(createMeetingDTO)
        val meeting = MeetingService.getMeeting(id)
        print(meeting)
    }
}

fun getDefaultCreateMeetingDTO(hostId: UUID, dishId: UUID): MeetingWriterDTO {
    return MeetingWriterDTO(
        "test",
        hostId,
        dishId,
        LocalDate.now(),
        "here",
        10
    )
}

fun addHost(uuid: UUID): UUID {
    transaction {
        Users.insert {
            it[id] = uuid
            it[name] = "test"
            it[email] = "test@email.com"
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