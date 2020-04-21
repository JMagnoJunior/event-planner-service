package com.mjrcompany.eventplannerservice.database

import com.mjrcompany.eventplannerservice.domain.EventStatus
import com.mjrcompany.eventplannerservice.domain.UserInEventStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import org.jetbrains.exposed.sql.`java-time`.timestamp
import java.math.BigDecimal
import java.util.*


object Events : Table() {
    val id: Column<UUID> = uuid("id")
    val title: Column<String> = varchar("description", 250)
    val host: Column<UUID> = uuid("host").references(Users.id)
    val subject: Column<UUID> = uuid("subject_id").references(Subjects.id)
    val date = date("event_date")
    val createDate = timestamp("create_date")
    val address: Column<String?> = varchar("place", 100).nullable()
    val maxNumberGuests: Column<Int> = integer("max_number_guests")
    val totalCost: Column<BigDecimal> = decimal("total_cost", 8, 2)
    val additionalInfo: Column<String?> = text("additional_info").nullable()
    val status = enumeration("status", EventStatus::class)

}

object Subjects : Table() {
    val id: Column<UUID> = uuid("id")
    val name: Column<String> = varchar("name", 250)
    val details: Column<String?> = text("details").nullable()
    val imageUrl: Column<String?> = varchar("image", 250).nullable()
}

object Tasks : IntIdTable() {
    val details: Column<String> = text("details")
    val event: Column<UUID> = uuid("event_id").references(Events.id)
    val owner: Column<UUID?> = uuid("owner_id").references(Users.id).nullable()
}

object Users : Table() {
    val id: Column<UUID> = uuid("id")
    val name: Column<String> = varchar("name", 200)
    val email: Column<String> = varchar("email", 200)
}

object UsersInEvents : Table() {
    val event: Column<UUID> = uuid("event_id").references(Events.id)
    val user: Column<UUID> = uuid("user_id").references(Users.id)
    val status = enumeration("status", UserInEventStatus::class)
}

val entities = arrayOf(
    Subjects,
    Tasks,
    Users,
    Events,
    UsersInEvents
)