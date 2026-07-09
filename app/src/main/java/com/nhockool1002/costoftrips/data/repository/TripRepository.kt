package com.nhockool1002.costoftrips.data.repository

import com.nhockool1002.costoftrips.data.local.dao.ExpenseDao
import com.nhockool1002.costoftrips.data.local.dao.TripDao
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.Trip
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val expenseDao: ExpenseDao
) {
    fun observeTrips(): Flow<List<Trip>> = tripDao.observeTrips()

    fun observeTrip(tripId: Long): Flow<Trip?> = tripDao.observeTrip(tripId)

    fun observeExpensesForTrip(tripId: Long): Flow<List<Expense>> =
        expenseDao.observeExpensesForTrip(tripId)

    fun observeAllExpenses(): Flow<List<Expense>> = expenseDao.observeAllExpenses()

    suspend fun createTrip(trip: Trip): Long = tripDao.insert(trip)

    suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    suspend fun deleteTrip(trip: Trip) = tripDao.delete(trip)

    suspend fun addExpense(expense: Expense): Long = expenseDao.insert(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun getAllTrips(): List<Trip> = tripDao.getAllTrips()

    suspend fun getAllExpenses(): List<Expense> = expenseDao.getAllExpenses()

    suspend fun reorderTrips(orderedTripIds: List<Long>) = tripDao.reorder(orderedTripIds)

    suspend fun reorderExpenses(orderedExpenseIds: List<Long>) = expenseDao.reorder(orderedExpenseIds)
}
