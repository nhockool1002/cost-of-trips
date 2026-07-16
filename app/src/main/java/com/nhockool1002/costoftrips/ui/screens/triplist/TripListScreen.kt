package com.nhockool1002.costoftrips.ui.screens.triplist

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.GradientStatCard
import com.nhockool1002.costoftrips.ui.screens.common.TripStatusBadge
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.LocalCurrency
import com.nhockool1002.costoftrips.util.TripStatus
import com.nhockool1002.costoftrips.util.openPlayStoreListing
import com.nhockool1002.costoftrips.util.tripStatus
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.text.DateFormat
import java.util.Date

private val tripEmojis = listOf("🧳", "✈️", "🏖️", "🏔️", "🌆", "🎒", "🚗", "⛺")

private fun tripEmojiFor(id: Long): String =
    tripEmojis[(id.toInt() and Int.MAX_VALUE) % tripEmojis.size]

private fun formatTripDuration(startDate: Long, endDate: Long): String {
    val format = DateFormat.getDateInstance(DateFormat.SHORT)
    return "${format.format(Date(startDate))} - ${format.format(Date(endDate))}"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TripListScreen(
    onTripClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TripListViewModel = viewModel(factory = appViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val currency = LocalCurrency.current

    var orderedTrips by remember { mutableStateOf(uiState.trips) }
    LaunchedEffect(uiState.trips) { orderedTrips = uiState.trips }

    var contextMenuTripId by remember { mutableStateOf<Long?>(null) }
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }

    var showRateDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (viewModel.shouldShowRateDialog()) {
            showRateDialog = true
            viewModel.onRateDialogShown()
        }
    }

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val displayedTrips = remember(orderedTrips, searchQuery) {
        if (searchQuery.isBlank()) {
            orderedTrips
        } else {
            orderedTrips.filter {
                it.trip.name.contains(searchQuery, ignoreCase = true) ||
                    it.trip.destination.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        // from/to indices are positions within the whole LazyColumn (which also
        // has the "greeting" and "hero" items above the list), not positions
        // within orderedTrips, so they must be resolved by key instead of used
        // directly or a drag near the list edges crashes with IndexOutOfBoundsException.
        val fromIndex = orderedTrips.indexOfFirst { it.trip.id == from.key }
        val toIndex = orderedTrips.indexOfFirst { it.trip.id == to.key }
        if (fromIndex != -1 && toIndex != -1) {
            orderedTrips = orderedTrips.toMutableList().apply {
                add(toIndex, removeAt(fromIndex))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Crossfade(targetState = isSearchActive, label = "trip-list-top-bar-title") { searching ->
                        if (searching) {
                            val focusRequester = remember { FocusRequester() }
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(stringResource(R.string.trip_list_search_placeholder)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        } else {
                            Text(stringResource(R.string.trip_list_title), style = MaterialTheme.typography.titleLarge)
                        }
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close_search))
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        if (orderedTrips.isNotEmpty()) {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.action_search))
                            }
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                        }
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isWideScreen = maxWidth > 600.dp
            val horizontalPadding = if (isWideScreen) 32.dp else 14.dp
            val contentMaxWidth = if (isWideScreen) 640.dp else maxWidth

            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "greeting") {
                    Text(
                        stringResource(R.string.trip_list_greeting),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                item(key = "hero") {
                    GradientStatCard(
                        title = stringResource(R.string.trip_list_overview_total),
                        value = CurrencyFormatter.format(uiState.trips.sumOf { it.total }, currency),
                        subtitle = stringResource(R.string.trip_list_overview_subtitle, uiState.trips.size),
                        compact = true
                    )
                }
                if (orderedTrips.isEmpty()) {
                    item(key = "empty-state") {
                        EmptyState()
                    }
                } else if (displayedTrips.isEmpty()) {
                    item(key = "no-results") {
                        NoSearchResults()
                    }
                } else if (searchQuery.isBlank()) {
                    items(displayedTrips, key = { it.trip.id }) { item ->
                        ReorderableItem(reorderableState, key = item.trip.id) { _ ->
                            TripCard(
                                emoji = tripEmojiFor(item.trip.id),
                                name = item.trip.name,
                                destination = item.trip.destination,
                                duration = formatTripDuration(item.trip.startDate, item.trip.endDate),
                                status = tripStatus(item.trip.startDate, item.trip.endDate),
                                total = item.total,
                                dragModifier = Modifier.longPressDraggableHandle(
                                    onDragStopped = {
                                        scope.launch {
                                            viewModel.reorderTrips(orderedTrips.map { it.trip.id })
                                        }
                                    }
                                ),
                                onClick = { onTripClick(item.trip.id) },
                                onLongClick = { contextMenuTripId = item.trip.id },
                                showContextMenu = contextMenuTripId == item.trip.id,
                                onDismissContextMenu = { contextMenuTripId = null },
                                onDeleteClick = {
                                    tripToDelete = item.trip
                                    contextMenuTripId = null
                                }
                            )
                        }
                    }
                } else {
                    // While filtering, indices no longer line up with the full
                    // orderedTrips list, so reordering is disabled and no drag
                    // handle is shown.
                    items(displayedTrips, key = { it.trip.id }) { item ->
                        TripCard(
                            emoji = tripEmojiFor(item.trip.id),
                            name = item.trip.name,
                            destination = item.trip.destination,
                            duration = formatTripDuration(item.trip.startDate, item.trip.endDate),
                            status = tripStatus(item.trip.startDate, item.trip.endDate),
                            total = item.total,
                            dragModifier = null,
                            onClick = { onTripClick(item.trip.id) },
                            onLongClick = { contextMenuTripId = item.trip.id },
                            showContextMenu = contextMenuTripId == item.trip.id,
                            onDismissContextMenu = { contextMenuTripId = null },
                            onDeleteClick = {
                                tripToDelete = item.trip
                                contextMenuTripId = null
                            }
                        )
                    }
                }
            }
        }
    }

    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text(stringResource(R.string.trip_list_delete_trip)) },
            text = { Text(stringResource(R.string.trip_list_delete_trip_message, trip.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTrip(trip)
                    tripToDelete = null
                }) { Text(stringResource(R.string.common_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (showRateDialog) {
        RateAppDialog(
            onRateNow = {
                viewModel.onRateDialogRated()
                openPlayStoreListing(context)
                showRateDialog = false
            },
            onDismiss = { showRateDialog = false }
        )
    }
}

@Composable
private fun RateAppDialog(onRateNow: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rate_dialog_title)) },
        text = { Text(stringResource(R.string.rate_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onRateNow) { Text(stringResource(R.string.rate_dialog_rate_now)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.rate_dialog_later)) }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TripCard(
    emoji: String,
    name: String,
    destination: String,
    duration: String,
    status: TripStatus,
    total: Double,
    dragModifier: Modifier?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    showContextMenu: Boolean,
    onDismissContextMenu: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currency = LocalCurrency.current
    Box {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, style = MaterialTheme.typography.titleMedium)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    ) {
                        Text(name, style = MaterialTheme.typography.titleMedium)
                        if (destination.isNotBlank()) {
                            Text(
                                "📍 $destination",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TripStatusBadge(status = status)
                    if (dragModifier != null) {
                        Text(
                            "☰",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = dragModifier.padding(start = 6.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "📅 $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            CurrencyFormatter.format(total, currency),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
        DropdownMenu(expanded = showContextMenu, onDismissRequest = onDismissContextMenu) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.trip_list_delete_trip)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun NoSearchResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.trip_list_search_no_results),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧳✨", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text(stringResource(R.string.trip_list_empty_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.trip_list_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
