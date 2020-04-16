package com.mjrcompany.eventplannerservice.dish

import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.DishWriterDTO
import com.mjrcompany.eventplannerservice.dishes.DishService
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class DishServiceTest : RootTestDefinition() {

    @Test
    fun `it should get dish by id`() {

        val dishName = "test"
        val dishId = TestDatabaseHelper.addDish(UUID.randomUUID(), dishName)

        val result: Dish = DishService.getDishes(dishId)
            .getOrElse { throw RuntimeException("Error getting com.mjrcompany.eventplannerservice.dish") }

        assertEquals(dishName, result.name)
        assertEquals(dishId, result.id)
    }

    @Test
    fun `it should create a new dish`() {

        val dishName = "test"
        val dishDetails = "com.mjrcompany.eventplannerservice.dish details"
        val dishDTO = DishWriterDTO(dishName, dishDetails)

        val dishId = DishService.createDishes(dishDTO)
            .toOption()
            .getOrElse { throw RuntimeException("Error creating com.mjrcompany.eventplannerservice.dish") }

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
            DishWriterDTO(dishName, updatedDetails)

        DishService.updateDish(dishId, dishDTO)

        val dish = TestDatabaseHelper.queryDishById(dishId)
        assertEquals(dish.name, dishName)
        assertEquals(dish.detail, updatedDetails)
    }

}

