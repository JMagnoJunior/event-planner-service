package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.s3

import arrow.core.Either
import com.amazonaws.AmazonServiceException
import com.amazonaws.HttpMethod
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.mjrcompany.eventplannerservice.S3LinkException
import com.mjrcompany.eventplannerservice.core.ServiceResult
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*


object ImageUploadService {
    private var log = LoggerFactory.getLogger(ImageUploadService::class.java)

    const val EXPIRATION_TIME = 3 // minutes

    fun generateSignedGettURL(imageName: String): ServiceResult<URL> {
        return withS3Client(imageName) {
            it.withMethod(HttpMethod.GET)
        }
    }

    fun generateSignedPutURL(imageName: String): ServiceResult<URL> {
        return withS3Client(imageName) {
            it.withMethod(HttpMethod.PUT).withContentType("image/jpeg")
        }
    }

    private fun withS3Client(
        imageName: String,
        block: (GeneratePresignedUrlRequest) -> GeneratePresignedUrlRequest
    ): ServiceResult<URL> {
        val bucketName = "event-planner-bucket"
        val objectKey = "images-event/$imageName"
        return try {
            val s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build()

            val expiration = Date()
            var expTimeMillis = expiration.time
            expTimeMillis += 1000 * 60 * EXPIRATION_TIME.toLong()
            expiration.time = expTimeMillis

            val generatePresignedUrl = GeneratePresignedUrlRequest(bucketName, objectKey).withExpiration(expiration)

            val url: URL = s3Client.generatePresignedUrl(block(generatePresignedUrl))
            Either.right(url)
        } catch (e: AmazonServiceException) {
            log.error("Could not generate  signed url for image $objectKey", e)
            Either.left(S3LinkException(e.message ?: "Error generating URL"))
        } catch (e: Exception) {
            log.error("Could not generate  signed url for image $objectKey", e)
            Either.left(S3LinkException(e.message ?: "Error generating URL"))
        }
    }
}