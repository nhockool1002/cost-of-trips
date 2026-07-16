package com.nhockool1002.costoftrips.ui.screens.statistics

import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
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
class StatisticsViewModelTest {

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
    fun `categoryBreakdown sums per category and omits categories with no spend`() = runTest(mainDispatcherRule.testDispatcher) {
        val tripId = repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 30.0))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 20.0))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.TRANSPORT, amount = 10.0))

        val state = StatisticsViewModel(repository).uiState.first { it.categoryBreakdown.isNotEmpty() }

        assertEquals(2, state.categoryBreakdown.size)
        val food = state.categoryBreakdown.first { it.category == ExpenseCategory.FOOD }
        assertEquals(50.0, food.total, 0.0001)
        assertTrue(state.categoryBreakdown.none { it.category == ExpenseCategory.ACCOMMODATION })
    }

    @Test
    fun `categoryBreakdown is sorted by total descending`() = runTest(mainDispatcherRule.testDispatcher) {
        val tripId = repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 10.0))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.TRANSPORT, amount = 90.0))

        val state = StatisticsViewModel(repository).uiState.first { it.categoryBreakdown.size == 2 }

        assertEquals(ExpenseCategory.TRANSPORT, state.categoryBreakdown[0].category)
        assertEquals(ExpenseCategory.FOOD, state.categoryBreakdown[1].category)
    }

    @Test
    fun `monthlyTrend covers the last six months including the current one`() = runTest(mainDispatcherRule.testDispatcher) {
        val tripId = repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 25.0, date = System.currentTimeMillis()))

        val state = StatisticsViewModel(repository).uiState.first { it.monthlyTrend.isNotEmpty() }

        assertEquals(6, state.monthlyTrend.size)
        assertEquals(25.0, state.monthlyTrend.last().total, 0.0001)
    }

    @Test
    fun `mostExpensiveTrip reflects analytics across all trips`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
            .let { tripId -> repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 42.0)) }

        val state = StatisticsViewModel(repository).uiState.first { it.analytics.mostExpensiveTrip != null }

        assertEquals("Trip", state.analytics.mostExpensiveTrip?.trip?.name)
        assertEquals(42.0, state.analytics.mostExpensiveTrip?.total)
    }
}
