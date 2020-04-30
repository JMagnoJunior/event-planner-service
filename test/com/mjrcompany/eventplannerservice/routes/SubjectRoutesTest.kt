package com.mjrcompany.eventplannerservice.routes

import com.mjrcompany.eventplannerservice.*
import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.util.KtorExperimentalAPI
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@KtorExperimentalAPI
class SubjectRoutesTest : RootTestDefinition() {

    @Test
    fun `it should create a subject when user is authenticated`() {
        val subject = SubjectWritable("subject name", "subject detail", "some url")

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/subjects/") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(subject))
            }.apply {
                assertEquals(HttpStatusCode.Created, response.status())
                val subjectId = gson.fromJson(
                    response.content,
                    UUID::class.java
                )

                assertNotNull(subjectId)

                val subjectCreated =
                    TestDatabaseHelper.querySubjectById(subjectId)

                assertEquals(subject.name, subjectCreated.name)
                assertEquals(subject.details, subjectCreated.detail)
                assertEquals(subject.imageUrl, subjectCreated.imageUrl)

            }
        }
    }

    @Test
    fun `it should modify subject when updating and user is authenticated`() {

        val subjectName = "subject name"
        val subjectDetail = "subject detail"
        val subjectId = TestDatabaseHelper.generateSubject(
            subjectName,
            subjectDetail
        )

        val modifiedName = "modified subject name"
        val modifiedImageUrl = "modified url"
        val modifiedSubject = SubjectWritable(modifiedName, subjectDetail, modifiedImageUrl)

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Put, "/subjects/$subjectId") {
                addHeader("Content-Type", "application/json")
                addAuthenticationHeader(this)
                setBody(gson.toJson(modifiedSubject))
            }.apply {
                assertEquals(HttpStatusCode.Accepted, response.status())

                val result =
                    TestDatabaseHelper.querySubjectById(subjectId)
                assertEquals(modifiedName, result.name)
                assertEquals(modifiedImageUrl, result.imageUrl)

            }
        }
    }

    @Test
    fun `it should return a subject when get by id`() {
        val subjectName = "subject name"
        val subjectDetail = "subject detail"
        val subjectImageUrl = "subject detail"

        val subjectId = TestDatabaseHelper.generateSubject(
            subjectName,
            subjectDetail,
            subjectImageUrl
        )

        withCustomTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/subjects/$subjectId").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val result = gson.fromJson(
                    response.content,
                    Subject::class.java
                )

                assertEquals(subjectName, result.name)
                assertEquals(subjectDetail, result.detail)
                assertEquals(subjectId, result.id)
                assertEquals(subjectImageUrl, result.imageUrl)
            }
        }
    }

}
