package com.mjrcompany.eventplannerservice

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.createTestEnvironment
import io.ktor.server.testing.withApplication
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class RootTestDefinition {

    @BeforeTest
    fun startDatabase() {
        DatabaseSetup.initHikariDatasource(
            hikariTest
        )

    }

    @AfterTest
    fun dropDatabase() {
        DatabaseSetup.destroy()
    }

}

@KtorExperimentalAPI
fun <R> withCustomTestApplication(moduleFunction: Application.() -> Unit, test: TestApplicationEngine.() -> R): R {

    return withApplication(
        createTestEnvironment {
            this.config = MapApplicationConfig(
                "cognito.jwt-validation.issuer" to "https://cognito-idp.eu-central-1.amazonaws.com/eu-central-1_tUDwHXns5",
                "cognito.jwt-validation.kidAccessToken" to "test",
                "cognito.jwt-validation.jwtProvider" to "",
                "test.jwt-validation.secret" to "secret",
                "event-planner.jwt.issue" to "http://cheetos.com",
                "event-planner.jwt.secret" to "secret"

            )
        }
    ) {
        moduleFunction(application)
        test()
    }

}