package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import arrow.core.Either
import arrow.core.flatMap
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withFriendInEventRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.withHostRequestPermission
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.exchangeAuthCodeForJWTTokens
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.generateEventPlannerIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateAccessToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.cognito.validateCognitoIdToken
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.EventOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.TaskOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.UsersOrderBy
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.s3.ImageUploadService
import com.mjrcompany.eventplannerservice.core.*
import com.mjrcompany.eventplannerservice.domain.AcceptGuestInEventWritable
import com.mjrcompany.eventplannerservice.domain.EventSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.TaskOwnerWritable
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import com.mjrcompany.eventplannerservice.tasks.TaskService
import com.mjrcompany.eventplannerservice.users.UserService
import com.typesafe.config.ConfigFactory
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.KtorExperimentalAPI


fun Route.subjects() {
    route("/subjects") {
        CrudRestApi.createResource(
            this,
            getDefaultIdAsUUID, SubjectService.crudResources
        )
    }
}


