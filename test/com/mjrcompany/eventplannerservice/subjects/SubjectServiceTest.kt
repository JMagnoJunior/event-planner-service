package com.mjrcompany.eventplannerservice.subjects

import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse
import com.mjrcompany.eventplannerservice.RootTestDefinition
import com.mjrcompany.eventplannerservice.TestDatabaseHelper
import com.mjrcompany.eventplannerservice.domain.SubjectWritable
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals


class SubjectServiceTest : RootTestDefinition() {

//    @Test
//    fun `it should get dish by id`() {
//
//        val dishName = "test"
//        val dishId = TestDatabaseHelper.generateSubject(UUID.randomUUID(), dishName)
//
//        val result = SubjectService.getSubject(dishId)
//
//        result.fold(
//            { throw RuntimeException("Error while querying the subjects") },
//            {
//
//                when (it) {
//                    is Some -> {
//                        assertEquals(dishName, it.t.name)
//                        assertEquals(dishId, it.t.id)
//                    }
//
//                    is None -> {
//                        throw RuntimeException("Dish not found!")
//                    }
//                }
//            }
//        )
//
//    }
//
//    @Test
//    fun `it should create a new dish`() {
//
//        val dishName = "test"
//        val dishDetails = "details"
//        val dishDTO = SubjectWritable(dishName, dishDetails)
//
//        val dishId = SubjectService.createSubject(dishDTO)
//            .toOption()
//            .getOrElse { throw RuntimeException("Error creating subjects") }
//
//        val dish = TestDatabaseHelper.querySubjectById(dishId)
//        assertEquals(dish.name, dishName)
//        assertEquals(dish.detail, dishDetails)
//    }
//
//    @Test
//    fun `it should update a dish`() {
//
//        val dishName = "test"
//        val dishId = TestDatabaseHelper.generateSubject(UUID.randomUUID(), dishName)
//        val updatedDetails = "update details"
//        val dishDTO =
//            SubjectWritable(dishName, updatedDetails)
//
//        SubjectService.updateSubject(dishId, dishDTO)
//
//        val dish = TestDatabaseHelper.querySubjectById(dishId)
//        assertEquals(dish.name, dishName)
//        assertEquals(dish.detail, updatedDetails)
//    }

}

