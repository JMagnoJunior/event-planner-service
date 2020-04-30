package com.mjrcompany.eventplannerservice.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*


data class User(val id: UUID, val name: String, val email: String)

data class GuestInEvent(val id: UUID, val name: String, val email: String, val status: UserInEventStatus)

data class Subject(val id: UUID, val name: String, val detail: String?, val imageUrl: String?)

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
    val subject: Subject,
    val date: LocalDateTime,
    val createDate: LocalDateTime,
    val address: String,
    val maxNumberGuest: Int,
    val tasks: List<Task> = emptyList(),
    val guestInEvents: List<GuestInEvent> = emptyList(),
    val totalCost: BigDecimal,
    val additionalInfo: String?,
    val eventStatus: EventStatus,
    val pricePerGuest: Number = {
        val format = NumberFormat.getInstance()

        if (guestInEvents.isNotEmpty()) {
            format.parse((totalCost / BigDecimal(guestInEvents.size)).toString())
        } else {
            format.parse(totalCost.toString())
        }
    }()

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