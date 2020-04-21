package com.mjrcompany.eventplannerservice.domain

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


data class User(val id: UUID, val name: String, val email: String?)

data class Guest(val id: UUID, val name: String, val email: String?, val status: UserInEventStatus)

data class Subject(val id: UUID, val name: String, val detail: String?)

data class Task(
    val id: Int,
    val details: String,
    val eventId: UUID,
    val owner: UUID?
)

data class Event(
    val id: UUID,
    val title: String,
    val host: User,
    val subject: Subject?,
    val date: LocalDate,
    val createDate: LocalDateTime,
    val place: String?,
    val maxNumberGuest: Int,
    val tasks: List<Task> = emptyList(),
    val guests: List<Guest> = emptyList(),
    val totalCost: BigDecimal,
    val additionalInfo: String?,
    val eventStatus: EventStatus
)

enum class EventStatus(val status: String) {
    Open("open"),
    Close("close")
}

enum class UserInEventStatus(val status: String) {
    Pending("pending"),
    Accept("Accept"),
    Reject("Reject")
}