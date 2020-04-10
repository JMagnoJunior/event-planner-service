package com.magnojr.foodwithfriends.commons

import java.time.LocalDate
import java.util.*


data class User(val id: UUID, val name: String, val email: String?)

data class Dish(val id: UUID, val name: String, val detail: String?)

data class Task(
    val id: Int,
    val details: String,
    val meetingId: UUID
)

data class Meeting(
    val id: UUID,
    val description: String,
    val user: User?,
    val dish: Dish?,
    val date: LocalDate,
    val place: String?,
    val maxNumberFriend: Int,
    val tasks: List<Task>?
)

