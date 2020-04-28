package com.mjrcompany.eventplannerservice

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import com.mjrcompany.eventplannerservice.database.Events
import com.mjrcompany.eventplannerservice.database.entities
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import com.mjrcompany.eventplannerservice.domain.UserWritable
import com.mjrcompany.eventplannerservice.event.EventService
import com.mjrcompany.eventplannerservice.subjects.SubjectService
import com.mjrcompany.eventplannerservice.users.UserService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime


object DatabaseSetup {

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

    fun initDevDb() {

        val result = transaction { Events.selectAll().count() }

        if (result <= 0) {
            listOf(
                UserWritable("Marina", "ninafroes@gmail.com"),
                UserWritable("Magno", "is.magnojr@gmail.com")
            ).map {
                UserService.createUser(it).toOption().getOrElse { throw RuntimeException("error creating admin user") }
            }

            val subjectsIds =
                listOf(
                    SubjectWritable("subject 1", "detail 1"),
                    SubjectWritable("subject 2", "detail 2")
                )
                    .map {
                        SubjectService.createSubject(it).toOption()
                            .getOrElse { throw RuntimeException("error creating admin subject") }
                    }

            listOf(
                EventDTO(
                    "event 1",
                    "ninafroes@gmail.com",
                    subjectsIds[0],
                    LocalDateTime.now(),
                    "Here",
                    10,
                    BigDecimal.TEN,
                    "additional info"
                ),
                EventDTO(
                    "event 2",
                    "is.magnojr@gmail.com",
                    subjectsIds[1],
                    LocalDateTime.now(),
                    "Here",
                    10,
                    BigDecimal.TEN,
                    "additional info"
                )
            ).map { EventService.createEvent(it) }
        }

    }

}

val hikariDefault: () -> HikariDataSource = fun(): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = "org.h2.Driver"
    config.jdbcUrl = "jdbc:h2:file:./data/local_event_planner_db"
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