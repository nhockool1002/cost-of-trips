package com.nhockool1002.costoftrips.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class UserPreferencesRepositoryTest {

    private lateinit var repository: UserPreferencesRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        repository = UserPreferencesRepository(context)
        // preferencesDataStore() is a process-wide singleton keyed by file path (by design,
        // to guard against two DataStore instances on one file), so it carries state across
        // test methods in this JVM run regardless of Context identity - reset it via the
        // repository's own API rather than touching the backing file, which wouldn't reset
        // the already-cached in-memory DataStore instance anyway.
        runTest {
            repository.setThemeMode(ThemeMode.DARK)
            repository.setCurrency(AppCurrency.VND)
            repository.setReminderEnabled(false)
            repository.setReminderIntervalHours(6)
        }
    }

    @Test
    fun `themeMode defaults to DARK`() = runTest {
        assertEquals(ThemeMode.DARK, repository.themeMode.first())
    }

    @Test
    fun `setThemeMode persists the choice`() = runTest {
        repository.setThemeMode(ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, repository.themeMode.first())
    }

    @Test
    fun `currency defaults to VND`() = runTest {
        assertEquals(AppCurrency.VND, repository.currency.first())
    }

    @Test
    fun `setCurrency persists the choice`() = runTest {
        repository.setCurrency(AppCurrency.EUR)
        assertEquals(AppCurrency.EUR, repository.currency.first())
    }

    @Test
    fun `reminderEnabled defaults to false`() = runTest {
        assertEquals(false, repository.reminderEnabled.first())
    }

    @Test
    fun `setReminderEnabled persists the choice`() = runTest {
        repository.setReminderEnabled(true)
        assertEquals(true, repository.reminderEnabled.first())
    }

    @Test
    fun `reminderIntervalHours defaults to 6`() = runTest {
        assertEquals(6, repository.reminderIntervalHours.first())
    }

    @Test
    fun `setReminderIntervalHours persists the choice`() = runTest {
        repository.setReminderIntervalHours(24)
        assertEquals(24, repository.reminderIntervalHours.first())
    }
}
