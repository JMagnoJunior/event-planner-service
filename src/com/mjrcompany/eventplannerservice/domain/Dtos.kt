package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.functions.isLessThan
import org.valiktor.functions.isPositive
import org.valiktor.functions.isPositiveOrZero
import org.valiktor.validate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*


data class EventDTO(
    val title: String,
    val host: String,
    val subject: UUID,
    val date: LocalDateTime,
    val address: String,
    val maxNumberGuest: Int,
    val totalCost: BigDecimal,
    val additionalInfo: String?
) : Validable<EventDTO> {
    override fun validation(): Either<ValidationErrorsDTO, EventDTO> {
        return withCustomValidator(this) {
            validate(this) {
                validate(EventDTO::title).hasSize(
                    min = 3,
                    max = 100
                )
                validate(EventDTO::maxNumberGuest).isPositiveOrZero()
                validate(EventDTO::totalCost).isLessThan(BigDecimal.valueOf(1000000.00)).isPositive()
            }
        }
    }

}
