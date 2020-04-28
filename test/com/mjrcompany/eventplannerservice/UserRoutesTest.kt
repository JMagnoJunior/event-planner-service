package com.mjrcompany.eventplannerservice

import com.google.gson.Gson
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWritable
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


@KtorExperimentalAPI
class UserRoutesTest(): RootTestDefinition() {

    @Test
    fun `it should get a host by id`() {
        withCustomTestApplication({ module(testing = true) }) {
            val hostName = "test"
            val hostEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(UUID.randomUUID(), hostName, hostEmail)


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
    fun `it should get a host by email`() {
        withCustomTestApplication({ module(testing = true) }) {

            val hostName = "test"
            val hostEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(UUID.randomUUID(), hostName, hostEmail)


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
    fun `it should create a host`() {
        withCustomTestApplication({ this.module(testing = true) }) {
            val hostName = "test"
            val hostEmail = "mail@mail.com"
            val userWritable = UserWritable(hostName, hostEmail)

            handleRequest(HttpMethod.Post, "/users/") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }
        }
    }

    @Test
    fun `it should update a host name`() {
        withCustomTestApplication({ module(testing = true) }) {
            val hostName = "test"
            val hostEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(UUID.randomUUID(), hostName, hostEmail)

            val hostNewName = "new test"
            val userWritable = UserWritable(hostNewName, hostEmail)

            handleRequest(HttpMethod.Put, "/users/$id") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }

            val updatedUser: User = TestDatabaseHelper.queryUserById(id)
            assertEquals(hostNewName, updatedUser.name)

        }
    }

}

