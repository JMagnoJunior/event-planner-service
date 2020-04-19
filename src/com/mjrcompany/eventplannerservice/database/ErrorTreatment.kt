package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database

import arrow.core.Either
import arrow.core.Option
import com.mjrcompany.eventplannerservice.DatabaseAccessException
import com.mjrcompany.eventplannerservice.core.ServiceResult

fun <T> withDatabaseErrorTreatment(block: () -> T): ServiceResult<T> {
    return try {
        Either.right(
            block()
        )
    } catch (e: Exception) {
        Either.left(
            DatabaseAccessException(
                e.message ?: "", e.toString()
            )
        )
    }
}