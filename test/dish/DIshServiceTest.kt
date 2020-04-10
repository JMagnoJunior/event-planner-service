package com.magnojr.foodwithfriends.dish

import com.magnojr.foodwithfriends.RootTestDefinition
import com.magnojr.foodwithfriends.commons.Dish
import com.magnojr.foodwithfriends.commons.DishWriterDTO
import com.magnojr.foodwithfriends.commons.Dishes
import com.magnojr.foodwithfriends.dishes.DishService

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class DishServiceTest : RootTestDefinition() {

    @Test
    fun `it should get dish by id`() {

        val dishName = "test"
        val dishId = addDish(UUID.randomUUID(), dishName)

        val result: Dish = DishService.getDishes(dishId)

        assertEquals(dishName, result.name)
        assertEquals(dishId, result.id)
    }

    @Test
    fun `it should create a new dish`() {

        val dishDTO = DishWriterDTO("test", "dish details")

        val dishId = DishService.createDishes(dishDTO)

        transaction {
            Dishes
                .select { Dishes.id eq dishId }
                .map {
                    assertEquals(it[Dishes.name], dishDTO.name)
                    assertEquals(it[Dishes.details], dishDTO.details)
                }
        }
    }

    @Test
    fun `it should update a dish`() {

        val dishName = "test"
        val dishId = addDish(UUID.randomUUID(), dishName)
        val updatedDetails = "update details"
        val dishDTO = DishWriterDTO(dishName, updatedDetails)

        DishService.updateDish(dishId, dishDTO)

        transaction {
            Dishes
                .select { Dishes.id eq dishId }
                .map {
                    assertEquals(it[Dishes.name], dishDTO.name)
                    assertEquals(it[Dishes.details], dishDTO.details)
                }
        }


    }
}


fun addDish(uuid: UUID, dishName: String): UUID {
    transaction {
        Dishes.insert {
            it[id] = uuid
            it[name] = dishName
        }
    }
    return uuid
}