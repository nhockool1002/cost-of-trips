package com.nhockool1002.costoftrips.ui.screens.addexpense

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.CuteTextField
import com.nhockool1002.costoftrips.ui.screens.common.badgeColor
import com.nhockool1002.costoftrips.util.LocalCurrency
import com.nhockool1002.costoftrips.ui.screens.common.displayName
import com.nhockool1002.costoftrips.ui.screens.common.emoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    tripId: Long,
    onExpenseAdded: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AddExpenseViewModel = viewModel(factory = appViewModelFactory(context, tripId))

    var category by rememberSaveable { mutableStateOf(ExpenseCategory.OTHER) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_expense_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(stringResource(R.string.add_expense_category_label), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ExpenseCategory.entries) { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = { Text("${option.emoji()} ${option.displayName()}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            CuteTextField(
                value = amountText,
                onValueChange = { amountText = it; showError = false },
                label = stringResource(R.string.add_expense_amount_label),
                emoji = "💵",
                emojiContainerColor = category.badgeColor(),
                suffix = { Text(LocalCurrency.current.symbol) },
                isError = showError,
                supportingText = {
                    if (showError) Text(stringResource(R.string.add_expense_amount_required))
                },
                textStyle = MaterialTheme.typography.headlineMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            CuteTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.add_expense_note_label),
                emoji = "📝",
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0.0) {
                        showError = true
                    } else {
                        viewModel.addExpense(category, amount, note, onExpenseAdded)
                    }
                },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(100),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.add_expense_save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
