package com.jianwang.portfolio.ledgerlite.domain

import java.util.Locale

data class LedgerLine(
    val id: String,
    val title: String,
    val amountCents: Long,
    val category: String,
    val note: String,
    val spentAtMillis: Long
)

data class CategoryTotal(
    val category: String,
    val totalCents: Long
)

object LedgerAnalytics {
    fun filter(lines: List<LedgerLine>, query: String, category: String?): List<LedgerLine> {
        return lines.filter { line ->
            val matchesQuery = query.isBlank() ||
                line.title.contains(query, ignoreCase = true) ||
                line.note.contains(query, ignoreCase = true) ||
                line.category.contains(query, ignoreCase = true)
            val matchesCategory = category == null || line.category == category
            matchesQuery && matchesCategory
        }
    }

    fun totalCents(lines: List<LedgerLine>): Long = lines.sumOf { it.amountCents }

    fun categoryTotals(lines: List<LedgerLine>): List<CategoryTotal> {
        return lines.groupBy { it.category }
            .map { (name, items) -> CategoryTotal(name, items.sumOf { it.amountCents }) }
            .sortedByDescending { it.totalCents }
    }
}

object LedgerCsv {
    fun render(lines: List<LedgerLine>, formatSpentAt: (Long) -> String): String {
        val header = "id,title,amount,category,note,spent_at"
        val rows = lines.map { line ->
            val amount = "%.2f".format(Locale.US, line.amountCents / 100.0)
            val title = line.title.replace(",", " ")
            val note = line.note.replace(",", " ")
            "${line.id},$title,$amount,${line.category},$note,${formatSpentAt(line.spentAtMillis)}"
        }
        return (listOf(header) + rows).joinToString(separator = "\n") + "\n"
    }
}
