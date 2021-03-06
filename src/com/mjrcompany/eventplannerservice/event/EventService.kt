package com.mjrcompany.eventplannerservice.event

import arrow.core.Either
import arrow.core.Option
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.event.EventDomain
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.users.UserRepository
import org.slf4j.LoggerFactory
import java.util.*


object EventService {
    private val log = LoggerFactory.getLogger(EventService::class.java)

    val createEvent = fun(hostEmail: String, event: EventDomain.EventValidatable): ServiceResult<UUID> {
        log.info("Will create an event for host $hostEmail , event: $event")

        val result = withDatabaseErrorTreatment {

            UserRepository.getUserByEmail(hostEmail)
                .map {
                    EventDomain.EventWritable(
                        event.title,
                        it.id,
                        event.subject,
                        event.date,
                        event.address,
                        event.maxNumberGuest,
                        event.totalCost,
                        event.additionalInfo
                    )
                }
                .map { EventRepository.createEvent(it) }
                .getOrElse { throw NotFoundException("User not found: ${hostEmail}") }

        }

        if (result is Either.Left) {
            log.error("Error creating event $event error - ${result.a}")
        }

        return result
    }

    val updateEvent = fun(hostEmail: String, id: UUID, event: EventDomain.EventValidatable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            UserRepository.getUserByEmail(hostEmail)
                .map {
                    EventDomain.EventWritable(
                        event.title,
                        it.id,
                        event.subject,
                        event.date,
                        event.address,
                        event.maxNumberGuest,
                        event.totalCost,
                        event.additionalInfo
                    )
                }
                .map {
                    EventRepository.updateMeeting(
                        id,
                        it
                    )
                }
                .getOrElse { throw NotFoundException("Host not found: ${hostEmail}") }


        }

        if (result.isLeft()) {
            log.error("Error updating event $event")
        }

        return result
    }

    val getEvent = fun(id: UUID): ServiceResult<Option<EventDomain.Event>> {
        log.debug("Querying the event: $id")
        val result = withDatabaseErrorTreatment {
            EventRepository.getEventById(id)
        }

        result.map { if (it.isEmpty()) log.info("event not found") }
        return result
    }

    val getAllEventsFromUser = fun(hostId: UUID, pagination: Pagination): ServiceResult<Page<EventDomain.Event>> {
        val result = withDatabaseErrorTreatment {
            EventRepository.getAllEventsFromUser(hostId, pagination)
        }

        if (result.isLeft()) {
            log.error("Error listing all event")
        }

        return result
    }

    val subscribeEvent = fun(id: UUID, eventSubscriber: EventDomain.EventSubscriberWritable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            EventRepository.insertFriendInEvent(
                id,
                eventSubscriber
            )
        }
        if (result.isLeft()) {
            log.error("Error subscribing to event. event Id: $id , guest: $eventSubscriber")
        }

        return result

    }

    val acceptGuest =
        fun(id: UUID, acceptGuestInEventWritable: EventDomain.AcceptGuestInEventWritable): ServiceResult<Unit> {
            val result = withDatabaseErrorTreatment {
                EventRepository.updateGuestStatus(
                    id,
                    acceptGuestInEventWritable
                )
            }
            if (result.isLeft()) {
                log.error("Error update guest in event. event Id: $id , guest: acceptGuestInEventWritable$")
            }

            return result

        }

}


