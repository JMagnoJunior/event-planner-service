package com.mjrcompany.eventplannerservice.domain

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import java.time.LocalDate
import java.util.*

data class DishWritable(val name: String, val details: String?) :
    Validable<DishWritable> {
    override fun validation(): Either<ValidationErrorsDTO, DishWritable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(DishWritable::name).hasSize(
                    min = 3,
                    max = 100
                )
            }
        }
    }
}

data class UserWritable(val name: String, val email: String?) :
    Validable<UserWritable> {
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

data class TaskOwnerWritable(val friendId: UUID) :
    Validable<TaskOwnerWritable> {
    override fun validation(): Either<ValidationErrorsDTO, TaskOwnerWritable> {
        return withCustomValidator(this)
    }
}

data class TaskWritable(val details: String) :
    Validable<TaskWritable> {
    override fun validation(): Either<ValidationErrorsDTO, TaskWritable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(TaskWritable::details).hasSize(
                    min = 3
                )
            }
        }
    }
}

data class MeetingSubscriberWritable(val friendId: UUID) :
    Validable<MeetingSubscriberWritable> {
    override fun validation(): Either<ValidationErrorsDTO, MeetingSubscriberWritable> {
        return withCustomValidator(this)
    }
}

data class MeetingWritable(
    val description: String,
    val host: UUID,
    val dish: UUID,
    val date: LocalDate,
    val place: String?,
    val maxNumberFriends: Int
) : Validable<MeetingWritable> {
    override fun validation(): Either<ValidationErrorsDTO, MeetingWritable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(MeetingWritable::description).hasSize(
                    min = 3,
                    max = 100
                )
                validate(MeetingWritable::maxNumberFriends).isPositiveOrZero()
            }
        }
    }

}
