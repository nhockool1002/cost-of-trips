package com.nhockool1002.costoftrips.ui.screens.createtrip

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import java.text.DateFormat
import java.util.Date

private enum class DateTarget { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTripScreen(
    onTripCreated: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: CreateTripViewModel = viewModel(factory = appViewModelFactory(context))

    var name by rememberSaveable { mutableStateOf("") }
    var destination by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var endDate by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var showError by rememberSaveable { mutableStateOf(false) }
    var datePickerTarget by remember { mutableStateOf<DateTarget?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_trip_title)) },
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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; showError = false },
                label = { Text(stringResource(R.string.create_trip_name_label)) },
                isError = showError,
                supportingText = {
                    if (showError) Text(stringResource(R.string.create_trip_name_required))
                },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destination,
                onValueChange = { destination = it },
                label = { Text(stringResource(R.string.create_trip_destination_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = DateFormat.getDateInstance().format(Date(startDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.create_trip_start_date_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerTarget = DateTarget.START }
            )
            OutlinedTextField(
                value = DateFormat.getDateInstance().format(Date(endDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.create_trip_end_date_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerTarget = DateTarget.END }
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(stringResource(R.string.create_trip_note_label)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        viewModel.createTrip(name, destination, startDate, endDate, note, onTripCreated)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.create_trip_save))
            }
        }
    }

    datePickerTarget?.let { target ->
        val initial = if (target == DateTarget.START) startDate else endDate
        val state = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { datePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        if (target == DateTarget.START) startDate = it else endDate = it
                    }
                    datePickerTarget = null
                }) { Text(stringResource(R.string.create_trip_save)) }
            },
            dismissButton = {
                TextButton(onClick = { datePickerTarget = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
