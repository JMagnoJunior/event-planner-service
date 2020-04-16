package com.mjrcompany.eventplannerservice

import com.mjrcompany.eventplannerservice.database.entities
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


object DatabaseHandler {

    fun initHikariDatasource(hikari: () -> HikariDataSource = hikariDefault): Database {
        val db = Database.connect(hikari())
        transaction(db) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(*entities)
        }
        return db
    }

    fun destroy() {
        transaction {
            SchemaUtils.drop(*entities)
        }
    }

}

val hikariDefault: () -> HikariDataSource = fun(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.h2.Driver"
    config.jdbcUrl = "jdbc:h2:file:./data/local_food_meeting_db"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    config.validate()
    return HikariDataSource(config)
}


val hikariTest: () -> HikariDataSource = fun(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.h2.Driver"
    config.jdbcUrl = "jdbc:h2:mem:test"
    config.maximumPoolSize = 3
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    config.validate()
    return HikariDataSource(config)
}