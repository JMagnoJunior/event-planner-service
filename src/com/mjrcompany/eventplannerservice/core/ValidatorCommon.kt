package com.mjrcompany.eventplannerservice.core

import arrow.core.Either
import org.valiktor.ConstraintViolationException
import org.valiktor.i18n.mapToMessage
import java.util.*


interface Validable<T> {
    fun validation(): Either<ValidationErrorsDTO, T>
}

data class ValidationError(val field: String, val message: String)

data class ValidationErrorsDTO(val errors: List<ValidationError>)

fun <T> withCustomValidator(obj: T, block: () -> Unit = {}): Either<ValidationErrorsDTO, T> {
    try {
        block()
    } catch (ex: ConstraintViolationException) {
        val errors = ex.constraintViolations
            .mapToMessage(baseName = "messages", locale = Locale.ENGLISH)
            .map {
                ValidationError(
                    it.property,
                    it.message
                )
            }
        return Either.left(ValidationErrorsDTO(errors))
    }
    return Either.right(obj)
}