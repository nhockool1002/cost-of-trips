package com.nhockool1002.costoftrips.ui.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nhockool1002.costoftrips.R
import com.nhockool1002.costoftrips.ui.appViewModelFactory
import com.nhockool1002.costoftrips.ui.screens.common.BarDatum
import com.nhockool1002.costoftrips.ui.screens.common.DonutSlice
import com.nhockool1002.costoftrips.ui.screens.common.ProgressCard
import com.nhockool1002.costoftrips.ui.screens.common.SpendingDonutChart
import com.nhockool1002.costoftrips.ui.screens.common.TrendBarChart
import com.nhockool1002.costoftrips.ui.screens.common.badgeColor
import com.nhockool1002.costoftrips.ui.screens.common.displayName
import com.nhockool1002.costoftrips.util.CurrencyFormatter
import com.nhockool1002.costoftrips.util.LocalCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    val context = LocalContext.current
    val viewModel: StatisticsViewModel = viewModel(factory = appViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    val monthlyGoal by viewModel.monthlyGoal.collectAsState()
    val yearlyGoal by viewModel.yearlyGoal.collectAsState()
    val analytics = uiState.analytics
    val currency = LocalCurrency.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.trip_list_analytics_title)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "category-breakdown") {
                ChartCard(title = stringResource(R.string.trip_list_analytics_category_breakdown)) {
                    SpendingDonutChart(
                        slices = uiState.categoryBreakdown.map {
                            DonutSlice(it.category.displayName(), it.total, it.category.badgeColor())
                        },
                        centerLabel = CurrencyFormatter.format(uiState.categoryBreakdown.sumOf { it.total }, currency)
                    )
                }
            }
            item(key = "monthly-trend") {
                ChartCard(title = stringResource(R.string.trip_list_analytics_monthly_trend)) {
                    TrendBarChart(bars = uiState.monthlyTrend.map { BarDatum(it.label, it.total) })
                }
            }
            monthlyGoal?.let { goal ->
                item(key = "monthly-goal") {
                    ProgressCard(
                        title = stringResource(R.string.statistics_monthly_goal_label),
                        current = analytics.monthlyTotal,
                        limit = goal
                    )
                }
            }
            yearlyGoal?.let { goal ->
                item(key = "yearly-goal") {
                    ProgressCard(
                        title = stringResource(R.string.statistics_yearly_goal_label),
                        current = analytics.yearlyTotal,
                        limit = goal
                    )
                }
            }
            item(key = "monthly") {
                AnalyticsRow(
                    emoji = "📅",
                    label = stringResource(R.string.trip_list_analytics_monthly),
                    value = CurrencyFormatter.format(analytics.monthlyTotal, currency)
                )
            }
            item(key = "yearly") {
                AnalyticsRow(
                    emoji = "🗓️",
                    label = stringResource(R.string.trip_list_analytics_yearly),
                    value = CurrencyFormatter.format(analytics.yearlyTotal, currency)
                )
            }
            item(key = "top-trip") {
                AnalyticsRow(
                    emoji = "🏆",
                    label = stringResource(R.string.trip_list_analytics_top_trip),
                    value = analytics.mostExpensiveTrip?.let {
                        "${it.trip.name} · ${CurrencyFormatter.format(it.total, currency)}"
                    } ?: stringResource(R.string.trip_list_analytics_no_data)
                )
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            content()
        }
    }
}

@Composable
private fun AnalyticsRow(emoji: String, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, modifier = Modifier.padding(end = 12.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
