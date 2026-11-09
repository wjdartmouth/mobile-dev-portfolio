package com.jianwang.portfolio.ledgerlite.ui

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jianwang.portfolio.ledgerlite.data.CsvExporter
import com.jianwang.portfolio.ledgerlite.data.Expense
import com.jianwang.portfolio.ledgerlite.data.ExpenseCategory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

private val categoryColors = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF9C27B0),
    Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF00BCD4), Color(0xFF795548)
)

@Composable
fun LedgerRoot(vm: LedgerViewModel = viewModel()) {
    val nav = rememberNavController()
    val expenses by vm.expenses.collectAsState()
    val query by vm.searchQuery.collectAsState()
    val cat by vm.selectedCategory.collectAsState()
    val context = LocalContext.current

    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                expenses, query, cat,
                onQuery = vm::setQuery,
                onCategory = vm::setCategoryFilter,
                onDelete = vm::delete,
                onAdd = { vm.resetForm(); nav.navigate("add") },
                onShare = { CsvExporter.share(context, expenses) }
            )
        }
        composable("add") {
            AddExpenseScreen(vm, onBack = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    expenses: List<Expense>,
    query: String,
    category: ExpenseCategory?,
    onQuery: (String) -> Unit,
    onCategory: (ExpenseCategory?) -> Unit,
    onDelete: (Expense) -> Unit,
    onAdd: () -> Unit,
    onShare: () -> Unit
) {
    val total = expenses.sumOf { it.amountCents }
    val byCat = remember(expenses) {
        expenses.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amountCents } }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger Lite") },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Export CSV")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add expense")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query, onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search expenses") }, singleLine = true
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = category == null, onClick = { onCategory(null) }, label = { Text("All") })
                }
                items(ExpenseCategory.entries) { c ->
                    FilterChip(selected = category == c, onClick = { onCategory(c) }, label = { Text(c.label) })
                }
            }
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    DonutChart(byCat, Modifier.size(96.dp))
                    Spacer(Modifier.size(16.dp))
                    Column {
                        Text("Total", style = MaterialTheme.typography.labelMedium)
                        Text(formatMoney(total), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            modifier = Modifier.semantics { contentDescription = "Total spending ${formatMoney(total)}" })
                        Text("${expenses.size} expenses", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (expenses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No expenses match. Add one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expenses, key = { it.id }) { e -> ExpenseRow(e, onDelete) }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(byCat: Map<String, Long>, modifier: Modifier) {
    val total = byCat.values.sum().coerceAtLeast(1)
    val entries = ExpenseCategory.entries
    Canvas(modifier.semantics { contentDescription = "Spending by category chart" }) {
        var start = -90f
        entries.forEachIndexed { idx, cat ->
            val value = byCat[cat.name] ?: 0L
            if (value == 0L) return@forEachIndexed
            val sweep = 360f * value / total
            drawArc(
                color = categoryColors[idx % categoryColors.size],
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 22.dp.toPx(), cap = StrokeCap.Butt)
            )
            start += sweep
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onDelete: (Expense) -> Unit) {
    val fmt = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Medium)
                Text("${expense.category} · ${fmt.format(Date(expense.spentAt))}", style = MaterialTheme.typography.bodySmall)
            }
            Text(formatMoney(expense.amountCents), fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { onDelete(expense) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete ${expense.title}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseScreen(vm: LedgerViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val form by vm.form.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TopAppBar(title = { Text("Add expense") }, navigationIcon = {
            TextButton(onClick = onBack) { Text("Cancel") }
        }, actions = {
            TextButton(onClick = { vm.save(onSaved) }) { Text("Save") }
        })
        OutlinedTextField(form.title, vm::updateTitle, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(form.amount, vm::updateAmount, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Text("Category")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ExpenseCategory.entries) { c ->
                FilterChip(selected = form.category == c, onClick = { vm.updateCategory(c) }, label = { Text(c.label) })
            }
        }
        OutlinedTextField(form.note, vm::updateNote, label = { Text("Note") }, modifier = Modifier.fillMaxWidth())
        form.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

private fun formatMoney(cents: Long): String {
    val fmt = NumberFormat.getCurrencyInstance(Locale.US)
    fmt.currency = Currency.getInstance("USD")
    return fmt.format(cents / 100.0)
}
