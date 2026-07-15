package com.nhockool1002.costoftrips.ui.screens.createtrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.repository.TripRepository
import kotlinx.coroutines.launch

class CreateTripViewModel(
    private val repository: TripRepository
) : ViewModel() {

    fun createTrip(
        name: String,
        destination: String,
        startDate: Long,
        endDate: Long,
        note: String,
        budget: Double?,
        onSaved: () -> Unit
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createTrip(
                Trip(
                    name = name.trim(),
                    destination = destination.trim(),
                    startDate = startDate,
                    endDate = endDate,
                    note = note.trim(),
                    budget = budget
                )
            )
            onSaved()
        }
    }
}
