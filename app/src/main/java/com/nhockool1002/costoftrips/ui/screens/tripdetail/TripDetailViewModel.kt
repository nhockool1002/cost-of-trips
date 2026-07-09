package com.nhockool1002.costoftrips.ui.screens.tripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: Trip? = null,
    val expenses: List<Expense> = emptyList(),
    val total: Double = 0.0
)

class TripDetailViewModel(
    private val repository: TripRepository,
    private val tripId: Long
) : ViewModel() {

    val uiState: StateFlow<TripDetailUiState> = repository.observeTrip(tripId)
        .combine(repository.observeExpensesForTrip(tripId)) { trip, expenses ->
            TripDetailUiState(trip, expenses, expenses.sumOf { it.amount })
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripDetailUiState())

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }
}
