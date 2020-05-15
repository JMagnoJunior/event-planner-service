package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users

import arrow.core.Either
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.UserInEventStatus
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.validate
import java.util.*

sealed class UserDomain {
    data class User(val id: UUID, val name: String, val email: String) : UserDomain()

    data class UserWritable(val name: String, val email: String) :
        Validatable<UserWritable>, UserDomain() {
        override fun validation(): Either<ValidationErrorsDTO, UserWritable> {
            return withCustomValidator(this) {
                validate(this) {
                    validate(UserWritable::name).hasSize(
                        min = 3,
                        max = 100
                    )
                    validate(UserWritable::email).isEmail()
                }
            }
        }
    }

    data class GuestInEvent(val id: UUID, val name: String, val email: String, val status: UserInEventStatus) :
        UserDomain()
}