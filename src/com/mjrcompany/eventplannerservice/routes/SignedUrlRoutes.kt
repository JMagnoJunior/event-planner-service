package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.s3.ImageUploadService
import com.mjrcompany.eventplannerservice.core.withErrorTreatment
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route


fun Route.signedUrl() {
    route("/signed-url") {
        get("/get-image/{image-name}") {
            val imageName = call.parameters["image-name"].toString()
            val (status, body) = withErrorTreatment {
                HttpStatusCode.OK to ImageUploadService.generateSignedGettURL(imageName)
            }
            call.respond(status, body)
        }

        authenticate {
            get("/put-image/{image-name}") {
                val imageName = call.parameters["image-name"].toString()
                val (status, body) = withErrorTreatment {
                    HttpStatusCode.OK to ImageUploadService.generateSignedPutURL(imageName)
                }
                call.respond(status, body)
            }
        }
    }
}


