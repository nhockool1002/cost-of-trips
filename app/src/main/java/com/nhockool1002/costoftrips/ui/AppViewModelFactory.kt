package com.nhockool1002.costoftrips.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.nhockool1002.costoftrips.CostOfTripsApp
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.ui.screens.addexpense.AddExpenseViewModel
import com.nhockool1002.costoftrips.ui.screens.createtrip.CreateTripViewModel
import com.nhockool1002.costoftrips.ui.screens.settings.SettingsViewModel
import com.nhockool1002.costoftrips.ui.screens.statistics.StatisticsViewModel
import com.nhockool1002.costoftrips.ui.screens.tripdetail.TripDetailViewModel
import com.nhockool1002.costoftrips.ui.screens.triplist.TripListViewModel

class AppViewModelFactory(
    private val tripRepository: TripRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appContext: Context,
    private val tripId: Long = 0
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(TripListViewModel::class.java) ->
                TripListViewModel(tripRepository, userPreferencesRepository) as T
            modelClass.isAssignableFrom(CreateTripViewModel::class.java) ->
                CreateTripViewModel(tripRepository) as T
            modelClass.isAssignableFrom(TripDetailViewModel::class.java) ->
                TripDetailViewModel(tripRepository, tripId) as T
            modelClass.isAssignableFrom(AddExpenseViewModel::class.java) ->
                AddExpenseViewModel(tripRepository, tripId) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(userPreferencesRepository, tripRepository, appContext) as T
            modelClass.isAssignableFrom(StatisticsViewModel::class.java) ->
                StatisticsViewModel(tripRepository, userPreferencesRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

fun appViewModelFactory(context: Context, tripId: Long = 0): AppViewModelFactory {
    val app = context.applicationContext as CostOfTripsApp
    return AppViewModelFactory(app.tripRepository, app.userPreferencesRepository, app, tripId)
}
