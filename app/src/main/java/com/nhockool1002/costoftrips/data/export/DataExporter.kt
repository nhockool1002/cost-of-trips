package com.nhockool1002.costoftrips.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseCategory
import com.nhockool1002.costoftrips.data.local.entity.ExpenseSplitMember
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// A parsed expense paired with the group-split members it references by name
// rather than id, since member ids are re-assigned on import (see ImportedTrip).
data class ImportedExpense(val expense: Expense, val paidByName: String?, val splitWithNames: List<String>)

// A parsed trip with its members and expenses, without ids: importing always
// inserts fresh rows (via Room's autoGenerate) rather than reusing the
// exported ids, since those may collide with rows already in the database.
// Members are carried by name so they can be re-linked to their freshly
// generated ids once inserted.
data class ImportedTrip(val trip: Trip, val memberNames: List<String>, val expenses: List<ImportedExpense>)

object DataExporter {

    fun buildJson(
        trips: List<Trip>,
        expenses: List<Expense>,
        members: List<TripMember>,
        splits: List<ExpenseSplitMember>
    ): String {
        val expensesByTrip = expenses.groupBy { it.tripId }
        val membersByTrip = members.groupBy { it.tripId }
        val splitMemberIdsByExpense = splits.groupBy({ it.expenseId }, { it.memberId })
        val tripsArray = JSONArray()
        trips.forEach { trip ->
            val tripMembers = membersByTrip[trip.id].orEmpty()
            val memberNameById = tripMembers.associate { it.id to it.name }

            val membersArray = JSONArray()
            tripMembers.forEach { membersArray.put(it.name) }

            val expensesArray = JSONArray()
            expensesByTrip[trip.id].orEmpty().forEach { expense ->
                expensesArray.put(
                    JSONObject().apply {
                        put("id", expense.id)
                        put("category", expense.category.name)
                        put("amount", expense.amount)
                        put("note", expense.note)
                        put("date", expense.date)
                        expense.paidByMemberId?.let { memberNameById[it] }?.let { put("paidBy", it) }
                        val splitWithArray = JSONArray()
                        splitMemberIdsByExpense[expense.id].orEmpty()
                            .mapNotNull { memberNameById[it] }
                            .forEach { splitWithArray.put(it) }
                        if (splitWithArray.length() > 0) put("splitWith", splitWithArray)
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
                    trip.budget?.let { put("budget", it) }
                    put("members", membersArray)
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
                note = tripJson.optString("note"),
                budget = if (tripJson.has("budget")) tripJson.optDouble("budget") else null
            )
            val membersArray = tripJson.optJSONArray("members") ?: JSONArray()
            val memberNames = (0 until membersArray.length()).map { membersArray.getString(it) }

            val expensesArray = tripJson.optJSONArray("expenses") ?: JSONArray()
            val expenses = (0 until expensesArray.length()).map { j ->
                val expenseJson = expensesArray.getJSONObject(j)
                val category = try {
                    ExpenseCategory.valueOf(expenseJson.getString("category"))
                } catch (e: IllegalArgumentException) {
                    ExpenseCategory.OTHER
                }
                val expense = Expense(
                    tripId = 0,
                    category = category,
                    amount = expenseJson.getDouble("amount"),
                    note = expenseJson.optString("note"),
                    date = expenseJson.optLong("date", System.currentTimeMillis())
                )
                val paidByName = if (expenseJson.has("paidBy")) expenseJson.optString("paidBy") else null
                val splitWithArray = expenseJson.optJSONArray("splitWith") ?: JSONArray()
                val splitWithNames = (0 until splitWithArray.length()).map { splitWithArray.getString(it) }
                ImportedExpense(expense, paidByName, splitWithNames)
            }
            ImportedTrip(trip, memberNames, expenses)
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
