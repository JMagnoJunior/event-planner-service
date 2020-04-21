package com.mjrcompany.eventplannerservice

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.database.Events
import com.mjrcompany.eventplannerservice.database.entities
import com.mjrcompany.eventplannerservice.domain.EventWritable
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
import java.time.LocalDate


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
            val usersIds = listOf(
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
                EventWritable("event 1", usersIds[0], subjectsIds[0], LocalDate.now(), "Here", 10, BigDecimal.TEN),
                EventWritable("event 2", usersIds[1], subjectsIds[1], LocalDate.now(), "Here", 10, BigDecimal.TEN)
            ).map { EventService.createEvent(it) }
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