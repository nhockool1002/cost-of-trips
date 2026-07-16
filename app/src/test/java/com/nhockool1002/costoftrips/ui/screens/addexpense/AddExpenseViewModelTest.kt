package com.nhockool1002.costoftrips.ui.screens.addexpense

import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
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
class AddExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: TripRepository
    private var tripId: Long = 0

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = TripRepository(database.tripDao(), database.expenseDao(), database.tripMemberDao(), database.expenseSplitDao())
        runTest {
            tripId = repository.createTrip(Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L))
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `members reflects trip members`() = runTest {
        repository.addMember(TripMember(tripId = tripId, name = "An"))
        val members = AddExpenseViewModel(repository, tripId).members.first { it.isNotEmpty() }
        assertEquals("An", members[0].name)
    }

    @Test
    fun `addExpense persists the expense with its splits and trims the note`() = runTest {
        val an = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val viewModel = AddExpenseViewModel(repository, tripId)

        var saved = false
        viewModel.addExpense(
            category = ExpenseCategory.FOOD,
            amount = 40.0,
            note = "  pho  ",
            paidByMemberId = an,
            splitMemberIds = listOf(an),
            onSaved = { saved = true }
        )

        val expenses = repository.observeExpensesForTrip(tripId).first { it.isNotEmpty() }
        assertEquals(40.0, expenses[0].amount, 0.0001)
        assertEquals("pho", expenses[0].note)
        assertEquals(an, expenses[0].paidByMemberId)
        assertTrue(saved)

        assertEquals(listOf(an), repository.getAllSplits().map { it.memberId })
    }

    @Test
    fun `addExpense ignores a non-positive amount`() = runTest {
        val viewModel = AddExpenseViewModel(repository, tripId)
        var saved = false
        viewModel.addExpense(ExpenseCategory.OTHER, 0.0, "", null, emptyList()) { saved = true }
        assertTrue(repository.getAllExpenses().isEmpty())
        assertTrue(!saved)
    }
}
