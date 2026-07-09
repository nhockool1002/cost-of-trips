package com.nhockool1002.costoftrips.ui.screens.createtrip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.nhockool1002.costoftrips.ui.screens.common.CuteTextField
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CuteTextField(
                value = name,
                onValueChange = { name = it; showError = false },
                label = stringResource(R.string.create_trip_name_label),
                emoji = "🧳",
                isError = showError,
                supportingText = {
                    if (showError) Text(stringResource(R.string.create_trip_name_required))
                },
                modifier = Modifier.fillMaxWidth()
            )
            CuteTextField(
                value = destination,
                onValueChange = { destination = it },
                label = stringResource(R.string.create_trip_destination_label),
                emoji = "📍",
                modifier = Modifier.fillMaxWidth()
            )
            CuteTextField(
                value = DateFormat.getDateInstance().format(Date(startDate)),
                onValueChange = {},
                label = stringResource(R.string.create_trip_start_date_label),
                emoji = "📅",
                onClick = { datePickerTarget = DateTarget.START },
                modifier = Modifier.fillMaxWidth()
            )
            CuteTextField(
                value = DateFormat.getDateInstance().format(Date(endDate)),
                onValueChange = {},
                label = stringResource(R.string.create_trip_end_date_label),
                emoji = "🏁",
                onClick = { datePickerTarget = DateTarget.END },
                modifier = Modifier.fillMaxWidth()
            )
            CuteTextField(
                value = note,
                onValueChange = { note = it },
                label = stringResource(R.string.create_trip_note_label),
                emoji = "📝",
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    if (name.isBlank()) {
                        showError = true
                    } else {
                        viewModel.createTrip(name, destination, startDate, endDate, note, onTripCreated)
                    }
                },
                shape = RoundedCornerShape(100),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.create_trip_save), style = MaterialTheme.typography.titleMedium)
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
