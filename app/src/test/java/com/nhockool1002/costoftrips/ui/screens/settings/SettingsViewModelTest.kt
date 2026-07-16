package com.nhockool1002.costoftrips.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.data.preferences.UserPreferencesRepository
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.testutil.InMemoryDatabaseFactory
import com.nhockool1002.costoftrips.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var tripRepository: TripRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: SettingsViewModel

    // SettingsViewModel eagerly launches 4 long-lived stateIn(WhileSubscribed) coroutines on
    // viewModelScope at construction time; those never complete on their own. Deliberately NOT
    // passing mainDispatcherRule.testDispatcher into runTest() below (unlike the other
    // ViewModel test files) keeps Main's dispatcher on its own independent scheduler, so those
    // coroutines are invisible to runTest's own "any active child jobs left?" completion check -
    // sharing the scheduler here reliably triggered a spurious 1-minute
    // UncompletedCoroutinesError (confirmed repeatedly in CI; trying to just cancel
    // viewModelScope before each test ends wasn't reliable enough to fully close the race).
    // Routing every SettingsViewModel through this store and clearing it is still good hygiene
    // for cross-test isolation, just no longer the thing preventing the hang.
    private val viewModelStore = ViewModelStore()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        database = InMemoryDatabaseFactory.create()
        tripRepository = TripRepository(database.tripDao(), database.expenseDao(), database.tripMemberDao(), database.expenseSplitDao())
        preferencesRepository = UserPreferencesRepository(context)
        viewModel = SettingsViewModel(preferencesRepository, tripRepository, context)
        viewModelStore.put("settings", viewModel)
    }

    @After
    fun tearDown() {
        viewModelStore.clear()
        database.close()
    }

    @Test
    fun `setThemeMode persists and is reflected in themeMode`() = runTest {
        viewModel.setThemeMode(ThemeMode.LIGHT)
        val mode = preferencesRepository.themeMode.first { it == ThemeMode.LIGHT }
        assertEquals(ThemeMode.LIGHT, mode)
        viewModelStore.clear()
    }

    @Test
    fun `setCurrency persists and is reflected in currency`() = runTest {
        viewModel.setCurrency(AppCurrency.USD)
        val currency = preferencesRepository.currency.first { it == AppCurrency.USD }
        assertEquals(AppCurrency.USD, currency)
        viewModelStore.clear()
    }

    @Test
    fun `setReminderEnabled persists the flag`() = runTest {
        viewModel.setReminderEnabled(true)
        val enabled = preferencesRepository.reminderEnabled.first { it }
        assertTrue(enabled)
        viewModelStore.clear()
    }

    @Test
    fun `setReminderIntervalHours persists the value`() = runTest {
        viewModel.setReminderIntervalHours(12)
        val hours = preferencesRepository.reminderIntervalHours.first { it == 12 }
        assertEquals(12, hours)
        viewModelStore.clear()
    }

    @Test
    fun `exportData then importData round-trips a trip into a fresh database`() = runTest {
        val tripId = tripRepository.createTrip(Trip(name = "Exported Trip", destination = "Hoi An", startDate = 0L, endDate = 0L))
        val memberId = tripRepository.addMember(TripMember(tripId = tripId, name = "An"))
        tripRepository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 15.0, paidByMemberId = memberId),
            splitMemberIds = listOf(memberId)
        )

        val json = viewModel.exportData()

        val freshDatabase = InMemoryDatabaseFactory.create()
        try {
            val freshRepository = TripRepository(
                freshDatabase.tripDao(), freshDatabase.expenseDao(), freshDatabase.tripMemberDao(), freshDatabase.expenseSplitDao()
            )
            val freshViewModel = SettingsViewModel(preferencesRepository, freshRepository, ApplicationProvider.getApplicationContext())
            viewModelStore.put("settings-fresh", freshViewModel)

            val result = freshViewModel.importData(json)

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull())
            assertEquals("Exported Trip", freshRepository.getAllTrips()[0].name)
            assertEquals(1, freshRepository.getAllMembers().size)
            assertEquals(1, freshRepository.getAllExpenses().size)
        } finally {
            freshDatabase.close()
        }
        viewModelStore.clear()
    }

    @Test
    fun `importData with malformed JSON returns failure`() = runTest {
        val result = viewModel.importData("not json")
        assertTrue(result.isFailure)
        viewModelStore.clear()
    }
}
