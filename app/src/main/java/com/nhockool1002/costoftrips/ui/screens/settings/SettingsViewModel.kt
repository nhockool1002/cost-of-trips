package com.nhockool1002.costoftrips.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nhockool1002.costoftrips.data.export.DataExporter
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.notification.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val tripRepository: TripRepository,
    private val appContext: Context
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    val currency: StateFlow<AppCurrency> = preferencesRepository.currency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppCurrency.VND)

    fun setCurrency(currency: AppCurrency) {
        viewModelScope.launch { preferencesRepository.setCurrency(currency) }
    }

    val reminderEnabled: StateFlow<Boolean> = preferencesRepository.reminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reminderIntervalHours: StateFlow<Int> = preferencesRepository.reminderIntervalHours
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReminderEnabled(enabled)
            if (enabled) {
                ReminderScheduler.schedule(appContext, reminderIntervalHours.value)
            } else {
                ReminderScheduler.cancel(appContext)
            }
        }
    }

    fun setReminderIntervalHours(hours: Int) {
        viewModelScope.launch {
            preferencesRepository.setReminderIntervalHours(hours)
            if (reminderEnabled.value) {
                ReminderScheduler.schedule(appContext, hours)
            }
        }
    }

    suspend fun exportData(): String {
        val trips = tripRepository.getAllTrips()
        val expenses = tripRepository.getAllExpenses()
        val members = tripRepository.getAllMembers()
        val splits = tripRepository.getAllSplits()
        return DataExporter.buildJson(trips, expenses, members, splits)
    }

    suspend fun importData(json: String): Result<Int> = runCatching {
        val importedTrips = DataExporter.parseJson(json)
        tripRepository.importTrips(importedTrips)
    }
}
