package com.mjrcompany.eventplannerservice.subjects

import arrow.core.Either
import arrow.core.Option
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import org.slf4j.LoggerFactory
import java.util.*

object SubjectService {
    val log = LoggerFactory.getLogger(SubjectService::class.java)

    val createSubject = fun(subject: SubjectWritable): Either<ResponseErrorException, UUID> {
        log.info("Will create a subject: $subject")

        val result = withDatabaseErrorTreatment {
            SubjectRepository.createSubject(
                subject
            )
        }

        if (result.isLeft()) {
            log.error("error creating task: $subject")
        }

        return result
    }

    val getSubject = fun(id: UUID): ServiceResult<Option<Subject>> {

        log.debug("Querying the subject: $id")
        val result = withDatabaseErrorTreatment {
            SubjectRepository.getDishById(id)
        }
        result.map { if (it.isEmpty()) log.info("dish not found") }
        return result

    }

    val updateSubject = fun(id: UUID, subject: SubjectWritable): Either<ResponseErrorException, Unit> {
        log.info("Will update a subject: $subject")
        return Either.right(
            SubjectRepository.updateDish(
                id,
                subject
            )
        )
    }

    val crudResources = CrudResource(
        createSubject,
        updateSubject,
        getSubject
    )
}