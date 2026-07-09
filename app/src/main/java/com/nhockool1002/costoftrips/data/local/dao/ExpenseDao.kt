package com.nhockool1002.costoftrips.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nhockool1002.costoftrips.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY sortOrder ASC, date DESC")
    fun observeExpensesForTrip(tripId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    fun observeAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("UPDATE expenses SET sortOrder = :sortOrder WHERE id = :expenseId")
    suspend fun updateSortOrder(expenseId: Long, sortOrder: Int)

    @Transaction
    suspend fun reorder(orderedExpenseIds: List<Long>) {
        orderedExpenseIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }
}
