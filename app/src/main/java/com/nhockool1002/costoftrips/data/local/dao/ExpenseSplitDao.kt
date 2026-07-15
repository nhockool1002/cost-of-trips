package com.nhockool1002.costoftrips.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nhockool1002.costoftrips.data.local.entity.ExpenseSplitMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseSplitDao {
    @Insert
    suspend fun insertAll(splits: List<ExpenseSplitMember>)

    @Query("DELETE FROM expense_split_members WHERE expenseId = :expenseId")
    suspend fun deleteForExpense(expenseId: Long)

    @Query("SELECT * FROM expense_split_members WHERE expenseId IN (SELECT id FROM expenses WHERE tripId = :tripId)")
    fun observeSplitsForTrip(tripId: Long): Flow<List<ExpenseSplitMember>>

    @Query("SELECT * FROM expense_split_members")
    suspend fun getAllSplits(): List<ExpenseSplitMember>

    @Transaction
    suspend fun replaceSplitsForExpense(expenseId: Long, memberIds: List<Long>) {
        deleteForExpense(expenseId)
        if (memberIds.isNotEmpty()) {
            insertAll(memberIds.map { ExpenseSplitMember(expenseId, it) })
        }
    }
}
