package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.routes


import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.s3.ImageUploadService
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route


fun Route.signedUrl() {
    route("/signed-url") {
        get("/get-image/{image-name}") {
            val imageName = call.parameters["image-name"].toString()
            val result = ImageUploadService.generateSignedGettURL(imageName)
            call.respond(result)
        }

        get("/put-image/{image-name}") {
            val imageName = call.parameters["image-name"].toString()
            val result = ImageUploadService.generateSignedPutURL(imageName)
            call.respond(result)
        }
    }
}


