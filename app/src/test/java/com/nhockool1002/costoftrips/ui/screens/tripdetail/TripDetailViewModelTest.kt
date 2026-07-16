package com.nhockool1002.costoftrips.ui.screens.tripdetail

import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.Expense
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
@Config(sdk = [34])
class TripDetailViewModelTest {

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

    private fun viewModel() = TripDetailViewModel(repository, tripId)

    @Test
    fun `total sums every expense regardless of split info`() = runTest {
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 40.0))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.TRANSPORT, amount = 10.0))

        val state = viewModel().uiState.first { it.expenses.size == 2 }
        assertEquals(50.0, state.total, 0.0001)
    }

    @Test
    fun `an evenly split expense produces a settlement from the non-payer to the payer`() = runTest {
        val an = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val binh = repository.addMember(TripMember(tripId = tripId, name = "Binh"))
        repository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 100.0, paidByMemberId = an),
            splitMemberIds = listOf(an, binh)
        )

        val state = viewModel().uiState.first { it.settlements.isNotEmpty() }

        assertEquals(1, state.settlements.size)
        val settlement = state.settlements[0]
        assertEquals("Binh", settlement.from.name)
        assertEquals("An", settlement.to.name)
        assertEquals(50.0, settlement.amount, 0.0001)
    }

    @Test
    fun `an expense with a payer but no split members produces no settlements`() = runTest {
        val an = repository.addMember(TripMember(tripId = tripId, name = "An"))
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 100.0, paidByMemberId = an))

        val state = viewModel().uiState.first { it.expenses.isNotEmpty() }
        assertTrue(state.settlements.isEmpty())
    }

    @Test
    fun `deleteExpense removes the expense`() = runTest {
        val expenseId = repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 5.0))
        val vm = viewModel()
        val initial = vm.uiState.first { it.expenses.isNotEmpty() }

        vm.deleteExpense(initial.expenses.first { it.id == expenseId })

        val updated = repository.observeExpensesForTrip(tripId).first { it.isEmpty() }
        assertTrue(updated.isEmpty())
    }

    @Test
    fun `addMember then deleteMember`() = runTest {
        val vm = viewModel()
        vm.addMember("An")
        repository.observeMembersForTrip(tripId).first { it.isNotEmpty() }

        val state = vm.uiState.first { it.members.isNotEmpty() }
        assertEquals("An", state.members[0].name)

        vm.deleteMember(state.members[0])
        val afterDelete = repository.observeMembersForTrip(tripId).first { it.isEmpty() }
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `addMember ignores a blank name`() = runTest {
        viewModel().addMember("   ")
        assertTrue(repository.getAllMembers().isEmpty())
    }

    @Test
    fun `setBudget updates the trip`() = runTest {
        val vm = viewModel()
        vm.uiState.first { it.trip != null }

        vm.setBudget(500.0)

        val updated = repository.observeTrip(tripId).first { it?.budget == 500.0 }
        assertEquals(500.0, updated?.budget)
    }
}
