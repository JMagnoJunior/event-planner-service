package com.mjrcompany.eventplannerservice.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


data class User(val id: UUID, val name: String, val email: String?)

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
    val guests: List<User> = emptyList()
)

