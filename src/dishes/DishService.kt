package com.magnojr.foodwithfriends.dishes

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import com.magnojr.foodwithfriends.commons.Dish
import com.magnojr.foodwithfriends.commons.DishWriterDTO
import com.magnojr.foodwithfriends.commons.NotFoundException
import com.magnojr.foodwithfriends.commons.ResponseErrorException
import com.magnojr.foodwithfriends.core.CrudResource
import java.util.*

object DishService {
    val createDishes = fun(dishDTO: DishWriterDTO): Either<ResponseErrorException, UUID> {
        return Either.right(DishRepository.createDish(dishDTO))
    }

    val getDishes = fun(id: UUID): Either<ResponseErrorException, Dish> {
        return when (val result = DishRepository.getDishById(id)) {
            is Some -> Either.right(result.t)
            is None -> Either.left(NotFoundException("Dish not Found!"))
        }
    }

    val updateDish = fun(id: UUID, dishDTO: DishWriterDTO): Either<ResponseErrorException, Unit> {
        return Either.right(DishRepository.updateDish(id, dishDTO))
    }

    val crudResources = CrudResource(createDishes, updateDish, getDishes)
}