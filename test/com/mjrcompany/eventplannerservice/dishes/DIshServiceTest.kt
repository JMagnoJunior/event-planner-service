package com.mjrcompany.eventplannerservice.dishes

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.DishWritable
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class DishServiceTest : RootTestDefinition() {

    @Test
    fun `it should get dish by id`() {

        val dishName = "test"
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID(), dishName)

        val result = DishService.getDishes(dishId)

        result.fold(
            { throw RuntimeException("Error while querying the dishes") },
            {

                when (it) {
                    is Some -> {
                        assertEquals(dishName, it.t.name)
                        assertEquals(dishId, it.t.id)
                    }

                    is None -> {
                        throw RuntimeException("Dish not found!")
                    }
                }
            }
        )

    }

    @Test
    fun `it should create a new dish`() {

        val dishName = "test"
        val dishDetails = "details"
        val dishDTO = DishWritable(dishName, dishDetails)

        val dishId = DishService.createDishes(dishDTO)
            .toOption()
            .getOrElse { throw RuntimeException("Error creating dishes") }

        val dish = TestDatabaseHelper.queryDishById(dishId)
        assertEquals(dish.name, dishName)
        assertEquals(dish.detail, dishDetails)
    }

    @Test
    fun `it should update a dish`() {

        val dishName = "test"
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID(), dishName)
        val updatedDetails = "update details"
        val dishDTO =
            DishWritable(dishName, updatedDetails)

        DishService.updateDish(dishId, dishDTO)

        val dish = TestDatabaseHelper.queryDishById(dishId)
        assertEquals(dish.name, dishName)
        assertEquals(dish.detail, updatedDetails)
    }

}

