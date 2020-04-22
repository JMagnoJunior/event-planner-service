package com.mjrcompany.eventplannerservice.com.mjrcompany.eventplannerservice.core

import arrow.core.Option
import com.mjrcompany.eventplannerservice.database.Events
import com.mjrcompany.eventplannerservice.database.Tasks
import com.mjrcompany.eventplannerservice.database.Users
import io.ktor.application.ApplicationCall
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import kotlin.math.ceil

interface Sortable {
    val type: Expression<*>
}

enum class UsersOrderBy(override val type: Expression<*>) : Sortable {
    Name(Users.name),
    Email(Users.email);

    companion object {
        const val fieldDefault = "Name"
        const val sortOrderDefault = "ASC"
        val orderBy: (ApplicationCall) -> Option<Pair<Sortable, SortOrder>> = {
            Option.just(
                Pair(
                    (valueOf(it.parameters["orderBy"] ?: fieldDefault) as Sortable),
                    (SortOrder.valueOf(it.parameters["sortBy"] ?: sortOrderDefault))
                )
            )
        }

    }

}

enum class EventOrderBy(override val type: Expression<*>) : Sortable {
    CreateDate(Events.createDate),
    Date(Events.date);

    companion object {
        const val fieldDefault = "CreateDate"
        const val sortOrderDefault = "ASC"
        val orderBy: (ApplicationCall) -> Option<Pair<Sortable, SortOrder>> = {
            Option.just(
                Pair(
                    (valueOf(it.parameters["orderBy"] ?: fieldDefault) as Sortable),
                    (SortOrder.valueOf(it.parameters["sortBy"] ?: sortOrderDefault))
                )
            )
        }
    }
}

enum class TaskOrderBy(override val type: Expression<*>) : Sortable {
    Id(Tasks.id);

    companion object {
        const val fieldDefault = "Id"
        const val sortOrderDefault = "ASC"
        val orderBy: (ApplicationCall) -> Option<Pair<Sortable, SortOrder>> = {
            Option.just(
                Pair(
                    (valueOf(it.parameters["orderBy"] ?: fieldDefault) as Sortable),
                    (SortOrder.valueOf(it.parameters["sortBy"] ?: sortOrderDefault))
                )
            )
        }
    }
}


data class OrderBy(val field: Sortable, val sortOrder: SortOrder)

data class Pagination(val page: Long, val totalItems: Int, val orderBy: OrderBy)

data class Page<T>(val items: List<T>, val currentPage: Long, val totalPages: Long)

const val PAGE_DEFAULT = 1L
const val TOTAL_ITEMS_DEFAULT = 10

fun <T> Query.withPagination(
    pagination: Pagination,
    block: (ResultRow) -> T
): Page<T> {
    val (page, totalItems) = pagination
    val count = this.count()
    val totalPages = ceil(count.toDouble() / totalItems).toLong()
    val offset = totalItems * (page - 1)
    val (field, sortOrder) = pagination.orderBy

    val t = this
        .orderBy(field.type, order = sortOrder)
        .limit(totalItems, offset = offset)
        .map {
            block(it)
        }

    return Page(t, page, totalPages)
}