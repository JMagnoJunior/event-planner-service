package com.mjrcompany.eventplannerservice

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class RootTestDefinition {

    @BeforeTest
    fun startDatabase() {
        DatabaseHandler.initHikariDatasource(
            hikariTest
        )

    }

    @AfterTest
    fun dropDatabase() {
        DatabaseHandler.destroy()
    }

}