package com.nhockool1002.costoftrips

import android.app.Application
import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.notification.NotificationHelper
import com.nhockool1002.costoftrips.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CostOfTripsApp : Application() {
    lateinit var tripRepository: TripRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        tripRepository = TripRepository(
            database.tripDao(),
            database.expenseDao(),
            database.tripMemberDao(),
            database.expenseSplitDao(),
            database.checklistItemDao()
        )
        userPreferencesRepository = UserPreferencesRepository(this)

        NotificationHelper.createChannel(this)

        // Periodic work otherwise survives process death on its own, but this
        // guards against the (rare) case where WorkManager's own store lost
        // track of it while the preference still says "on".
        applicationScope.launch {
            if (userPreferencesRepository.reminderEnabled.first()) {
                val hours = userPreferencesRepository.reminderIntervalHours.first()
                ReminderScheduler.schedule(this@CostOfTripsApp, hours)
            }
        }
    }
}
