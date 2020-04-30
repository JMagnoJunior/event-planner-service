package com.mjrcompany.eventplannerservice.routes

import com.google.gson.Gson
import com.mjrcompany.eventplannerservice.*
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
import kotlin.test.assertNotEquals


@KtorExperimentalAPI
class UserRoutesTest : RootTestDefinition() {

    @Test
    fun `it should receive a host when get by id`() {
        withCustomTestApplication({ module(testing = true) }) {
            val userName = "test"
            val userEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(
                UUID.randomUUID(),
                userName,
                userEmail
            )


            handleRequest(HttpMethod.Get, "/users/$id").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val user = Gson().fromJson(response.content, User::class.java)
                assertEquals(id, user.id)
                assertEquals(userName, user.name)
                assertEquals(userEmail, user.email)
            }
        }
    }

    @Test
    fun `it should receive a user when get by email`() {
        withCustomTestApplication({ module(testing = true) }) {

            val userName = "test"
            val userEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(
                UUID.randomUUID(),
                userName,
                userEmail
            )


            handleRequest(HttpMethod.Get, "/users/$id").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val user = Gson().fromJson(response.content, User::class.java)
                assertEquals(id, user.id)
                assertEquals(userName, user.name)
                assertEquals(userEmail, user.email)
            }
        }
    }

    @Test
    fun `it should create a user if it does not exist`() {
        withCustomTestApplication({ this.module(testing = true) }) {
            val userName = "test"
            val userEmail = "mail@mail.com"
            val userWritable = UserWritable(userName, userEmail)

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
    fun `it should update the host name when updating user`() {
        withCustomTestApplication({ module(testing = true) }) {
            val userName = "test"
            val userEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(
                UUID.randomUUID(),
                userName,
                userEmail
            )

            val userNewName = "new test"
            val userWritable = UserWritable(userNewName, userEmail)

            handleRequest(HttpMethod.Put, "/users/$id") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }

            val updatedUser: User =
                TestDatabaseHelper.queryUserById(id)
            assertEquals(userNewName, updatedUser.name)

        }
    }

    @Test
    fun `it should not update email when updating user`() {
        withCustomTestApplication({ module(testing = true) }) {
            val userName = "test"
            val userEmail = "mail@mail.com"
            val id = TestDatabaseHelper.generateUser(
                UUID.randomUUID(),
                userName,
                userEmail
            )

            val hostNewEmail = "newEmail@mail.com"
            val userWritable = UserWritable(userName, hostNewEmail)

            handleRequest(HttpMethod.Put, "/users/$id") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())
            }

            val updatedUser: User =
                TestDatabaseHelper.queryUserById(id)
            assertNotEquals(hostNewEmail, updatedUser.name)
            assertNotEquals(userEmail, updatedUser.name)

        }
    }

    @Test
    fun `it should fail to create users with duplicated email`() {
        withCustomTestApplication({ module(testing = true) }) {
            val userName = "test"
            val sameEmail = "same@mail.com"

            val userWritable1 = UserWritable(userName, sameEmail)

            val userName2 = "test2"
            val userWritable2 = UserWritable(userName2, sameEmail)

            handleRequest(HttpMethod.Post, "/users/") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable1))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
            }

            handleRequest(HttpMethod.Post, "/users/") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(userWritable2))
            }.apply {
                assertEquals(HttpStatusCode.InternalServerError, response.status())
                val error = gson.fromJson(
                    response.content,
                    ErrorResponse::class.java
                )
                assertEquals(error.message, "The user already exists on the database")
            }

        }
    }


}

