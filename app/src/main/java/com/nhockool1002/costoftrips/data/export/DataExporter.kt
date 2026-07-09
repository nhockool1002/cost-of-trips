package com.nhockool1002.costoftrips.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.Trip
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
