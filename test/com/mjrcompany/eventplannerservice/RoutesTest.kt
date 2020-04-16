package com.mjrcompany.eventplannerservice

import com.google.gson.Gson
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.User
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class UserRoutesTest : RootTestDefinition() {

    @Test
    fun `it should get a dish by id`() {

        val dishName = "test"
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID(), dishName)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dishes/$dishId").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val dish = Gson().fromJson(response.content, Dish::class.java)
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
        TestDatabaseHelper.addHost(id, hostName, hostEmail)

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
    fun `a host should get a meeting by id`() {
        val id = UUID.randomUUID()
        TestDatabaseHelper.addMeeting(id)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/meetings/${id}").apply {

                assertEquals(HttpStatusCode.OK, response.status())
                val user = Gson().fromJson(response.content, User::class.java)
                assertEquals(id, user.id)
            }
        }
    }


}

