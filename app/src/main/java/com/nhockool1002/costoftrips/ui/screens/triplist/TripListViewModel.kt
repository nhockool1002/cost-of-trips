package com.nhockool1002.costoftrips.ui.screens.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TripWithTotal(val trip: Trip, val total: Double)

class TripListViewModel(
    private val repository: TripRepository
) : ViewModel() {

    val trips: StateFlow<List<TripWithTotal>> = repository.observeTrips()
        .combine(repository.observeAllExpenses()) { trips, expenses ->
            val byTrip = expenses.groupBy { it.tripId }
            trips.map { trip ->
                TripWithTotal(trip, byTrip[trip.id].orEmpty().sumOf { it.amount })
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
