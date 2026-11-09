package com.jianwang.portfolio.ledgerlite.data
import java.util.UUID

object SeedData {
    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.expenses().count() > 0) return
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        listOf(
            Expense(UUID.randomUUID().toString(), "Grocery run", 4850, "FOOD", "", now - day, now),
            Expense(UUID.randomUUID().toString(), "Metro card", 2200, "TRANSPORT", "", now - 2 * day, now),
            Expense(UUID.randomUUID().toString(), "Rent share", 85000, "HOUSING", "Nov", now - 3 * day, now),
            Expense(UUID.randomUUID().toString(), "Cinema", 1800, "ENTERTAINMENT", "", now - 4 * day, now),
            Expense(UUID.randomUUID().toString(), "Pharmacy", 1260, "HEALTH", "", now - 5 * day, now),
            Expense(UUID.randomUUID().toString(), "Headphones", 5999, "SHOPPING", "", now - 6 * day, now)
        ).forEach { db.expenses().upsert(it) }
    }
}
