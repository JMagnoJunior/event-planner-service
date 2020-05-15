package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event

import arrow.core.Either
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.subjects.SubjectDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks.TaskDomain
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.users.UserDomain
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isPositive
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*

sealed class EventDomain {
    data class EventValidatable(
        val title: String,
        val subject: UUID,
        val date: LocalDateTime,
        val address: String,
        val maxNumberGuest: Int,
        val totalCost: BigDecimal,
        val additionalInfo: String?
    ) : Validatable<EventValidatable>, EventDomain() {
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

    data class Event(
        val id: UUID,
        val title: String,
        val host: UserDomain.User,
        val subject: SubjectDomain.Subject,
        val date: LocalDateTime,
        val createDate: LocalDateTime,
        val address: String,
        val maxNumberGuest: Int,
        val tasks: List<TaskDomain.Task> = emptyList(),
        val guestInEvents: List<UserDomain.GuestInEvent> = emptyList(),
        val totalCost: BigDecimal,
        val additionalInfo: String?,
        val eventStatus: EventStatus,
        val pricePerGuest: Number = {
            val format = NumberFormat.getInstance()

            if (guestInEvents.isNotEmpty()) {
                format.parse((totalCost / BigDecimal(guestInEvents.size)).toString())
            } else {
                format.parse(totalCost.toString())
            }
        }()

    )

    data class EventSubscriberWritable(val guestId: UUID) :
        Validatable<EventSubscriberWritable>, EventDomain() {
        override fun validation(): Either<ValidationErrorsDTO, EventSubscriberWritable> {
            return withCustomValidator(this)
        }
    }

    data class AcceptGuestInEventWritable(val guestId: UUID, val status: UserInEventStatus) :
        Validatable<AcceptGuestInEventWritable>, EventDomain() {
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
    ) : EventDomain()


}

enum class EventStatus(val status: String) {
    Open("open"),
    Close("close")
}

enum class UserInEventStatus(val status: String) {
    Pending("pending"),
    Accept("Accept"),
    Reject("Reject")
}
