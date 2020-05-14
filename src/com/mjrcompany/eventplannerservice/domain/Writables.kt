package com.mjrcompany.eventplannerservice.domain

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.*
import org.valiktor.validate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


/*
This module contains classes  which can be used to write on the database.
The classes on this module which implements Validatable interface can also be received as a parameter from request.
 */



data class SubjectWritable(
    val name: String,
    val details: String?,
    val createdBy: UUID,
    val imageUrl: String? = null
) :
    Validatable<SubjectWritable> {
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
    Validatable<UserWritable> {
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
    Validatable<TaskOwnerWritable> {
    override fun validation(): Either<ValidationErrorsDTO, TaskOwnerWritable> {
        return withCustomValidator(this)
    }
}

data class TaskWritable(val details: String) :
    Validatable<TaskWritable> {
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

data class EventSubscriberWritable(val guestId: UUID) :
    Validatable<EventSubscriberWritable> {
    override fun validation(): Either<ValidationErrorsDTO, EventSubscriberWritable> {
        return withCustomValidator(this)
    }
}

data class AcceptGuestInEventWritable(val guestId: UUID, val status: UserInEventStatus) :
    Validatable<AcceptGuestInEventWritable> {
    override fun validation(): Either<ValidationErrorsDTO, AcceptGuestInEventWritable> {
        return withCustomValidator(this)
    }
}


data class EventWritable(
    val title: String,
    val host: UUID,
    val subject: UUID,
    val date: LocalDateTime,
    val address: String,
    val maxNumberGuest: Int,
    val totalCost: BigDecimal,
    val additionalInfo: String?
)

/*

The "Validatable" classes are received on the request, but can not be inserted on the database.
It has to be converted into a Writable class

 */

data class SubjectValidatable(
    val name: String,
    val details: String?,
    val imageUrl: String? = null
) :
    Validatable<SubjectValidatable> {
    override fun validation(): Either<ValidationErrorsDTO, SubjectValidatable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(SubjectValidatable::name).hasSize(
                    min = 3,
                    max = 100
                )
            }
        }
    }
}


data class EventValidatable(
    val title: String,
    val subject: UUID,
    val date: LocalDateTime,
    val address: String,
    val maxNumberGuest: Int,
    val totalCost: BigDecimal,
    val additionalInfo: String?
) : Validatable<EventValidatable> {
    override fun validation(): Either<ValidationErrorsDTO, EventValidatable> {
        return withCustomValidator(this) {
            validate(this) {
                validate(EventValidatable::title).hasSize(
                    min = 3,
                    max = 100
                )
                validate(EventValidatable::maxNumberGuest).isPositiveOrZero()
                validate(EventValidatable::totalCost).isLessThan(BigDecimal.valueOf(1000000.00)).isPositive()
            }
        }
    }
}