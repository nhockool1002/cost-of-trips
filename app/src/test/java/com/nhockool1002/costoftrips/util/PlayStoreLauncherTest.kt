package com.nhockool1002.costoftrips.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class PlayStoreLauncherTest {

    @Test
    fun `openPlayStoreListing starts a market intent for the app's package`() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        openPlayStoreListing(context)

        val started = shadowOf(context).nextStartedActivity
        assertEquals("market://details?id=${context.packageName}", started.data.toString())
    }
}
