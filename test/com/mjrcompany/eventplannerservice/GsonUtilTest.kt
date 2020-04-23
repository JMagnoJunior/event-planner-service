package com.mjrcompany.eventplannerservice

import com.google.gson.GsonBuilder
import com.mjrcompany.eventplannerservice.util.LocalDateAdapter
import com.mjrcompany.eventplannerservice.util.LocalDateTimeAdapter
import java.time.LocalDate
import java.time.LocalDateTime

val gson = GsonBuilder().registerTypeAdapter(
    LocalDate::class.java,
    LocalDateAdapter
).registerTypeAdapter(
    LocalDateTime::class.java,
    LocalDateTimeAdapter
).create()

