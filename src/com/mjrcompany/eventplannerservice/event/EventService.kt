package com.mjrcompany.eventplannerservice.event

import arrow.core.Option
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Event
import com.mjrcompany.eventplannerservice.domain.EventSubscriberWritable
import com.mjrcompany.eventplannerservice.domain.EventWritable
import org.slf4j.LoggerFactory
import java.util.*


object EventService {
    private val log = LoggerFactory.getLogger(EventService::class.java)

    val createEvent = fun(event: EventWritable): ServiceResult<UUID> {
        val result = withDatabaseErrorTreatment {
            EventRepository.createEvent(
                event
            )
        }

        if (result.isLeft()) {
            log.error("Error creating event $event")
        }

        return result
    }

    val updateEvent = fun(id: UUID, event: EventWritable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            EventRepository.updateMeeting(
                id,
                event
            )
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

    val subscribeEvent = fun(id: UUID, eventSubscriber: EventSubscriberWritable): ServiceResult<Unit> {
        val result = withDatabaseErrorTreatment {
            EventRepository.insertFriendInEvent(
                id,
                eventSubscriber
            )
        }
        if (result.isLeft()) {
            log.error("Error subscribing to event. event Id: $id , friend: $eventSubscriber")
        }

        return result

    }

    val crudResources = CrudResource(
        createEvent,
        updateEvent,
        getEvent
    )

}


