package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.UsersOrderBy
import com.mjrcompany.eventplannerservice.core.CrudRestApi
import com.mjrcompany.eventplannerservice.core.getDefaultIdAsUUID
import com.mjrcompany.eventplannerservice.users.UserService
import io.ktor.routing.Route
import io.ktor.routing.route


fun Route.users() {
    route("/users") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID,
            UserService.crudResources,
            UsersOrderBy.orderBy
        )
    }
}

