package com.nhockool1002.costoftrips.ui.screens.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TripWithTotal(val trip: Trip, val total: Double)

data class SpendingAnalytics(
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val mostExpensiveTrip: TripWithTotal? = null
)

data class TripListUiState(
    val trips: List<TripWithTotal> = emptyList(),
    val analytics: SpendingAnalytics = SpendingAnalytics()
)

class TripListViewModel(
    private val repository: TripRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<TripListUiState> = repository.observeTrips()
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

            TripListUiState(
                trips = tripsWithTotal,
                analytics = SpendingAnalytics(
                    monthlyTotal = monthlyTotal,
                    yearlyTotal = yearlyTotal,
                    mostExpensiveTrip = tripsWithTotal.filter { it.total > 0 }.maxByOrNull { it.total }
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripListUiState())

    fun reorderTrips(orderedTripIds: List<Long>) {
        viewModelScope.launch { repository.reorderTrips(orderedTripIds) }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch { repository.deleteTrip(trip) }
    }

    // Shows the rate-app dialog at most once per calendar day, and never again once the
    // user has rated (or the caller otherwise marks it permanently dismissed).
    suspend fun shouldShowRateDialog(): Boolean {
        if (userPreferencesRepository.rateDialogDismissedPermanently.first()) return false
        val lastShownAt = userPreferencesRepository.rateDialogLastShownAt.first()
        return !isSameDay(lastShownAt, System.currentTimeMillis())
    }

    fun onRateDialogShown() {
        viewModelScope.launch { userPreferencesRepository.setRateDialogLastShownAt(System.currentTimeMillis()) }
    }

    fun onRateDialogRated() {
        viewModelScope.launch { userPreferencesRepository.setRateDialogDismissedPermanently(true) }
    }
}

internal fun isSameDay(millisA: Long, millisB: Long): Boolean {
    if (millisA == 0L) return false
    val calA = Calendar.getInstance().apply { timeInMillis = millisA }
    val calB = Calendar.getInstance().apply { timeInMillis = millisB }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}
