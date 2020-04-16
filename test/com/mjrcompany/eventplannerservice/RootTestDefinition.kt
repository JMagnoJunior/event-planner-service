package com.mjrcompany.eventplannerservice

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