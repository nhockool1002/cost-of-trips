package com.nhockool1002.costoftrips.data.repository

import com.nhockool1002.costoftrips.data.export.ImportedExpense
import com.nhockool1002.costoftrips.data.export.ImportedTrip
import com.nhockool1002.costoftrips.data.local.AppDatabase
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import com.nhockool1002.costoftrips.testutil.InMemoryDatabaseFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TripRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TripRepository

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = TripRepository(
            database.tripDao(),
            database.expenseDao(),
            database.tripMemberDao(),
            database.expenseSplitDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun trip(name: String = "Trip", budget: Double? = null) =
        Trip(name = name, destination = "Somewhere", startDate = 1000L, endDate = 2000L, budget = budget)

    @Test
    fun `createTrip inserts and is returned by getAllTrips`() = runTest {
        val id = repository.createTrip(trip("Da Lat"))
        val trips = repository.getAllTrips()
        assertEquals(1, trips.size)
        assertEquals(id, trips[0].id)
        assertEquals("Da Lat", trips[0].name)
    }

    @Test
    fun `observeTrips emits inserted trips`() = runTest {
        repository.createTrip(trip("A"))
        val trips = repository.observeTrips().first()
        assertEquals(1, trips.size)
        assertEquals("A", trips[0].name)
    }

    @Test
    fun `updateTrip persists changes`() = runTest {
        val id = repository.createTrip(trip("Old name"))
        val stored = repository.getAllTrips().first()
        repository.updateTrip(stored.copy(name = "New name", budget = 100.0))

        val updated = repository.observeTrip(id).first()
        assertEquals("New name", updated?.name)
        assertEquals(100.0, updated?.budget)
    }

    @Test
    fun `deleteTrip cascades to its expenses, members and splits`() = runTest {
        val tripId = repository.createTrip(trip())
        val memberId = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val expenseId = repository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 10.0, paidByMemberId = memberId),
            splitMemberIds = listOf(memberId)
        )

        repository.deleteTrip(repository.getAllTrips().first { it.id == tripId })

        assertTrue(repository.getAllTrips().isEmpty())
        assertTrue(repository.getAllExpenses().none { it.id == expenseId })
        assertTrue(repository.getAllMembers().none { it.id == memberId })
        assertTrue(repository.getAllSplits().isEmpty())
    }

    @Test
    fun `addExpense with split members records the splits`() = runTest {
        val tripId = repository.createTrip(trip())
        val m1 = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val m2 = repository.addMember(TripMember(tripId = tripId, name = "Binh"))

        val expenseId = repository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.FOOD, amount = 50.0, paidByMemberId = m1),
            splitMemberIds = listOf(m1, m2)
        )

        val splits = repository.observeSplitsForTrip(tripId).first()
        assertEquals(setOf(m1, m2), splits.filter { it.expenseId == expenseId }.map { it.memberId }.toSet())
    }

    @Test
    fun `addExpense without split members records no splits`() = runTest {
        val tripId = repository.createTrip(trip())
        repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 5.0))
        assertTrue(repository.getAllSplits().isEmpty())
    }

    @Test
    fun `updateExpense persists changes`() = runTest {
        val tripId = repository.createTrip(trip())
        val expenseId = repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 5.0))
        val stored = repository.getAllExpenses().first { it.id == expenseId }
        repository.updateExpense(stored.copy(amount = 99.0, note = "updated"))

        val updated = repository.observeExpensesForTrip(tripId).first().first { it.id == expenseId }
        assertEquals(99.0, updated.amount, 0.0001)
        assertEquals("updated", updated.note)
    }

    @Test
    fun `deleteExpense removes it and its splits`() = runTest {
        val tripId = repository.createTrip(trip())
        val memberId = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val expenseId = repository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 5.0),
            splitMemberIds = listOf(memberId)
        )
        repository.deleteExpense(repository.getAllExpenses().first { it.id == expenseId })

        assertTrue(repository.getAllExpenses().none { it.id == expenseId })
        assertTrue(repository.getAllSplits().none { it.expenseId == expenseId })
    }

    @Test
    fun `deleteMember clears paidByMemberId on expenses that referenced it`() = runTest {
        val tripId = repository.createTrip(trip())
        val memberId = repository.addMember(TripMember(tripId = tripId, name = "An"))
        val expenseId = repository.addExpense(
            Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 5.0, paidByMemberId = memberId)
        )

        repository.deleteMember(repository.getAllMembers().first { it.id == memberId })

        val expense = repository.getAllExpenses().first { it.id == expenseId }
        assertNull(expense.paidByMemberId)
        assertTrue(repository.getAllMembers().isEmpty())
    }

    @Test
    fun `reorderTrips updates sortOrder to match the given order`() = runTest {
        val id1 = repository.createTrip(trip("First"))
        val id2 = repository.createTrip(trip("Second"))
        val id3 = repository.createTrip(trip("Third"))

        repository.reorderTrips(listOf(id3, id1, id2))

        val bySortOrder = repository.getAllTrips().sortedBy { it.sortOrder }.map { it.id }
        assertEquals(listOf(id3, id1, id2), bySortOrder)
    }

    @Test
    fun `reorderExpenses updates sortOrder to match the given order`() = runTest {
        val tripId = repository.createTrip(trip())
        val e1 = repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 1.0))
        val e2 = repository.addExpense(Expense(tripId = tripId, category = ExpenseCategory.OTHER, amount = 2.0))

        repository.reorderExpenses(listOf(e2, e1))

        val bySortOrder = repository.getAllExpenses().sortedBy { it.sortOrder }.map { it.id }
        assertEquals(listOf(e2, e1), bySortOrder)
    }

    @Test
    fun `importTrips inserts trips with fresh ids and links members and splits by name`() = runTest {
        // Mirrors what DataExporter.parseJson actually produces: id always left at its
        // default (0), never set explicitly - Room's autoGenerate only kicks in for 0/omitted
        // ids, and would otherwise insert whatever explicit id it's given verbatim.
        val imported = ImportedTrip(
            trip = Trip(name = "Imported Trip", destination = "Hue", startDate = 0L, endDate = 0L),
            memberNames = listOf("An", "Binh"),
            expenses = listOf(
                ImportedExpense(
                    expense = Expense(tripId = 0L, category = ExpenseCategory.TRANSPORT, amount = 30.0),
                    paidByName = "An",
                    splitWithNames = listOf("An", "Binh")
                )
            )
        )

        val count = repository.importTrips(listOf(imported))
        assertEquals(1, count)

        val trips = repository.getAllTrips()
        assertEquals(1, trips.size)
        val newTripId = trips[0].id
        assertTrue("import must assign an auto-generated id", newTripId > 0L)

        val members = repository.getAllMembers()
        assertEquals(setOf("An", "Binh"), members.map { it.name }.toSet())
        val anId = members.first { it.name == "An" }.id
        val binhId = members.first { it.name == "Binh" }.id

        val expenses = repository.getAllExpenses()
        assertEquals(1, expenses.size)
        assertEquals(newTripId, expenses[0].tripId)
        assertEquals(anId, expenses[0].paidByMemberId)

        val splits = repository.getAllSplits()
        assertEquals(setOf(anId, binhId), splits.map { it.memberId }.toSet())
    }

    @Test
    fun `importTrips with an unmatched paidBy name leaves paidByMemberId null`() = runTest {
        val imported = ImportedTrip(
            trip = Trip(name = "Trip", destination = "", startDate = 0L, endDate = 0L),
            memberNames = emptyList(),
            expenses = listOf(
                ImportedExpense(
                    expense = Expense(tripId = 0L, category = ExpenseCategory.OTHER, amount = 1.0),
                    paidByName = "Unknown Person",
                    splitWithNames = emptyList()
                )
            )
        )
        repository.importTrips(listOf(imported))
        assertNull(repository.getAllExpenses().first().paidByMemberId)
    }
}
