package com.magnojr.foodwithfriends.dishes

import arrow.core.Option
import arrow.core.firstOrNone
import com.magnojr.foodwithfriends.commons.DataMapper
import com.magnojr.foodwithfriends.commons.Dish
import com.magnojr.foodwithfriends.commons.DishWriterDTO
import com.magnojr.foodwithfriends.commons.Dishes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


object DishRepository {

    fun createDish(dishDTO: DishWriterDTO): UUID {
        lateinit var dishId: UUID
        transaction {
            dishId = Dishes.insert {
                writeAttributes(it, UUID.randomUUID(), dishDTO)
            } get Dishes.id
        }
        return dishId
    }

    fun updateDish(id: UUID, dishDTO: DishWriterDTO) {
        transaction {
            Dishes.update({ Dishes.id eq id }) {
                writeAttributes(it, dishDTO)
            }
        }
    }

    fun getDishById(id: UUID): Option<Dish> {
        lateinit var result: Option<Dish>
        transaction {
            result = Dishes
                .select { Dishes.id eq id }
                .map {
                    DataMapper.mapToDish(it)
                }
                .firstOrNone()
        }
        return result
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, dishDTO: DishWriterDTO) {
        it[Dishes.name] = dishDTO.name
        it[Dishes.details] = dishDTO.details
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, dishDTO: DishWriterDTO) {
        it[Dishes.id] = id
        writeAttributes(it, dishDTO)
    }

}


