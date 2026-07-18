package com.nhockool1002.costoftrips

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.notification.NotificationHelper
import com.nhockool1002.costoftrips.notification.ReminderScheduler
import com.nhockool1002.costoftrips.widget.CostOfTripsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
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
            database.expenseSplitDao()
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

        // Keeps the home screen widget in sync with every trip/expense change
        // (add, edit, delete, duplicate, import) without threading a refresh
        // call through each call site that mutates data.
        applicationScope.launch {
            combine(tripRepository.observeTrips(), tripRepository.observeAllExpenses()) { trips, expenses ->
                trips to expenses
            }.collect {
                CostOfTripsWidget().updateAll(this@CostOfTripsApp)
            }
        }
    }
}
