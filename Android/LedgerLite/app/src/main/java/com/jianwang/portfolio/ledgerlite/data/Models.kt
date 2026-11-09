package com.jianwang.portfolio.ledgerlite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExpenseCategory(val label: String) {
    FOOD("Food"),
    TRANSPORT("Transport"),
    HOUSING("Housing"),
    ENTERTAINMENT("Entertainment"),
    HEALTH("Health"),
    SHOPPING("Shopping"),
    OTHER("Other")
}

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey val id: String,
    val title: String,
    val amountCents: Long,
    val category: String,
    val note: String,
    val spentAt: Long,
    val createdAt: Long
)
