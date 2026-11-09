package com.jianwang.portfolio.ledgerlite.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jianwang.portfolio.ledgerlite.LedgerLiteApp
import com.jianwang.portfolio.ledgerlite.data.Expense
import com.jianwang.portfolio.ledgerlite.data.ExpenseCategory
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FormState(
    val title: String = "",
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val note: String = "",
    val error: String? = null
)

class LedgerViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as LedgerLiteApp).database.expenses()
    private val query = MutableStateFlow("")
    private val categoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val searchQuery = query.asStateFlow()
    val selectedCategory = categoryFilter.asStateFlow()

    val expenses = combine(dao.observeAll(), query, categoryFilter) { all, q, cat ->
        all.filter { e ->
            val matchesQ = q.isBlank() || e.title.contains(q, true) || e.note.contains(q, true) || e.category.contains(q, true)
            val matchesC = cat == null || e.category == cat.name
            matchesQ && matchesC
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form.asStateFlow()

    fun setQuery(q: String) { query.value = q }
    fun setCategoryFilter(c: ExpenseCategory?) { categoryFilter.value = c }

    fun resetForm() { _form.value = FormState() }
    fun updateTitle(v: String) { _form.value = _form.value.copy(title = v, error = null) }
    fun updateAmount(v: String) { _form.value = _form.value.copy(amount = v.filter { it.isDigit() || it == '.' }, error = null) }
    fun updateCategory(c: ExpenseCategory) { _form.value = _form.value.copy(category = c) }
    fun updateNote(v: String) { _form.value = _form.value.copy(note = v) }

    fun save(onDone: () -> Unit) {
        val f = _form.value
        val amount = f.amount.toDoubleOrNull()
        when {
            f.title.isBlank() -> _form.value = f.copy(error = "Title is required")
            amount == null || amount <= 0 -> _form.value = f.copy(error = "Enter a valid amount")
            else -> viewModelScope.launch {
                val now = System.currentTimeMillis()
                dao.upsert(
                    Expense(
                        id = UUID.randomUUID().toString(),
                        title = f.title.trim(),
                        amountCents = (amount * 100).toLong(),
                        category = f.category.name,
                        note = f.note.trim(),
                        spentAt = now,
                        createdAt = now
                    )
                )
                resetForm()
                onDone()
            }
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { dao.delete(expense) }
    }
}
