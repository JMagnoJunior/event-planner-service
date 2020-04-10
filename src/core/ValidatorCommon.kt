package com.magnojr.foodwithfriends.core

import arrow.core.Either
import org.valiktor.ConstraintViolationException


interface Validable<T> {
    fun validation(): Either<ValidationErrorsDTO, T>
}

data class ValidationError(val field: String, val message: String)

data class ValidationErrorsDTO(val errors: List<ValidationError>)

fun <T> withCustomValidator(obj: T, block: () -> Unit): Either<ValidationErrorsDTO, T> {
    try {
        block()
    } catch (ex: ConstraintViolationException) {
        val errors = ex.constraintViolations
            .map { ValidationError(it.property, it.constraint.name) }
        return Either.left(ValidationErrorsDTO(errors))
    }
    return Either.right(obj)
}