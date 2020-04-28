package com.mjrcompany.eventplannerservice.event

import arrow.core.Option
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Page
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core.Pagination
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.domain.EventDTO
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.*
import com.mjrcompany.eventplannerservice.users.UserRepository
import org.slf4j.LoggerFactory
import java.util.*


object EventService {
    private val log = LoggerFactory.getLogger(EventService::class.java)

    val createEvent = fun(event: EventDTO): ServiceResult<UUID> {
        val result = withDatabaseErrorTreatment {

            UserRepository.getUserByEmail(event.host)
                .map {
                    EventWritable(
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
                .getOrElse { throw NotFoundException("Host not found: ${event.host}") }

        }

        if (result.isLeft()) {
            log.error("Error creating event $event")
        }

        return result
    }

    val updateEvent = fun(id: UUID, event: EventDTO): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            UserRepository.getUserByEmail(event.host)
                .map {
                    EventWritable(
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
                .getOrElse { throw NotFoundException("Host not found: ${event.host}") }


        }

        if (result.isLeft()) {
            log.error("Error updating event $event")
        }

        return result
    }

    val getEvent = fun(id: UUID): ServiceResult<Option<Event>> {
        log.debug("Querying the event: $id")
        val result = withDatabaseErrorTreatment {
            EventRepository.getEventById(id)
        }

        result.map { if (it.isEmpty()) log.info("event not found") }
        return result
    }

    val getAllEvents = fun(pagination: Pagination): ServiceResult<Page<Event>> {
        val result = withDatabaseErrorTreatment {
            EventRepository.getAllEvents(pagination)
        }

        if (result.isLeft()) {
            log.error("Error listing all event")
        }

        return result
    }

    val subscribeEvent = fun(id: UUID, eventSubscriber: EventSubscriberWritable): ServiceResult<Unit> {
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

    val acceptGuest = fun(id: UUID, acceptGuestInEventWritable: AcceptGuestInEventWritable): ServiceResult<Unit> {
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

    val crudResources = CrudResource(
        createEvent,
        updateEvent,
        getEvent,
        getAllEvents
    )

}


