package com.magnojr.foodwithfriends.commons

import com.google.gson.Gson
import com.magnojr.foodwithfriends.RootTestDefinition
import com.magnojr.foodwithfriends.module
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class UserRoutesTest : RootTestDefinition() {
    @Test
    fun `should create users`() {

        val dishName = "test"
        val dishId = addDish(UUID.randomUUID(), dishName)

        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/dishes/$dishId").apply {

                assertEquals(HttpStatusCode.OK, response.status())

                val dish = Gson().fromJson(response.content, Dish::class.java)
                assertEquals(dishId, dish.id)
                assertEquals(dishName, dish.name)
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