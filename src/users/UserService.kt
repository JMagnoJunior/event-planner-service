package com.magnojr.foodwithfriends.users


import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.magnojr.foodwithfriends.commons.NotFoundException
import com.magnojr.foodwithfriends.commons.ResponseErrorException
import com.magnojr.foodwithfriends.commons.User
import com.magnojr.foodwithfriends.commons.UserWriterDTO
import com.magnojr.foodwithfriends.core.CrudResource
import java.util.*

object UserService {

    private val createUser = fun(userDTO: UserWriterDTO): Either<ResponseErrorException, UUID> {
        return Either.right(UserRepository.createUser(userDTO))
    }

    private val getUser = fun(id: UUID): Either<ResponseErrorException, User> {
        return when (val result = UserRepository.getUserById(id)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("User not Found!"))
        }
    }

    private val updateUser = fun(id: UUID, userDTO: UserWriterDTO): Either<ResponseErrorException, Unit> {
        return Either.right(UserRepository.updateUser(id, userDTO))
    }

    val crudResources = CrudResource(createUser, updateUser, getUser)
}

