package com.jianwang.portfolio.ledgerlite.data

import com.jianwang.portfolio.ledgerlite.domain.LedgerLine

fun Expense.toLedgerLine() = LedgerLine(
    id = id,
    title = title,
    amountCents = amountCents,
    category = category,
    note = note,
    spentAtMillis = spentAt
)
