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

data class DishWriterDTO(val name: String, val details: String?) :
    Validable<DishWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, DishWriterDTO> {
        return withCustomValidator(this) {
            validate(this) {
                validate(DishWriterDTO::name).hasSize(
                    min = 3,
                    max = 100
                )
            }
        }
    }
}

data class UserWriterDTO(val name: String, val email: String?) :
    Validable<UserWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, UserWriterDTO> {
        return withCustomValidator(this) {
            validate(this) {
                validate(UserWriterDTO::name).hasSize(
                    min = 3,
                    max = 100
                )
                validate(UserWriterDTO::email).isEmail()
            }
        }
    }
}

data class TaskOwnerWriterDTO(val friendId: UUID) :
    Validable<TaskOwnerWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, TaskOwnerWriterDTO> {
        return withCustomValidator(this)
    }
}

data class TaskWriterDTO(val details: String) :
    Validable<TaskWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, TaskWriterDTO> {
        return withCustomValidator(this) {
            validate(this) {
                validate(TaskWriterDTO::details).hasSize(
                    min = 3
                )
            }
        }
    }
}

data class MeetingSubscriberWriterDTO(val friendId: UUID) :
    Validable<MeetingSubscriberWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, MeetingSubscriberWriterDTO> {
        return withCustomValidator(this)
    }
}

data class MeetingWriterDTO(
    val description: String,
    val host: UUID,
    val dish: UUID,
    val date: LocalDate,
    val place: String?,
    val maxNumberFriends: Int
) : Validable<MeetingWriterDTO> {
    override fun validation(): Either<ValidationErrorsDTO, MeetingWriterDTO> {
        return withCustomValidator(this) {
            validate(this) {
                validate(MeetingWriterDTO::description).hasSize(
                    min = 3,
                    max = 100
                )
                validate(MeetingWriterDTO::maxNumberFriends).isPositiveOrZero()
            }
        }
    }

}
