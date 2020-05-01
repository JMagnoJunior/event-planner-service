package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import com.mjrcompany.eventplannerservice.core.CrudRestApi
import com.mjrcompany.eventplannerservice.core.getDefaultIdAsUUID
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import io.ktor.routing.Route
import io.ktor.routing.route


fun Route.subjects() {
    route("/subjects") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, SubjectService.crudResources
        )
    }
}


