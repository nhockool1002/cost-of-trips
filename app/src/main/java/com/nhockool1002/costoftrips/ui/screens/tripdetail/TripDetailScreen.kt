package com.nhockool1002.costoftrips.ui.screens.tripdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.ChecklistItem
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.CategoryIcon
import com.nhockool1002.costoftrips.ui.screens.common.GradientStatCard
import com.nhockool1002.costoftrips.ui.screens.common.ProgressCard
import com.nhockool1002.costoftrips.ui.screens.common.TripStatusBadge
import com.nhockool1002.costoftrips.ui.screens.common.displayName
import com.nhockool1002.costoftrips.ui.screens.common.emoji
import com.nhockool1002.costoftrips.util.CurrencyAmountVisualTransformation
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.LocalCurrency
import com.nhockool1002.costoftrips.util.amountToRawDigits
import com.nhockool1002.costoftrips.util.rawDigitsToAmount
import com.nhockool1002.costoftrips.util.tripStatus
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.Plus
import compose.icons.tablericons.Trash
import compose.icons.tablericons.X
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.DateFormat
import java.util.Date

private enum class FilterDateTarget { FROM, TO }

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

    var showFilterDialog by remember { mutableStateOf(false) }
    var filterCategories by remember { mutableStateOf(emptySet<ExpenseCategory>()) }
    var filterMemberIds by remember { mutableStateOf(emptySet<Long>()) }
    var filterFromDate by remember { mutableStateOf<Long?>(null) }
    var filterToDate by remember { mutableStateOf<Long?>(null) }
    var filterDateTarget by remember { mutableStateOf<FilterDateTarget?>(null) }
    val isFilterActive = filterCategories.isNotEmpty() || filterMemberIds.isNotEmpty() ||
        filterFromDate != null || filterToDate != null
    val filteredExpenses = remember(orderedExpenses, filterCategories, filterMemberIds, filterFromDate, filterToDate) {
        orderedExpenses.filter { expense ->
            (filterCategories.isEmpty() || expense.category in filterCategories) &&
                (filterMemberIds.isEmpty() || expense.paidByMemberId in filterMemberIds) &&
                (filterFromDate == null || expense.date >= filterFromDate!!) &&
                (filterToDate == null || expense.date <= filterToDate!!)
        }
    }

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
                        Icon(TablerIcons.ArrowLeft, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (uiState.expenses.isNotEmpty()) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Text(
                                "▼",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isFilterActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExpenseClick) {
                Icon(TablerIcons.Plus, contentDescription = stringResource(R.string.trip_detail_add_expense))
            }
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 100.dp),
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
            item(key = "checklist") {
                ChecklistSection(
                    items = uiState.checklist,
                    onToggle = { viewModel.toggleChecklistItem(it) },
                    onDelete = { viewModel.deleteChecklistItem(it) },
                    onAdd = { viewModel.addChecklistItem(it) }
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
            } else if (filteredExpenses.isEmpty()) {
                item(key = "filter-no-results") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.trip_detail_filter_no_results),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!isFilterActive) {
                val memberNameById = uiState.members.associate { it.id to it.name }
                items(filteredExpenses, key = { it.id }) { expense ->
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
                            onDuplicateClick = { viewModel.duplicateExpense(expense) },
                            onDeleteClick = { expenseToDelete = expense }
                        )
                    }
                }
            } else {
                // While filtering, indices no longer line up with the full
                // orderedExpenses list, so reordering is disabled and no drag
                // handle is shown.
                val memberNameById = uiState.members.associate { it.id to it.name }
                items(filteredExpenses, key = { it.id }) { expense ->
                    ExpenseCard(
                        expense = expense,
                        paidByName = expense.paidByMemberId?.let { memberNameById[it] },
                        splitCount = uiState.splitMemberIdsByExpenseId[expense.id]?.size ?: 0,
                        dragModifier = null,
                        onDuplicateClick = { viewModel.duplicateExpense(expense) },
                        onDeleteClick = { expenseToDelete = expense }
                    )
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
        var budgetDigits by remember { mutableStateOf(amountToRawDigits(uiState.trip?.budget ?: 0.0, currency)) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text(stringResource(R.string.trip_detail_budget_label)) },
            text = {
                OutlinedTextField(
                    value = budgetDigits,
                    onValueChange = { input -> budgetDigits = input.filter { it.isDigit() } },
                    label = { Text(stringResource(R.string.trip_detail_budget_dialog_label)) },
                    suffix = { Text(currency.symbol) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = CurrencyAmountVisualTransformation(currency),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setBudget(if (budgetDigits.isEmpty()) null else rawDigitsToAmount(budgetDigits, currency))
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

    if (showFilterDialog) {
        val dateFormat = remember { DateFormat.getDateInstance(DateFormat.SHORT) }
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text(stringResource(R.string.trip_detail_filter_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.trip_detail_filter_category), style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ExpenseCategory.entries) { category ->
                            FilterChip(
                                selected = category in filterCategories,
                                onClick = {
                                    filterCategories = if (category in filterCategories) {
                                        filterCategories - category
                                    } else {
                                        filterCategories + category
                                    }
                                },
                                label = { Text("${category.emoji()} ${category.displayName()}") }
                            )
                        }
                    }
                    if (uiState.members.isNotEmpty()) {
                        Text(stringResource(R.string.trip_detail_filter_payer), style = MaterialTheme.typography.labelLarge)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uiState.members, key = { it.id }) { member ->
                                FilterChip(
                                    selected = member.id in filterMemberIds,
                                    onClick = {
                                        filterMemberIds = if (member.id in filterMemberIds) {
                                            filterMemberIds - member.id
                                        } else {
                                            filterMemberIds + member.id
                                        }
                                    },
                                    label = { Text(member.name) }
                                )
                            }
                        }
                    }
                    Text(stringResource(R.string.trip_detail_filter_date_range), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { filterDateTarget = FilterDateTarget.FROM },
                            label = {
                                Text(filterFromDate?.let { dateFormat.format(Date(it)) } ?: stringResource(R.string.trip_detail_filter_from))
                            }
                        )
                        AssistChip(
                            onClick = { filterDateTarget = FilterDateTarget.TO },
                            label = {
                                Text(filterToDate?.let { dateFormat.format(Date(it)) } ?: stringResource(R.string.trip_detail_filter_to))
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text(stringResource(R.string.common_close))
                }
            },
            dismissButton = {
                TextButton(
                    enabled = isFilterActive,
                    onClick = {
                        filterCategories = emptySet()
                        filterMemberIds = emptySet()
                        filterFromDate = null
                        filterToDate = null
                    }
                ) { Text(stringResource(R.string.trip_detail_filter_clear)) }
            }
        )
    }

    filterDateTarget?.let { target ->
        val initial = if (target == FilterDateTarget.FROM) filterFromDate else filterToDate
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { filterDateTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        if (target == FilterDateTarget.FROM) filterFromDate = it else filterToDate = it
                    }
                    filterDateTarget = null
                }) { Text(stringResource(R.string.create_trip_save)) }
            },
            dismissButton = {
                TextButton(onClick = { filterDateTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun BudgetCard(budget: Double?, spent: Double, onEditClick: () -> Unit) {
    if (budget == null) {
        AssistChip(
            onClick = onEditClick,
            label = { Text(stringResource(R.string.trip_detail_budget_set)) },
            leadingIcon = { Icon(TablerIcons.Plus, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        return
    }

    ProgressCard(
        title = stringResource(R.string.trip_detail_budget_label),
        current = spent,
        limit = budget,
        trailingIcon = { contentColor ->
            Icon(
                TablerIcons.Pencil,
                contentDescription = stringResource(R.string.trip_detail_budget_edit),
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        },
        onClick = onEditClick
    )
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
                                TablerIcons.X,
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
                    leadingIcon = { Icon(TablerIcons.Plus, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun ChecklistSection(
    items: List<ChecklistItem>,
    onToggle: (ChecklistItem) -> Unit,
    onDelete: (ChecklistItem) -> Unit,
    onAdd: (String) -> Unit
) {
    var newItemText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.trip_detail_checklist_label), style = MaterialTheme.typography.titleMedium)

            if (items.isEmpty()) {
                Text(
                    stringResource(R.string.trip_detail_checklist_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(checked = item.isChecked, onCheckedChange = { onToggle(item) })
                        Text(
                            item.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                            color = if (item.isChecked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                TablerIcons.X,
                                contentDescription = stringResource(R.string.common_delete),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    placeholder = { Text(stringResource(R.string.trip_detail_checklist_add_hint)) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        onAdd(newItemText)
                        newItemText = ""
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(TablerIcons.Plus, contentDescription = stringResource(R.string.trip_detail_checklist_add_hint))
                }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExpenseCard(
    expense: Expense,
    paidByName: String?,
    splitCount: Int,
    dragModifier: Modifier?,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = {}, onLongClick = { showMenu = true })
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
                    if (dragModifier != null) {
                        Text(
                            "☰",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = dragModifier.padding(horizontal = 8.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            TablerIcons.Trash,
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
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.common_duplicate)) },
                leadingIcon = { Text("📋") },
                onClick = {
                    showMenu = false
                    onDuplicateClick()
                }
            )
        }
    }
}
