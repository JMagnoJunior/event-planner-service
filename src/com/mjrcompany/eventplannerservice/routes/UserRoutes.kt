package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain
import com.mjrcompany.eventplannerservice.core.getParamIdAsUUID
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import com.mjrcompany.eventplannerservice.core.withValidRequest
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SortOrder


fun Route.users() {
    route("/users") {

        get("/") {
            val totalItems = call.parameters["totalItems"]?.toInt() ?: TOTAL_ITEMS_DEFAULT
            val page = call.parameters["Page"]?.toLong() ?: PAGE_DEFAULT
            val pagination = Pagination(page, totalItems, OrderBy(UsersOrderBy.Name, SortOrder.ASC))
            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to UserService.getAllUsers(pagination)
            }
            call.respond(status, body)
        }

        get("/{id}") {
            val userId = call.getParamIdAsUUID()
            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to UserService.getUser(userId)
                    .flatMap {
                        it.fold(
                            { Either.left(NotFoundException("User not found")) },
                            { resource -> Either.right(resource) }
                        )
                    }
            }
            call.respond(status, body)
        }

        authenticate {
            post("/") {
                val newUser = call.receive<UserDomain.UserWritable>()
                val (status, body) = withValidRequest(newUser) {
                    HttpStatusCode.Created to UserService.createUser(newUser)
                }
                call.respond(status, body)
            }

            put("/{id}") {
                val userId = call.getParamIdAsUUID()
                val user = call.receive<UserDomain.UserWritable>()
                val (status, body) = withValidRequest(user) {
                    HttpStatusCode.Accepted to UserService.updateUser(userId, user)
                }
                call.respond(status, body)
            }
        }

    }
}

