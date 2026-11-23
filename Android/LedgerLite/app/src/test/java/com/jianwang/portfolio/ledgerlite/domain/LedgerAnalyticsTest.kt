package com.jianwang.portfolio.ledgerlite.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LedgerAnalyticsTest {
    @Test
    fun blankSearchKeepsEveryExpense() {
        val lines = listOf(
            line("1", "Coffee", 300, "FOOD"),
            line("2", "Rent", 1000, "HOUSING", note = "November")
        )
        assertEquals(lines, LedgerAnalytics.filter(lines, "   ", null))
        assertEquals(1300L, LedgerAnalytics.totalCents(lines))
    }

    @Test
    fun categoryFilterThenCaseInsensitiveSearch() {
        val lines = listOf(
            line("food", "Coffee", 300, "FOOD"),
            line("note", "Taxi", 900, "TRANSPORT", note = "Airport"),
            line("other", "Rent", 10_000, "HOUSING")
        )
        assertEquals(listOf("food"), LedgerAnalytics.filter(lines, "COFF", "FOOD").map { it.id })
        assertEquals(listOf("note"), LedgerAnalytics.filter(lines, "air", null).map { it.id })
        assertEquals(listOf("other"), LedgerAnalytics.filter(lines, "housing", null).map { it.id })
        assertEquals(emptyList<String>(), LedgerAnalytics.filter(lines, "coffee", "HOUSING").map { it.id })
    }

    @Test
    fun categoryTotalsSortDescending() {
        val totals = LedgerAnalytics.categoryTotals(
            listOf(
                line("1", "A", 500, "FOOD"),
                line("2", "B", 100, "FOOD"),
                line("3", "C", 2_000, "HOUSING")
            )
        )
        assertEquals(listOf("HOUSING", "FOOD"), totals.map { it.category })
        assertEquals(listOf(2_000L, 600L), totals.map { it.totalCents })
    }

    @Test
    fun csvReplacesCommasAndKeepsRowOrder() {
        val csv = LedgerCsv.render(
            listOf(
                line("1", "Coffee, tea", 250, "FOOD", note = "a,b"),
                line("2", "Rent", 10, "HOUSING")
            )
        ) { "2026-11-16 09:00" }

        assertEquals(
            "id,title,amount,category,note,spent_at\n" +
                "1,Coffee  tea,2.50,FOOD,a b,2026-11-16 09:00\n" +
                "2,Rent,0.10,HOUSING,,2026-11-16 09:00\n",
            csv
        )
    }

    @Test
    fun emptyCsvIsHeaderOnly() {
        assertEquals(
            "id,title,amount,category,note,spent_at\n",
            LedgerCsv.render(emptyList()) { "" }
        )
    }

    private fun line(
        id: String,
        title: String,
        cents: Long,
        category: String,
        note: String = ""
    ) = LedgerLine(id, title, cents, category, note, 0L)
}
