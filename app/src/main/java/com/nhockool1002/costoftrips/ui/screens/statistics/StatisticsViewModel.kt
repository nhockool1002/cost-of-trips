package com.nhockool1002.costoftrips.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.ui.screens.triplist.SpendingAnalytics
import com.nhockool1002.costoftrips.ui.screens.triplist.TripWithTotal
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class StatisticsViewModel(
    private val repository: TripRepository
) : ViewModel() {

    val analytics: StateFlow<SpendingAnalytics> = repository.observeTrips()
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

            SpendingAnalytics(
                monthlyTotal = monthlyTotal,
                yearlyTotal = yearlyTotal,
                mostExpensiveTrip = tripsWithTotal.filter { it.total > 0 }.maxByOrNull { it.total }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpendingAnalytics())
}
