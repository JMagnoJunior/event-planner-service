package com.mjrcompany.eventplannerservice.dishes

import arrow.core.Option
import arrow.core.firstOrNone
import com.mjrcompany.eventplannerservice.database.DataMapper
import com.mjrcompany.eventplannerservice.domain.Dish
import com.mjrcompany.eventplannerservice.domain.DishWritable
import com.mjrcompany.eventplannerservice.database.Dishes
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.*


object DishRepository {

    fun createDish(dishDTO: DishWritable): UUID {
        lateinit var dishId: UUID
        transaction {
            dishId = Dishes.insert {
                writeAttributes(
                    it,
                    UUID.randomUUID(),
                    dishDTO
                )
            } get Dishes.id
        }
        return dishId
    }

    fun updateDish(id: UUID, dishDTO: DishWritable) {
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

    private fun writeAttributes(it: UpdateBuilder<Any>, dishDTO: DishWritable) {
        it[Dishes.name] = dishDTO.name
        it[Dishes.details] = dishDTO.details
    }

    private fun writeAttributes(it: UpdateBuilder<Any>, id: UUID, dishDTO: DishWritable) {
        it[Dishes.id] = id
        writeAttributes(it, dishDTO)
    }

}


