package com.magnojr.foodwithfriends

import kotlin.test.AfterTest
import kotlin.test.BeforeTest

open class RootTestDefinition {

    @BeforeTest
    fun startDatabase() {
        DatabaseHandler.initHikariDatasource(hikariTest)

    }

    @AfterTest
    fun dropDatabase() {
        DatabaseHandler.destroy()
    }

}