package com.nhockool1002.costoftrips.data.repository

import com.nhockool1002.costoftrips.data.export.ImportedTrip
import com.nhockool1002.costoftrips.data.local.dao.ExpenseDao
import com.nhockool1002.costoftrips.data.local.dao.ExpenseSplitDao
import com.nhockool1002.costoftrips.data.local.dao.TripDao
import com.nhockool1002.costoftrips.data.local.dao.TripMemberDao
import com.nhockool1002.costoftrips.data.local.entity.Expense
import com.nhockool1002.costoftrips.data.local.entity.ExpenseSplitMember
import com.nhockool1002.costoftrips.data.local.entity.Trip
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
    private val expenseDao: ExpenseDao,
    private val tripMemberDao: TripMemberDao,
    private val expenseSplitDao: ExpenseSplitDao
) {
    fun observeTrips(): Flow<List<Trip>> = tripDao.observeTrips()

    fun observeTrip(tripId: Long): Flow<Trip?> = tripDao.observeTrip(tripId)

    fun observeExpensesForTrip(tripId: Long): Flow<List<Expense>> =
        expenseDao.observeExpensesForTrip(tripId)

    fun observeAllExpenses(): Flow<List<Expense>> = expenseDao.observeAllExpenses()

    fun observeMembersForTrip(tripId: Long): Flow<List<TripMember>> =
        tripMemberDao.observeMembersForTrip(tripId)

    fun observeSplitsForTrip(tripId: Long): Flow<List<ExpenseSplitMember>> =
        expenseSplitDao.observeSplitsForTrip(tripId)

    suspend fun createTrip(trip: Trip): Long = tripDao.insert(trip)

    suspend fun updateTrip(trip: Trip) = tripDao.update(trip)

    suspend fun deleteTrip(trip: Trip) = tripDao.delete(trip)

    suspend fun addExpense(expense: Expense, splitMemberIds: List<Long> = emptyList()): Long {
        val expenseId = expenseDao.insert(expense)
        if (splitMemberIds.isNotEmpty()) {
            expenseSplitDao.replaceSplitsForExpense(expenseId, splitMemberIds)
        }
        return expenseId
    }

    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun addMember(member: TripMember): Long = tripMemberDao.insert(member)

    suspend fun deleteMember(member: TripMember) = tripMemberDao.deleteAndClear(member)

    suspend fun getAllTrips(): List<Trip> = tripDao.getAllTrips()

    suspend fun getAllExpenses(): List<Expense> = expenseDao.getAllExpenses()

    suspend fun getAllMembers(): List<TripMember> = tripMemberDao.getAllMembers()

    suspend fun getAllSplits(): List<ExpenseSplitMember> = expenseSplitDao.getAllSplits()

    suspend fun reorderTrips(orderedTripIds: List<Long>) = tripDao.reorder(orderedTripIds)

    suspend fun reorderExpenses(orderedExpenseIds: List<Long>) = expenseDao.reorder(orderedExpenseIds)

    suspend fun importTrips(importedTrips: List<ImportedTrip>): Int {
        importedTrips.forEach { importedTrip ->
            val tripId = tripDao.insert(importedTrip.trip)
            val memberIdByName = importedTrip.memberNames.associateWith { name ->
                tripMemberDao.insert(TripMember(tripId = tripId, name = name))
            }
            importedTrip.expenses.forEach { importedExpense ->
                val expenseId = expenseDao.insert(
                    importedExpense.expense.copy(
                        tripId = tripId,
                        paidByMemberId = importedExpense.paidByName?.let { memberIdByName[it] }
                    )
                )
                val splitIds = importedExpense.splitWithNames.mapNotNull { memberIdByName[it] }
                if (splitIds.isNotEmpty()) {
                    expenseSplitDao.replaceSplitsForExpense(expenseId, splitIds)
                }
            }
        }
        return importedTrips.size
    }
}
