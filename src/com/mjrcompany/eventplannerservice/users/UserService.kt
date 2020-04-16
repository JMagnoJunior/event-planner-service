package com.mjrcompany.eventplannerservice.users


import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.domain.User
import com.mjrcompany.eventplannerservice.domain.UserWriterDTO
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import java.util.*

object UserService {

    private val createUser = fun(userDTO: UserWriterDTO): ServiceResult<UUID> {
        return Either.right(
            UserRepository.createUser(
                userDTO
            )
        )
    }

    private val getUser = fun(id: UUID): ServiceResult<User> {
        return when (val result =
            UserRepository.getUserById(id)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("User not Found!"))
        }
    }

    private val updateUser = fun(id: UUID, userDTO: UserWriterDTO): ServiceResult<Unit> {
        return Either.right(
            UserRepository.updateUser(
                id,
                userDTO
            )
        )
    }

    val crudResources = CrudResource(
        createUser,
        updateUser,
        getUser
    )
}

