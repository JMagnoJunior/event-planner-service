package com.mjrcompany.eventplannerservice


import io.ktor.server.testing.withTestApplication
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() {
        withTestApplication({ module(testing = true) }) {

//            handleRequest(HttpMethod.Get, "/com.mjrcompany.eventplannerservice.meetings/").apply {
//                assertEquals(HttpStatusCode.OK, response.status())
//            }
        }
    }
}
