package com.magnojr.foodwithfriends.commons


import com.magnojr.foodwithfriends.core.CrudRestApi
import com.magnojr.foodwithfriends.core.getDefaultIdAsUUID
import com.magnojr.foodwithfriends.core.getIdAsInt
import com.magnojr.foodwithfriends.dishes.DishService
import com.magnojr.foodwithfriends.meetings.MeetingService
import com.magnojr.foodwithfriends.tasks.TaskService
import com.magnojr.foodwithfriends.users.UserService
import io.ktor.routing.Route
import io.ktor.routing.route


fun Route.meeting() {
    route("/meetings") {
        CrudRestApi.createResource(this, getDefaultIdAsUUID, MeetingService.crudResources)
        CrudRestApi.createSubResource(this, "tasks", getDefaultIdAsUUID, getIdAsInt, TaskService.crudResources)
    }
}

fun Route.dishes() {
    route("/dishes") {
        CrudRestApi.createResource(this, getDefaultIdAsUUID, DishService.crudResources)
    }
}

fun Route.users() {
    route("/users") {
        CrudRestApi.createResource(this, getDefaultIdAsUUID, UserService.crudResources)
    }
}

