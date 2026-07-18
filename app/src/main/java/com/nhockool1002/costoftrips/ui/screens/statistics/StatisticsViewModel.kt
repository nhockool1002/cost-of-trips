package com.nhockool1002.costoftrips.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.ui.screens.triplist.SpendingAnalytics
import com.nhockool1002.costoftrips.ui.screens.triplist.TripWithTotal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CategoryTotal(val category: ExpenseCategory, val total: Double)
data class MonthlyTotal(val label: String, val total: Double)

data class StatisticsUiState(
    val analytics: SpendingAnalytics = SpendingAnalytics(),
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val monthlyTrend: List<MonthlyTotal> = emptyList()
)

class StatisticsViewModel(
    private val repository: TripRepository,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val monthlyGoal: StateFlow<Double?> = preferencesRepository.monthlyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val yearlyGoal: StateFlow<Double?> = preferencesRepository.yearlyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val uiState: StateFlow<StatisticsUiState> = repository.observeTrips()
        .combine(repository.observeAllExpenses()) { trips, expenses ->
            val byTrip = expenses.groupBy { it.tripId }
            val tripsWithTotal = trips.map { trip ->
                TripWithTotal(trip, byTrip[trip.id].orEmpty().sumOf { it.amount })
            }

            val now = Calendar.getInstance()
            val currentMonth = now.get(Calendar.MONTH)
            val currentYear = now.get(Calendar.YEAR)
            val cal = Calendar.getInstance()
            var monthlyTotal = 0.0
            var yearlyTotal = 0.0
            expenses.forEach { expense ->
                cal.timeInMillis = expense.date
                if (cal.get(Calendar.YEAR) == currentYear) {
                    yearlyTotal += expense.amount
                    if (cal.get(Calendar.MONTH) == currentMonth) {
                        monthlyTotal += expense.amount
                    }
                }
            }

            val categoryBreakdown = ExpenseCategory.entries
                .map { category -> CategoryTotal(category, expenses.filter { it.category == category }.sumOf { it.amount }) }
                .filter { it.total > 0 }
                .sortedByDescending { it.total }

            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val monthlyTrend = (5 downTo 0).map { monthsAgo ->
                val monthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }
                val year = monthCal.get(Calendar.YEAR)
                val month = monthCal.get(Calendar.MONTH)
                val total = expenses.filter {
                    cal.timeInMillis = it.date
                    cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
                }.sumOf { it.amount }
                MonthlyTotal(monthFormat.format(monthCal.time), total)
            }

            StatisticsUiState(
                analytics = SpendingAnalytics(
                    monthlyTotal = monthlyTotal,
                    yearlyTotal = yearlyTotal,
                    mostExpensiveTrip = tripsWithTotal.filter { it.total > 0 }.maxByOrNull { it.total }
                ),
                categoryBreakdown = categoryBreakdown,
                monthlyTrend = monthlyTrend
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())
}
