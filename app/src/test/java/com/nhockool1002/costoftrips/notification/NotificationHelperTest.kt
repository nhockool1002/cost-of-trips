package com.nhockool1002.costoftrips.notification

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NotificationHelperTest {

    @Test
    fun `createChannel registers the expense reminder channel`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        NotificationHelper.createChannel(context)

        val manager = context.getSystemService(NotificationManager::class.java)
        assertNotNull(manager.getNotificationChannel(NotificationHelper.CHANNEL_ID))
    }

    @Test
    fun `showExpenseReminder posts a notification when permission is granted`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        NotificationHelper.createChannel(context)

        NotificationHelper.showExpenseReminder(context, "Da Lat Trip")

        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals(1, shadowOf(manager).allNotifications.size)
    }

    @Test
    fun `showExpenseReminder does nothing when permission is not granted`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
        NotificationHelper.createChannel(context)

        NotificationHelper.showExpenseReminder(context, null)

        val manager = context.getSystemService(NotificationManager::class.java)
        assertEquals(0, shadowOf(manager).allNotifications.size)
    }
}
