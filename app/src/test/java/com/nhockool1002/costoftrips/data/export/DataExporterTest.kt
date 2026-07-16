package com.nhockool1002.costoftrips.data.export

import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.ExpenseSplitMember
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises only the pure JSON logic ([DataExporter.buildJson] / [DataExporter.parseJson]);
 * [DataExporter.exportToDownloads] touches Android's ContentResolver/MediaStore and isn't
 * covered by a plain JVM unit test.
 */
class DataExporterTest {

    @Test
    fun `buildJson then parseJson round-trips a full trip`() {
        val trip = Trip(id = 1L, name = "Da Lat", destination = "Da Lat", startDate = 1000L, endDate = 2000L, note = "fun trip", budget = 5_000_000.0)
        val member1 = TripMember(id = 10L, tripId = 1L, name = "An")
        val member2 = TripMember(id = 11L, tripId = 1L, name = "Binh")
        val expense = Expense(
            id = 100L, tripId = 1L, category = ExpenseCategory.FOOD, amount = 200_000.0,
            note = "pho", date = 1500L, paidByMemberId = 10L
        )
        val splits = listOf(ExpenseSplitMember(100L, 10L), ExpenseSplitMember(100L, 11L))

        val json = DataExporter.buildJson(listOf(trip), listOf(expense), listOf(member1, member2), splits)
        val imported = DataExporter.parseJson(json)

        assertEquals(1, imported.size)
        val importedTrip = imported[0]
        assertEquals("Da Lat", importedTrip.trip.name)
        assertEquals(5_000_000.0, importedTrip.trip.budget)
        assertEquals(listOf("An", "Binh"), importedTrip.memberNames)
        assertEquals(1, importedTrip.expenses.size)

        val importedExpense = importedTrip.expenses[0]
        assertEquals(ExpenseCategory.FOOD, importedExpense.expense.category)
        assertEquals(200_000.0, importedExpense.expense.amount, 0.0001)
        assertEquals("An", importedExpense.paidByName)
        assertEquals(setOf("An", "Binh"), importedExpense.splitWithNames.toSet())
    }

    @Test
    fun `buildJson omits budget when null`() {
        val trip = Trip(id = 1L, name = "No Budget Trip", destination = "", startDate = 0L, endDate = 0L)
        val json = DataExporter.buildJson(listOf(trip), emptyList(), emptyList(), emptyList())
        val tripJson = JSONObject(json).getJSONArray("trips").getJSONObject(0)
        assertTrue(!tripJson.has("budget"))
    }

    @Test
    fun `parseJson defaults budget to null when absent`() {
        val trip = Trip(id = 1L, name = "No Budget Trip", destination = "", startDate = 0L, endDate = 0L, budget = null)
        val json = DataExporter.buildJson(listOf(trip), emptyList(), emptyList(), emptyList())
        val imported = DataExporter.parseJson(json)
        assertNull(imported[0].trip.budget)
    }

    @Test
    fun `parseJson defaults an unknown category to OTHER`() {
        val json = """
            {"trips":[{"name":"Trip","destination":"","startDate":0,"endDate":0,"members":[],
            "expenses":[{"id":1,"category":"NOT_A_REAL_CATEGORY","amount":10.0,"date":0}]}]}
        """.trimIndent()
        val imported = DataExporter.parseJson(json)
        assertEquals(ExpenseCategory.OTHER, imported[0].expenses[0].expense.category)
    }

    @Test
    fun `parseJson tolerates missing optional fields`() {
        val json = """
            {"trips":[{"name":"Trip","startDate":0,"endDate":0,
            "expenses":[{"category":"FOOD","amount":15.0}]}]}
        """.trimIndent()
        val imported = DataExporter.parseJson(json)
        val trip = imported[0]
        assertEquals("Trip", trip.trip.name)
        assertEquals("", trip.trip.destination)
        assertEquals(emptyList<String>(), trip.memberNames)
        assertNull(trip.expenses[0].paidByName)
        assertEquals(emptyList<String>(), trip.expenses[0].splitWithNames)
    }

    @Test
    fun `parseJson with no trips array elements returns an empty list`() {
        val imported = DataExporter.parseJson("""{"trips":[]}""")
        assertTrue(imported.isEmpty())
    }

    @Test
    fun `an expense with no split members omits splitWith from the JSON`() {
        val trip = Trip(id = 1L, name = "Trip", destination = "", startDate = 0L, endDate = 0L)
        val expense = Expense(id = 1L, tripId = 1L, category = ExpenseCategory.OTHER, amount = 5.0)
        val json = DataExporter.buildJson(listOf(trip), listOf(expense), emptyList(), emptyList())
        val expenseJson = JSONObject(json).getJSONArray("trips").getJSONObject(0).getJSONArray("expenses").getJSONObject(0)
        assertTrue(!expenseJson.has("splitWith"))
        assertTrue(!expenseJson.has("paidBy"))
    }
}
