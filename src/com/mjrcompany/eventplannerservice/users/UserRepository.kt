package com.mjrcompany.eventplannerservice.users

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.withPagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.database.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object UserRepository {

    fun createUser(userDTO: UserDomain.UserWritable): UUID {
        return transaction {
            Users.insert {
                it[id] = UUID.randomUUID()
                it[name] = userDTO.name
                it[email] = userDTO.email
            } get Users.id
        }
    }

    fun updateUser(id: UUID, userDTO: UserDomain.UserWritable) {
        transaction {
            Users.update({ Users.id eq id }) {
                writeAttributes(it, id, userDTO)
            }
        }
    }


    fun getAllUsers(pagination: Pagination): Page<UserDomain.User> {
        return transaction {
            Users.selectAll()
                .withPagination(pagination) {
                    DataMapper.mapToUser(it)
                }
        }
    }

    fun getUserById(id: UUID): Option<UserDomain.User> {
        return transaction {
            Users
                .select { Users.id eq id }
                .map {
                    DataMapper.mapToUser(it)
                }
                .firstOrNone()
        }
    }

    fun getUserByEmail(email: String): Option<UserDomain.User> {

        return transaction {
            Users
                .select { Users.email eq email }
                .map {
                    DataMapper.mapToUser(it)
                }
                .firstOrNone()
        }
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, userDTO: UserDomain.UserWritable) {
        it[Users.name] = userDTO.name
        it[Users.email] = userDTO.email
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, userDTO: UserDomain.UserWritable) {
        it[Users.id] = id
        writeAttributes(it, userDTO)
    }
}
