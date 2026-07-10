package com.nhockool1002.costoftrips.ui.screens.tripdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.CategoryIcon
import com.nhockool1002.costoftrips.ui.screens.common.GradientStatCard
import com.nhockool1002.costoftrips.ui.screens.common.displayName
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    tripId: Long,
    onBack: () -> Unit,
    onAddExpenseClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TripDetailViewModel = viewModel(factory = appViewModelFactory(context, tripId))
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var orderedExpenses by remember { mutableStateOf(uiState.expenses) }
    LaunchedEffect(uiState.expenses) { orderedExpenses = uiState.expenses }

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // from/to indices are positions within the whole LazyColumn (which also
        // has the "stat-card" item above the list), not positions within
        // orderedExpenses, so they must be resolved by key instead of used
        // directly or a drag near the list edges crashes with IndexOutOfBoundsException.
        val fromIndex = orderedExpenses.indexOfFirst { it.id == from.key }
        val toIndex = orderedExpenses.indexOfFirst { it.id == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            orderedExpenses = orderedExpenses.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.trip?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.trip_detail_add_expense)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAddExpenseClick
            )
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "stat-card") {
                GradientStatCard(
                    title = stringResource(R.string.trip_detail_total_label),
                    value = CurrencyFormatter.format(uiState.total)
                )
            }
            if (orderedExpenses.isEmpty()) {
                item(key = "empty-state") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🧾", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.trip_detail_empty_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.trip_detail_empty_expenses),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(orderedExpenses, key = { it.id }) { expense ->
                    ReorderableItem(reorderableState, key = expense.id) { _ ->
                        ExpenseCard(
                            expense = expense,
                            dragModifier = Modifier.longPressDraggableHandle(
                                onDragStopped = {
                                    scope.launch {
                                        viewModel.reorderExpenses(orderedExpenses.map { it.id })
                                    }
                                }
                            ),
                            onDeleteClick = { expenseToDelete = expense }
                        )
                    }
                }
            }
        }
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text(stringResource(R.string.expense_delete_title)) },
            text = { Text(stringResource(R.string.expense_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteExpense(expense)
                    expenseToDelete = null
                }) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    dragModifier: Modifier,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(expense.category)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(expense.category.displayName(), style = MaterialTheme.typography.titleMedium)
                    if (expense.note.isNotBlank()) {
                        Text(
                            expense.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "☰",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragModifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                CurrencyFormatter.format(expense.amount),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}
