package com.nhockool1002.costoftrips.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val UNIQUE_WORK_NAME = "expense_reminder"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ReminderSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun `schedule enqueues a unique periodic work request`() {
        ReminderScheduler.schedule(context, intervalHours = 6)

        // Not asserting a specific WorkInfo.State here: WorkManagerTestInitHelper's default
        // test executor runs unconstrained work immediately, so by the time this reads back
        // the WorkInfo the request may already have executed (and re-armed, or failed, since
        // ExpenseReminderWorker needs a real CostOfTripsApp it doesn't get under this
        // deliberately plain test Application) - none of that is what this test cares about.
        // What matters is that scheduling registers exactly one request under the unique name.
        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UNIQUE_WORK_NAME).get()
        assertEquals(1, workInfos.size)
    }

    @Test
    fun `scheduling again with the same name replaces the previous request`() {
        ReminderScheduler.schedule(context, intervalHours = 6)
        ReminderScheduler.schedule(context, intervalHours = 12)

        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UNIQUE_WORK_NAME).get()
        assertEquals(1, workInfos.size)
    }

    @Test
    fun `cancel removes the scheduled work`() {
        ReminderScheduler.schedule(context, intervalHours = 6)
        ReminderScheduler.cancel(context)

        val workInfos = WorkManager.getInstance(context).getWorkInfosForUniqueWork(UNIQUE_WORK_NAME).get()
        assertTrue(workInfos.all { it.state == WorkInfo.State.CANCELLED })
    }
}
