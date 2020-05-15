package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.tasks

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.validate
import java.util.*


sealed class TaskDomain {
    data class TaskWritable(val details: String) :
        Validatable<TaskWritable>, TaskDomain() {
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


    data class Task(
        val id: Int,
        val details: String,
        val eventId: UUID,
        val owner: UUID?
    ) : TaskDomain()

    data class TaskOwnerWritable(val friendId: UUID) :
        Validatable<TaskOwnerWritable>, TaskDomain() {
        override fun validation(): Either<ValidationErrorsDTO, TaskOwnerWritable> {
            return withCustomValidator(this)
        }
    }


}