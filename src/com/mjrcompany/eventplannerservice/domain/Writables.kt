package com.mjrcompany.eventplannerservice.domain

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEmail
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*

data class SubjectWritable(val name: String, val details: String?) :
    Validable<SubjectWritable> {
    override fun validation(): Either<ValidationErrorsDTO, SubjectWritable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(SubjectWritable::name).hasSize(
                    min = 3,
                    max = 100
                )
            }
        }
    }
}

data class UserWritable(val name: String, val email: String) :
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

data class EventSubscriberWritable(val friendId: UUID) :
    Validable<EventSubscriberWritable> {
    override fun validation(): Either<ValidationErrorsDTO, EventSubscriberWritable> {
        return withCustomValidator(this)
    }
}

data class EventWritable(
    val description: String,
    val host: UUID,
    val subject: UUID,
    val date: LocalDate,
    val place: String?,
    val maxNumberGuest: Int,
    val totalCost: BigDecimal
) : Validable<EventWritable> {
    override fun validation(): Either<ValidationErrorsDTO, EventWritable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(EventWritable::description).hasSize(
                    min = 3,
                    max = 100
                )
                validate(EventWritable::maxNumberGuest).isPositiveOrZero()
            }
        }
    }

}
