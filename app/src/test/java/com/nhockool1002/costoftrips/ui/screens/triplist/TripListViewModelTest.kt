package com.nhockool1002.costoftrips.ui.screens.triplist

import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.repository.TripRepository
import com.nhockool1002.costoftrips.testutil.InMemoryDatabaseFactory
import com.nhockool1002.costoftrips.testutil.MainDispatcherRule
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TripListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: TripRepository

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = TripRepository(database.tripDao(), database.expenseDao(), database.tripMemberDao(), database.expenseSplitDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `uiState reflects trips with their expense totals`() = runTest {
        val tripId = repository.createTrip(Trip(name = "Da Lat", destination = "Da Lat", startDate = 0L, endDate = 0L))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 100.0))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.TRANSPORT, amount = 50.0))

        val state = TripListViewModel(repository).uiState.first { it.trips.isNotEmpty() }

        assertEquals(1, state.trips.size)
        assertEquals(150.0, state.trips[0].total, 0.0001)
    }

    @Test
    fun `analytics counts only expenses from the current month and year`() = runTest {
        val tripId = repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
        val now = System.currentTimeMillis()
        val longAgo = now - TimeUnit.DAYS.toMillis(400)
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 20.0, date = now))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 999.0, date = longAgo))

        val state = TripListViewModel(repository).uiState.first { it.trips.isNotEmpty() }

        assertEquals(20.0, state.analytics.monthlyTotal, 0.0001)
        assertEquals(20.0, state.analytics.yearlyTotal, 0.0001)
    }

    @Test
    fun `mostExpensiveTrip ignores trips with zero spend`() = runTest {
        repository.createTrip(Trip(name = "Empty trip", destination = "", startDate = 0L, endDate = 0L))
        val spentTripId = repository.createTrip(Trip(name = "Spent trip", destination = "", startDate = 0L, endDate = 0L))
        repository.addExpense(Expense(tripId = spentTripId, category = ExpenseCategory.FOOD, amount = 10.0))

        val state = TripListViewModel(repository).uiState.first { it.trips.size == 2 }

        assertEquals("Spent trip", state.analytics.mostExpensiveTrip?.trip?.name)
    }

    @Test
    fun `reorderTrips persists the new order`() = runTest {
        val id1 = repository.createTrip(Trip(name = "First", destination = "", startDate = 0L, endDate = 0L))
        val id2 = repository.createTrip(Trip(name = "Second", destination = "", startDate = 0L, endDate = 0L))

        val viewModel = TripListViewModel(repository)
        viewModel.reorderTrips(listOf(id2, id1))

        val reordered = repository.observeTrips()
            .first { trips -> trips.sortedBy { it.sortOrder }.map { it.id } == listOf(id2, id1) }
        assertEquals(listOf(id2, id1), reordered.sortedBy { it.sortOrder }.map { it.id })
    }
}
