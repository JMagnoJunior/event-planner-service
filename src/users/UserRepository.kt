package com.magnojr.foodwithfriends.users

import arrow.core.Option
import arrow.core.firstOrNone
import com.magnojr.foodwithfriends.commons.DataMapper
import com.magnojr.foodwithfriends.commons.User
import com.magnojr.foodwithfriends.commons.UserWriterDTO
import com.magnojr.foodwithfriends.commons.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*

object UserRepository {

    fun createUser(userDTO: UserWriterDTO): UUID {
        lateinit var userId: UUID
        transaction {
            userId = Users.insert {
                it[id] = UUID.randomUUID()
                it[name] = userDTO.name
                it[email] = userDTO.email
            } get Users.id
        }
        return userId
    }

    fun updateUser(id: UUID, userDTO: UserWriterDTO) {
        transaction {
            Users.update({ Users.id eq id }) {
                UserRepository.writeAttributes(it, userDTO)
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

    private fun writeAttributes(it: UpdateBuilder<Any>, userDTO: UserWriterDTO) {
        it[Users.name] = userDTO.name
        it[Users.email] = userDTO.email
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, userDTO: UserWriterDTO) {
        it[Users.id] = id
        writeAttributes(it, userDTO)
    }
}
