package com.mjrcompany.eventplannerservice.domain

import arrow.core.Either
import com.mjrcompany.eventplannerservice.core.Validatable
import com.mjrcompany.eventplannerservice.core.ValidationErrorsDTO
import com.mjrcompany.eventplannerservice.core.withCustomValidator
import org.valiktor.functions.*
import org.valiktor.validate
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*


/*
This module contains classes  which can be used to write on the database.
The classes on this module which implements Validatable interface can also be received as a parameter from request.
 */


//sealed class SubjectDomain {
//    data class Subject(
//        val id: UUID,
//        val name: String,
//        val detail: String?,
//        val createdBy: UUID,
//        val imageUrl: String?
//    ) : SubjectDomain()
//
//    data class SubjectWritable(
//        val name: String,
//        val details: String?,
//        val createdBy: UUID,
//        val imageUrl: String? = null
//    ) :
//        Validatable<SubjectWritable>, SubjectDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, SubjectWritable> {
//            return withCustomValidator(this) {
//                validate(this) {
//                    validate(SubjectWritable::name).hasSize(
//                        min = 3,
//                        max = 100
//                    )
//                }
//            }
//        }
//    }
//
//    data class SubjectValidatable(
//        val name: String,
//        val details: String?,
//        val imageUrl: String? = null
//    ) :
//        Validatable<SubjectValidatable>, SubjectDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, SubjectValidatable> {
//            return withCustomValidator(this) {
//                validate(this) {
//                    validate(SubjectValidatable::name).hasSize(
//                        min = 3,
//                        max = 100
//                    )
//                }
//            }
//        }
//    }
//
//
//}


//sealed class UserDomain {
//    data class User(val id: UUID, val name: String, val email: String) : UserDomain()
//
//    data class UserWritable(val name: String, val email: String) :
//        Validatable<UserWritable>, UserDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, UserWritable> {
//            return withCustomValidator(this) {
//                validate(this) {
//                    validate(UserWritable::name).hasSize(
//                        min = 3,
//                        max = 100
//                    )
//                    validate(UserWritable::email).isEmail()
//                }
//            }
//        }
//    }
//
//    data class GuestInEvent(val id: UUID, val name: String, val email: String, val status: UserInEventStatus) :
//        UserDomain()
//}

//
//data class UserWritable(val name: String, val email: String) :
//    Validatable<UserWritable>, UserDomain() {
//    override fun validation(): Either<ValidationErrorsDTO, UserWritable> {
//        return withCustomValidator(this) {
//            validate(this) {
//                validate(UserWritable::name).hasSize(
//                    min = 3,
//                    max = 100
//                )
//                validate(UserWritable::email).isEmail()
//            }
//        }
//    }
//}

//
//sealed class TaskDomain {
//    data class TaskWritable(val details: String) :
//        Validatable<TaskWritable>, TaskDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, TaskWritable> {
//            return withCustomValidator(this) {
//                validate(this) {
//                    validate(TaskWritable::details).hasSize(
//                        min = 3
//                    )
//                }
//            }
//        }
//    }
//
//
//    data class Task(
//        val id: Int,
//        val details: String,
//        val eventId: UUID,
//        val owner: UUID?
//    ) : TaskDomain()
//
//    data class TaskOwnerWritable(val friendId: UUID) :
//        Validatable<TaskOwnerWritable>, TaskDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, TaskOwnerWritable> {
//            return withCustomValidator(this)
//        }
//    }
//
//
//}


//data class TaskWritable(val details: String) :
//    Validatable<TaskWritable> {
//    override fun validation(): Either<ValidationErrorsDTO, TaskWritable> {
//        return withCustomValidator(this) {
//            validate(this) {
//                validate(TaskWritable::details).hasSize(
//                    min = 3
//                )
//            }
//        }
//    }
//}


//sealed class EventDomain {
//    data class EventValidatable(
//        val title: String,
//        val subject: UUID,
//        val date: LocalDateTime,
//        val address: String,
//        val maxNumberGuest: Int,
//        val totalCost: BigDecimal,
//        val additionalInfo: String?
//    ) : Validatable<EventValidatable>, EventDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, EventValidatable> {
//            return withCustomValidator(this) {
//                validate(this) {
//                    validate(EventValidatable::title).hasSize(
//                        min = 3,
//                        max = 100
//                    )
//                    validate(EventValidatable::maxNumberGuest).isPositiveOrZero()
//                    validate(EventValidatable::totalCost).isLessThan(BigDecimal.valueOf(1000000.00)).isPositive()
//                }
//            }
//        }
//    }
//
//    data class Event(
//        val id: UUID,
//        val title: String,
//        val host: UserDomain.User,
//        val subject: SubjectDomain.Subject,
//        val date: LocalDateTime,
//        val createDate: LocalDateTime,
//        val address: String,
//        val maxNumberGuest: Int,
//        val tasks: List<TaskDomain.Task> = emptyList(),
//        val guestInEvents: List<UserDomain.GuestInEvent> = emptyList(),
//        val totalCost: BigDecimal,
//        val additionalInfo: String?,
//        val eventStatus: EventStatus,
//        val pricePerGuest: Number = {
//            val format = NumberFormat.getInstance()
//
//            if (guestInEvents.isNotEmpty()) {
//                format.parse((totalCost / BigDecimal(guestInEvents.size)).toString())
//            } else {
//                format.parse(totalCost.toString())
//            }
//        }()
//
//    )
//
//    data class EventSubscriberWritable(val guestId: UUID) :
//        Validatable<EventSubscriberWritable>, EventDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, EventSubscriberWritable> {
//            return withCustomValidator(this)
//        }
//    }
//
//    data class AcceptGuestInEventWritable(val guestId: UUID, val status: UserInEventStatus) :
//        Validatable<AcceptGuestInEventWritable>, EventDomain() {
//        override fun validation(): Either<ValidationErrorsDTO, AcceptGuestInEventWritable> {
//            return withCustomValidator(this)
//        }
//    }
//
//    data class EventWritable(
//        val title: String,
//        val host: UUID,
//        val subject: UUID,
//        val date: LocalDateTime,
//        val address: String,
//        val maxNumberGuest: Int,
//        val totalCost: BigDecimal,
//        val additionalInfo: String?
//    ) : EventDomain()
//
//
//}
//
//enum class EventStatus(val status: String) {
//    Open("open"),
//    Close("close")
//}
//
//enum class UserInEventStatus(val status: String) {
//    Pending("pending"),
//    Accept("Accept"),
//    Reject("Reject")
//}
