package com.mjrcompany.eventplannerservice

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.util.LocalDateAdapter
import com.mjrcompany.eventplannerservice.util.LocalDateTimeAdapter
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class UserRoutesTest : RootTestDefinition() {

    @Test
    fun `it should get a dish by id`() {

        val dishName = "test"
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID(), dishName)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/subjects/$dishId").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val dish = Gson().fromJson(response.content, Subject::class.java)
                assertEquals(dishId, dish.id)
                assertEquals(dishName, dish.name)
            }
        }
    }

    @Test
    fun `it should get a  by id`() {

        val id = UUID.randomUUID()
        val hostName = "test"
        val hostEmail = "mail@mail.com"
        TestDatabaseHelper.addUser(id, hostName, hostEmail)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/users/$id").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val user = Gson().fromJson(response.content, User::class.java)
                assertEquals(id, user.id)
                assertEquals(hostName, user.name)
                assertEquals(hostEmail, user.email)
            }
        }
    }

    @Test
    fun `a host should get an event by id`() {
        val id = UUID.randomUUID()
        TestDatabaseHelper.addMeeting(id)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/events/${id}").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val event = gson.fromJson(response.content, Event::class.java)
                assertEquals(id, event.id)
            }
        }
    }


}

