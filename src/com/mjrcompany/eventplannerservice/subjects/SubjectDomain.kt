package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.subjects

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.hasSize
import org.valiktor.validate
import java.util.*

sealed class SubjectDomain {
    data class Subject(
        val id: UUID,
        val name: String,
        val detail: String?,
        val createdBy: UUID,
        val imageUrl: String?
    ) : SubjectDomain()

    data class SubjectWritable(
        val name: String,
        val details: String?,
        val createdBy: UUID,
        val imageUrl: String? = null
    ) :
        Validatable<SubjectWritable>, SubjectDomain() {
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

    data class SubjectValidatable(
        val name: String,
        val details: String?,
        val imageUrl: String? = null
    ) :
        Validatable<SubjectValidatable>, SubjectDomain() {
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


}