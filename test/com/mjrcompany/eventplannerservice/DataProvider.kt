package com.mjrcompany.eventplannerservice

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import io.ktor.server.testing.TestApplicationRequest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

fun getEventDTO(subjectId: UUID): EventDTO {

    return EventDTO(
        title = "test",
//        host = hostEmail,
        address = "somwhere",
        subject = subjectId,
        maxNumberGuest = 10,
        date = LocalDateTime.now(),
        totalCost = BigDecimal.TEN,
        additionalInfo = null
    )
}

fun addAuthenticationHeader(testApplicationRequest: TestApplicationRequest) {
    testApplicationRequest.addHeader(
        "Authorization",
        "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4ZmE1YTQ0YS01NzQ5LTQyNGMtYWMwYS05YjAzYjRjYzA3NDciLCJ0b2tlbl91c2UiOiJhY2Nlc3MiLCJzY29wZSI6Im9wZW5pZCBwcm9maWxlIGVtYWlsIiwiYXV0aF90aW1lIjoxNTg3Mzc0MzQxLCJpc3MiOiJodHRwczovL2NvZ25pdG8taWRwLmV1LWNlbnRyYWwtMS5hbWF6b25hd3MuY29tL2V1LWNlbnRyYWwtMV90VUR3SFhuczUiLCJleHAiOjIwODczNzc5NDEsImlhdCI6MTU4NzM3NDM0MSwidmVyc2lvbiI6MiwianRpIjoiY2ZhNGEwZmMtNjE3NS00YTYyLTgwYWItYWNiZGRhNGI1MzM3IiwiY2xpZW50X2lkIjoidXA1dGMzYWV0ZDFza2dnYm9qZWRmanJxaCIsInVzZXJuYW1lIjoiOGZhNWE0NGEtNTc0OS00MjRjLWFjMGEtOWIwM2I0Y2MwNzQ3In0.hsRAWCpd92TMnZgL2Gf8v3VGRF0s4roLYpnWECdWZiE"
    )

}

fun buildXIdToken(testApplicationRequest: TestApplicationRequest, email: String, name: String) {
    val jwt = JWT.create()
        .withClaim("email", email)
        .withClaim("name", name)
        .withIssuer("http://cheetos.com")
        .sign(Algorithm.HMAC256("secret"))


    testApplicationRequest.addHeader(
        "X-Id-Token",
        jwt
    )
}