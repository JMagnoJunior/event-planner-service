package com.mjrcompany.eventplannerservice.users


import arrow.core.*
import com.mjrcompany.eventplannerservice.DuplicatedUserException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.IdTokenCognitoPayload
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWritable
import org.slf4j.LoggerFactory
import java.util.*

object UserService {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    val createUser = fun(newUser: UserWritable): ServiceResult<UUID> {
        log.info("Will create the user $newUser")

        val result = withDatabaseErrorTreatment {
            when (UserRepository.getUserByEmail(newUser.email)) {
                is None -> {
                    UserRepository.createUser(
                        newUser
                    )
                }
                is Some -> {
                    log.info(" The user ${newUser.email} already exists")
                    throw DuplicatedUserException("The user already exists on the database")
                }
            }
        }

        if (result.isLeft()) {
            log.error("Error creating user: $newUser")
        }

        return result
    }


    val getUser = fun(id: UUID): ServiceResult<Option<User>> {
        log.debug("Querying the user: $id")
        val result = withDatabaseErrorTreatment {
            UserRepository.getUserById(id)
        }

        result.map { if (it.isEmpty()) log.info("user not found") }
        return result
    }

    val getAllUsers = fun(pagination: Pagination): ServiceResult<Page<User>> {
        return withDatabaseErrorTreatment {
            UserRepository.getAllUsers(pagination)
        }
    }

    val updateUser = fun(id: UUID, user: UserWritable): ServiceResult<Unit> {
        log.info("will update the user $user")
        val result = withDatabaseErrorTreatment {
            UserRepository.updateUser(
                id,
                user
            )
        }

        if (result.isLeft()) {
            log.error("error creating user: $user")
        }
        return result
    }

    val getUserByEmail = fun(email: String): ServiceResult<Option<User>> {
        log.debug("Quering the user: $email")
        val result = withDatabaseErrorTreatment {
            UserRepository.getUserByEmail(email)
        }
        result.map { if (it.isEmpty()) log.info("user not found") }
        return result
    }

    val upsertUserFromIdPayload = fun(cognitoIdTokenPayload: IdTokenCognitoPayload): ServiceResult<User> {
        log.info("will upsert the user $cognitoIdTokenPayload")

        val user = getUserByEmail(cognitoIdTokenPayload.email)
            .flatMap {
                when (it) {
                    is Some -> Either.right(it.t)
                    is None -> createUser(UserWritable(cognitoIdTokenPayload.name, cognitoIdTokenPayload.email))
                        .map { uuid -> User(uuid, cognitoIdTokenPayload.name, cognitoIdTokenPayload.email) }
                }
            }

        if (user.isLeft()) {
            log.info("error upserting the user $cognitoIdTokenPayload")
        }

        return user
    }

}
