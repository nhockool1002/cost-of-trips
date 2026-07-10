package com.nhockool1002.costoftrips.ui.screens.triplist

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.GradientStatCard
import com.nhockool1002.costoftrips.util.CurrencyFormatter
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    onTripClick: (Long) -> Unit,
    onAddTripClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: TripListViewModel = viewModel(factory = appViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var orderedTrips by remember { mutableStateOf(uiState.trips) }
    LaunchedEffect(uiState.trips) { orderedTrips = uiState.trips }

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
                title = { Text(stringResource(R.string.trip_list_title), style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.trip_list_add_trip)) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = onAddTripClick
            )
        }
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "greeting") {
                Text(
                    stringResource(R.string.trip_list_greeting),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            item(key = "hero") {
                GradientStatCard(
                    title = stringResource(R.string.trip_list_overview_total),
                    value = CurrencyFormatter.format(uiState.trips.sumOf { it.total }),
                    subtitle = stringResource(R.string.trip_list_overview_subtitle, uiState.trips.size)
                )
            }
            if (orderedTrips.isEmpty()) {
                item(key = "empty-state") {
                    EmptyState()
                }
            } else {
                items(orderedTrips, key = { it.trip.id }) { item ->
                    ReorderableItem(reorderableState, key = item.trip.id) { _ ->
                        TripCard(
                            emoji = tripEmojiFor(item.trip.id),
                            name = item.trip.name,
                            destination = item.trip.destination,
                            duration = formatTripDuration(item.trip.startDate, item.trip.endDate),
                            total = item.total,
                            dragModifier = Modifier.longPressDraggableHandle(
                                onDragStopped = {
                                    scope.launch {
                                        viewModel.reorderTrips(orderedTrips.map { it.trip.id })
                                    }
                                }
                            ),
                            onClick = { onTripClick(item.trip.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TripCard(
    emoji: String,
    name: String,
    destination: String,
    duration: String,
    total: Double,
    dragModifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp)
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    if (destination.isNotBlank()) {
                        Text(
                            "📍 $destination",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "☰",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = dragModifier.padding(start = 8.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📅 $duration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(999.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        CurrencyFormatter.format(total),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
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
