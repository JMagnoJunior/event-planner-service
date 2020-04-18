package com.mjrcompany.eventplannerservice.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.date
import java.util.*


object Meetings : Table("meeting") {
    val id: Column<UUID> = uuid("id")
    val description: Column<String> = varchar("description", 250)
    val host: Column<UUID> = uuid("host").references(Users.id)
    val dish: Column<UUID> = uuid("dish_id").references(Dishes.id)
    val date = date("release_date")
    val place: Column<String?> = varchar("place", 100).nullable()
    val maxNumberFriends: Column<Int> = integer("max_number_friends")
}

object Dishes : Table() {
    val id: Column<UUID> = uuid("id")
    val name: Column<String> = varchar("name", 250)
    val details: Column<String?> = text("details").nullable()
}

object Tasks : IntIdTable() {
    val details: Column<String> = text("details")
    val meeting: Column<UUID> = uuid("meeting_id").references(Meetings.id)
    val owner: Column<UUID?> = uuid("owner_id").references(Users.id).nullable()
}

object Users : Table() {
    val id: Column<UUID> = uuid("id")
    val name: Column<String> = varchar("name", 200)
    val email: Column<String> = varchar("email", 200)
}

object FriendsInMeetings : Table() {
    val meeting: Column<UUID> = uuid("meeting_id").references(Meetings.id)
    val friend: Column<UUID> = uuid("user_id").references(Users.id)
}

val entities = arrayOf(
    Dishes,
    Tasks,
    Users,
    Meetings,
    FriendsInMeetings
)