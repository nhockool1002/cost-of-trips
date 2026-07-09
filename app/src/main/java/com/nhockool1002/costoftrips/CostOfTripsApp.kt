package com.nhockool1002.costoftrips

import android.app.Application
import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository

class CostOfTripsApp : Application() {
    lateinit var tripRepository: TripRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        tripRepository = TripRepository(database.tripDao(), database.expenseDao())
        userPreferencesRepository = UserPreferencesRepository(this)
    }
}
