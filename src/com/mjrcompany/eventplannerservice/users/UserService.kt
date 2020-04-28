package com.mjrcompany.eventplannerservice.users


import arrow.core.*
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.IdTokenPayload
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWritable
import com.mjrcompany.eventplannerservice.tasks.TaskService
import org.slf4j.LoggerFactory
import java.util.*

object UserService {
    private val log = LoggerFactory.getLogger(UserService::class.java)

    val createUser = fun(newUser: UserWritable): ServiceResult<UUID> {
        log.info("Will create the user $newUser")

        val result = withDatabaseErrorTreatment {
            when (val user = UserRepository.getUserByEmail(newUser.email)) {
                is None -> {
                    UserRepository.createUser(
                        newUser
                    )
                }
                is Some -> {
                    log.info(" The user ${newUser.email} already exists")
                    user.t.id
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

    private val updateUser = fun(id: UUID, user: UserWritable): ServiceResult<Unit> {
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

    val upsertUserFromIdPayload = fun(payload: IdTokenPayload): ServiceResult<User> {
        log.info("will upsert the user $payload")

        val user = getUserByEmail(payload.email)
            .flatMap {
                when (it) {
                    is Some -> Either.right(it.t)
                    is None -> createUser(UserWritable(payload.name, payload.email))
                        .map { uuid -> User(uuid, payload.name, payload.email) }
                }
            }

        if (user.isLeft()) {
            log.info("error upserting the user $payload")
        }

        return user
    }

    val crudResources = CrudResource(
        createUser,
        updateUser,
        getUser,
        getAllUsers
    )
}
