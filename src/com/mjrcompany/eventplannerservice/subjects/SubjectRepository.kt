package com.mjrcompany.eventplannerservice.subjects

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.withPagination
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.domain.Subject
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import com.mjrcompany.eventplannerservice.database.Subjects
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


object SubjectRepository {

    fun createSubject(subjectDTO: SubjectWritable): UUID {
        return transaction {
            Subjects.insert {
                writeAttributes(
                    it,
                    UUID.randomUUID(),
                    subjectDTO
                )
            } get Subjects.id
        }
    }

    fun updateSubject(id: UUID, subjectDTO: SubjectWritable) {
        transaction {
            Subjects.update({ Subjects.id eq id }) {
                writeAttributes(it, subjectDTO)
            }
        }
    }

    fun getAll(userId: UUID, pagination: Pagination): Page<Subject> {
        return transaction {
            Subjects.select { Subjects.createdBy eq userId }
                .withPagination(pagination) {
                    DataMapper.mapToSubject(it)
                }
        }
    }


    fun getSubjectById(id: UUID): Option<Subject> {
        lateinit var result: Option<Subject>
        transaction {
            result = Subjects
                .select { Subjects.id eq id }
                .map {
                    DataMapper.mapToSubject(it)
                }
                .firstOrNone()
        }
        return result
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, subjectDTO: SubjectWritable) {
        it[Subjects.name] = subjectDTO.name
        it[Subjects.details] = subjectDTO.details
        it[Subjects.imageUrl] = subjectDTO.imageUrl
        it[Subjects.createdBy] = subjectDTO.createdBy
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, subjectDTO: SubjectWritable) {
        it[Subjects.id] = id
        writeAttributes(it, subjectDTO)
    }

}


