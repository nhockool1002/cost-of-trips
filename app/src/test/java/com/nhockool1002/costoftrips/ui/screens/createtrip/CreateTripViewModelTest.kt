package com.nhockool1002.costoftrips.ui.screens.createtrip

import com.nhockool1002.costoftrips.data.local.AppDatabase
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
class CreateTripViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: TripRepository
    private lateinit var viewModel: CreateTripViewModel

    @Before
    fun setUp() {
        database = InMemoryDatabaseFactory.create()
        repository = TripRepository(database.tripDao(), database.expenseDao(), database.tripMemberDao(), database.expenseSplitDao())
        viewModel = CreateTripViewModel(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `createTrip trims strings and persists the trip`() = runTest(mainDispatcherRule.testDispatcher) {
        var saved = false
        viewModel.createTrip(
            name = "  Da Lat  ",
            destination = "  Da Lat City  ",
            startDate = 1000L,
            endDate = 2000L,
            note = "  fun  ",
            budget = 100.0,
            onSaved = { saved = true }
        )

        val trips = repository.observeTrips().first { it.isNotEmpty() }
        assertEquals("Da Lat", trips[0].name)
        assertEquals("Da Lat City", trips[0].destination)
        assertEquals("fun", trips[0].note)
        assertEquals(100.0, trips[0].budget)
        assertTrue(saved)
    }

    @Test
    fun `createTrip ignores a blank name`() = runTest(mainDispatcherRule.testDispatcher) {
        var saved = false
        viewModel.createTrip("   ", "Dest", 0L, 0L, "", null) { saved = true }
        assertTrue(repository.getAllTrips().isEmpty())
        assertTrue(!saved)
    }
}
