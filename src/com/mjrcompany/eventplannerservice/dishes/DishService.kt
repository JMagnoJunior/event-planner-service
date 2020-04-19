package com.mjrcompany.eventplannerservice.dishes

import arrow.core.Either
import arrow.core.Option
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.database.withDatabaseErrorTreatment
import com.mjrcompany.eventplannerservice.core.CrudResource
import com.mjrcompany.eventplannerservice.core.ServiceResult
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.DishWritable
import org.slf4j.LoggerFactory
import java.util.*

object DishService {
    val log = LoggerFactory.getLogger(DishService::class.java)

    val createDishes = fun(dish: DishWritable): Either<ResponseErrorException, UUID> {
        log.info("Will create a dish: $dish")

        return Either.right(
            DishRepository.createDish(
                dish
            )
        )
    }

    val getDishes = fun(id: UUID): ServiceResult<Option<Dish>> {

        log.debug("Querying the dish: $id")
        val result = withDatabaseErrorTreatment {
            DishRepository.getDishById(id)
        }
        result.map { if (it.isEmpty()) log.info("dish not found") }
        return result

    }

    val updateDish = fun(id: UUID, dish: DishWritable): Either<ResponseErrorException, Unit> {
        log.info("Will update a dish: $dish")
        return Either.right(
            DishRepository.updateDish(
                id,
                dish
            )
        )
    }

    val crudResources = CrudResource(
        createDishes,
        updateDish,
        getDishes
    )
}