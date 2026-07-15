package com.nhockool1002.costoftrips.ui.screens.tripdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.CategoryIcon
import com.nhockool1002.costoftrips.ui.screens.common.GradientStatCard
import com.nhockool1002.costoftrips.ui.screens.common.TripStatusBadge
import com.nhockool1002.costoftrips.ui.screens.common.displayName
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.LocalCurrency
import com.nhockool1002.costoftrips.util.tripStatus
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
    val currency = LocalCurrency.current

    var orderedExpenses by remember { mutableStateOf(uiState.expenses) }
    LaunchedEffect(uiState.expenses) { orderedExpenses = uiState.expenses }

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }

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
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            uiState.trip?.name.orEmpty(),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        uiState.trip?.let { trip ->
                            TripStatusBadge(
                                status = tripStatus(trip.startDate, trip.endDate),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                },
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
                    value = CurrencyFormatter.format(uiState.total, currency)
                )
            }
            item(key = "budget") {
                BudgetCard(
                    budget = uiState.trip?.budget,
                    spent = uiState.total,
                    onEditClick = { showBudgetDialog = true }
                )
            }
            item(key = "members") {
                MembersSection(
                    members = uiState.members,
                    onAddClick = { showAddMemberDialog = true },
                    onRemoveClick = { viewModel.deleteMember(it) }
                )
            }
            if (uiState.settlements.isNotEmpty()) {
                item(key = "balances") {
                    BalancesCard(settlements = uiState.settlements)
                }
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
                val memberNameById = uiState.members.associate { it.id to it.name }
                items(orderedExpenses, key = { it.id }) { expense ->
                    ReorderableItem(reorderableState, key = expense.id) { _ ->
                        ExpenseCard(
                            expense = expense,
                            paidByName = expense.paidByMemberId?.let { memberNameById[it] },
                            splitCount = uiState.splitMemberIdsByExpenseId[expense.id]?.size ?: 0,
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

    if (showAddMemberDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text(stringResource(R.string.trip_detail_add_member)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.trip_detail_member_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addMember(name)
                    showAddMemberDialog = false
                }) { Text(stringResource(R.string.trip_detail_add_member)) }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(uiState.trip?.budget?.toString().orEmpty()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(stringResource(R.string.trip_detail_budget_label)) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text(stringResource(R.string.trip_detail_budget_dialog_label)) },
                    suffix = { Text(currency.symbol) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setBudget(budgetInput.toDoubleOrNull())
                    showBudgetDialog = false
                }) { Text(stringResource(R.string.create_trip_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun BudgetCard(budget: Double?, spent: Double, onEditClick: () -> Unit) {
    val currency = LocalCurrency.current
    if (budget == null) {
        AssistChip(
            onClick = onEditClick,
            label = { Text(stringResource(R.string.trip_detail_budget_set)) },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        return
    }

    val remaining = budget - spent
    val isOverBudget = remaining < 0
    val containerColor = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer
    val contentColor = if (isOverBudget) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer
    val progress = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 1f

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(R.string.trip_detail_budget_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.trip_detail_budget_edit),
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = if (isOverBudget) {
                    stringResource(R.string.trip_detail_budget_over, CurrencyFormatter.format(-remaining, currency))
                } else {
                    stringResource(R.string.trip_detail_budget_remaining, CurrencyFormatter.format(remaining, currency))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun MembersSection(
    members: List<TripMember>,
    onAddClick: () -> Unit,
    onRemoveClick: (TripMember) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.trip_detail_members_label), style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(members, key = { it.id }) { member ->
                InputChip(
                    selected = false,
                    onClick = {},
                    label = { Text(member.name) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onRemoveClick(member) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = stringResource(R.string.common_delete),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                )
            }
            item(key = "add-member") {
                AssistChip(
                    onClick = onAddClick,
                    label = { Text(stringResource(R.string.trip_detail_add_member)) },
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun BalancesCard(settlements: List<SettlementSuggestion>) {
    val currency = LocalCurrency.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.trip_detail_balances_label), style = MaterialTheme.typography.titleMedium)
            settlements.forEach { settlement ->
                Text(
                    stringResource(
                        R.string.trip_detail_settlement_line,
                        settlement.from.name,
                        settlement.to.name,
                        CurrencyFormatter.format(settlement.amount, currency)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    paidByName: String?,
    splitCount: Int,
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
            if (paidByName != null && splitCount > 0) {
                Text(
                    stringResource(R.string.trip_detail_paid_by_split, paidByName, splitCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                CurrencyFormatter.format(expense.amount, LocalCurrency.current),
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
