package com.mjrcompany.eventplannerservice.routes

import com.google.gson.reflect.TypeToken
import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.subjects.SubjectDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


@KtorExperimentalAPI
class SubjectRoutesTest : RootTestDefinition() {

    @Test
    fun `it should create a subject with created by equals to id token user when creating a new subject`() {

        val user = TestDatabaseHelper.generateUser("test@mail.com").let {
            TestDatabaseHelper.queryUserByEmail(it)
        }
        val newSubject = SubjectDomain.SubjectValidatable(getRandomString(10), getRandomString(10), getRandomString(10))

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/subjects/") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                buildXIdToken(this, user.email, user.name, user.id)
                setBody(gson.toJson(newSubject))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
                val subjectId = gson.fromJson(
                    response.content,
                    UUID::class.java
                )

                assertNotNull(subjectId)

                val subjectCreated =
                    TestDatabaseHelper.querySubjectById(subjectId)

                assertEquals(newSubject.name, subjectCreated.name)
                assertEquals(newSubject.details, subjectCreated.detail)
                assertEquals(newSubject.imageUrl, subjectCreated.imageUrl)
                assertEquals(user.id, subjectCreated.createdBy)

            }
        }
    }

    @Test
    fun `it should list all subjects created by an user when filter get all by user id`() {

        val subjectOwner = TestDatabaseHelper.generateUser("test@mail.com").let {
            TestDatabaseHelper.queryUserByEmail(it)
        }

        val otherUser = TestDatabaseHelper.generateUser("other@mail.com").let {
            TestDatabaseHelper.queryUserByEmail(it)
        }

        val subjectsFromUser = listOf(generateSubject(subjectOwner), generateSubject(subjectOwner))
        val subjectsFromAnotherUser = listOf(generateSubject(otherUser))

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/subjects/?userId=${subjectOwner.id}").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val result: Page<SubjectDomain.Subject> =
                    gson.fromJson(response.content, object : TypeToken<Page<SubjectDomain.Subject?>?>() {}.type)

                assertEquals(subjectsFromUser.size, result.items.size)

                result.items.forEach {
                    assertTrue("it contains all subjects from user") { subjectsFromUser.contains(it) }
                    assertTrue("it not contains subjects another from user") { !subjectsFromAnotherUser.contains(it) }
                }

            }
        }
    }

    @Test
    fun `it should return a subject when get subject by id`() {
        withCustomTestApplication({ module(testing = true) }) {

            val user = TestDatabaseHelper.generateUser("test@mail.com").let {
                TestDatabaseHelper.queryUserByEmail(it)
            }
            val subject = generateSubject(user)

            handleRequest(HttpMethod.Get, "/subjects/${subject.id}").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val result = gson.fromJson(response.content, SubjectDomain.Subject::class.java)
                assertEquals(subject, result)
            }
        }
    }

    private fun generateSubject(user: UserDomain.User): SubjectDomain.Subject {
        val id = TestDatabaseHelper.generateSubject(
            getRandomString(10),
            getRandomString(10),
            user.id,
            getRandomString(10)
        )
        return TestDatabaseHelper.querySubjectById(id)
    }

}