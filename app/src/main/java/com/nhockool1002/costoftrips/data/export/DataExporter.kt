package com.nhockool1002.costoftrips.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.Trip
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A parsed trip paired with its expenses, without ids: importing always
// inserts fresh rows (via Room's autoGenerate) rather than reusing the
// exported ids, since those may collide with trips already in the database.
data class ImportedTrip(val trip: Trip, val expenses: List<Expense>)

object DataExporter {

    fun buildJson(trips: List<Trip>, expenses: List<Expense>): String {
        val expensesByTrip = expenses.groupBy { it.tripId }
        val tripsArray = JSONArray()
        trips.forEach { trip ->
            val expensesArray = JSONArray()
            expensesByTrip[trip.id].orEmpty().forEach { expense ->
                expensesArray.put(
                    JSONObject().apply {
                        put("id", expense.id)
                        put("category", expense.category.name)
                        put("amount", expense.amount)
                        put("note", expense.note)
                        put("date", expense.date)
                    }
                )
            }
            tripsArray.put(
                JSONObject().apply {
                    put("id", trip.id)
                    put("name", trip.name)
                    put("destination", trip.destination)
                    put("startDate", trip.startDate)
                    put("endDate", trip.endDate)
                    put("note", trip.note)
                    put("expenses", expensesArray)
                }
            )
        }
        return JSONObject().apply { put("trips", tripsArray) }.toString(2)
    }

    fun parseJson(json: String): List<ImportedTrip> {
        val root = JSONObject(json)
        val tripsArray = root.getJSONArray("trips")
        return (0 until tripsArray.length()).map { i ->
            val tripJson = tripsArray.getJSONObject(i)
            val trip = Trip(
                name = tripJson.getString("name"),
                destination = tripJson.optString("destination"),
                startDate = tripJson.getLong("startDate"),
                endDate = tripJson.getLong("endDate"),
                note = tripJson.optString("note")
            )
            val expensesArray = tripJson.optJSONArray("expenses") ?: JSONArray()
            val expenses = (0 until expensesArray.length()).map { j ->
                val expenseJson = expensesArray.getJSONObject(j)
                val category = try {
                    ExpenseCategory.valueOf(expenseJson.getString("category"))
                } catch (e: IllegalArgumentException) {
                    ExpenseCategory.OTHER
                }
                Expense(
                    tripId = 0,
                    category = category,
                    amount = expenseJson.getDouble("amount"),
                    note = expenseJson.optString("note"),
                    date = expenseJson.optLong("date", System.currentTimeMillis())
                )
            }
            ImportedTrip(trip, expenses)
        }
    }

    fun exportToDownloads(context: Context, json: String): Uri? {
        val fileName = "cost_of_trips_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.also {
                context.contentResolver.openOutputStream(it)?.use { stream ->
                    stream.write(json.toByteArray())
                }
            }
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(json)
            Uri.fromFile(file)
        }
    }
}
