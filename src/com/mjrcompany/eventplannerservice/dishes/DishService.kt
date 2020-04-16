package com.mjrcompany.eventplannerservice.dishes

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.DishWritable
import com.mjrcompany.eventplannerservice.NotFoundException
import com.mjrcompany.eventplannerservice.ResponseErrorException
import com.mjrcompany.eventplannerservice.core.CrudResource
import java.util.*

object DishService {
    val createDishes = fun(dish: DishWritable): Either<ResponseErrorException, UUID> {
        return Either.right(
            DishRepository.createDish(
                dish
            )
        )
    }

    val getDishes = fun(id: UUID): Either<ResponseErrorException, Dish> {
        return when (val result =
            DishRepository.getDishById(id)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("Dish not Found!"))
        }
    }

    val updateDish = fun(id: UUID, dish: DishWritable): Either<ResponseErrorException, Unit> {
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