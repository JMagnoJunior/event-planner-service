package com.mjrcompany.eventplannerservice.users

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.database.Users
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWritable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object UserRepository {

    fun createUser(userDTO: UserWritable): UUID {
        return transaction {
            Users.insert {
                it[id] = UUID.randomUUID()
                it[name] = userDTO.name
                it[email] = userDTO.email
            } get Users.id
        }
    }

    fun updateUser(id: UUID, userDTO: UserWritable) {
        transaction {
            Users.update({ Users.id eq id }) {
                writeAttributes(it, id, userDTO)
            }
        }
    }

    fun getUserById(id: UUID): Option<User> {
        lateinit var result: Option<User>
        transaction {
            result = Users
                .select { Users.id eq id }
                .map {
                    DataMapper.mapToUser(it)
                }
                .firstOrNone()
        }
        return result
    }

    fun getUserByEmail(email: String): Option<User> {
        lateinit var result: Option<User>
        transaction {
            result = Users
                .select { Users.email eq email }
                .map {
                    DataMapper.mapToUser(it)
                }
                .firstOrNone()
        }
        return result
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, userDTO: UserWritable) {
        it[Users.name] = userDTO.name
        it[Users.email] = userDTO.email
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, userDTO: UserWritable) {
        it[Users.id] = id
        writeAttributes(it, userDTO)
    }
}
