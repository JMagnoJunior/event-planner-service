package com.mjrcompany.eventplannerservice.users


import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.authorization.IdTokenPayload
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWritable
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import java.util.*

object UserService {

    val createUser = fun(user: UserWritable): ServiceResult<UUID> {
        return Either.right(
            UserRepository.createUser(
                user
            )
        )
    }


    val getUser = fun(id: UUID): ServiceResult<User> {
        return when (val result =
            UserRepository.getUserById(id)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("User not Found!"))
        }
    }

    private val updateUser = fun(id: UUID, user: UserWritable): ServiceResult<Unit> {
        return Either.right(
            UserRepository.updateUser(
                id,
                user
            )
        )
    }

    val getUserByEmail = fun(email: String): ServiceResult<User> {
        return when (val result =
            UserRepository.getUserByEmail(email)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("User not Found!"))
        }
    }

    fun upsertUserFromIdPayload(payload: IdTokenPayload): ServiceResult<User> {
        val maybeUser = getUserByEmail(payload.email)
            .toOption()

        val user = if (maybeUser is Some) {
            maybeUser.t
        } else {
            val userWritable = UserWritable(payload.name, payload.email)
            val result = createUser(userWritable)
            if (result is Either.Left) {
                return result
            }
            User(result.toOption().orNull()!!, payload.name, payload.email)
        }
        return Either.right(user)
    }

    val crudResources = CrudResource(
        createUser,
        updateUser,
        getUser
    )
}

