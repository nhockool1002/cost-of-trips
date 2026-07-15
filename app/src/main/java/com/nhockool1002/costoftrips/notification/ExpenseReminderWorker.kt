package com.nhockool1002.costoftrips.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nhockool1002.costoftrips.CostOfTripsApp
import com.nhockool1002.costoftrips.util.TripStatus
import com.nhockool1002.costoftrips.util.tripStatus

class ExpenseReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as CostOfTripsApp
        val ongoingTrip = app.tripRepository.getAllTrips()
            .firstOrNull { tripStatus(it.startDate, it.endDate) == TripStatus.ONGOING }
        NotificationHelper.showExpenseReminder(applicationContext, ongoingTrip?.name)
        return Result.success()
    }
}
