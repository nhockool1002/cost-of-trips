package com.nhockool1002.costoftrips.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nhockool1002.costoftrips.data.local.entity.ChecklistItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ChecklistItemDao {
    @Query("SELECT * FROM checklist_items WHERE tripId = :tripId ORDER BY sortOrder ASC, id ASC")
    fun observeItemsForTrip(tripId: Long): Flow<List<ChecklistItem>>

    @Query("SELECT * FROM checklist_items")
    suspend fun getAllItems(): List<ChecklistItem>

    @Insert
    suspend fun insert(item: ChecklistItem): Long

    @Update
    suspend fun update(item: ChecklistItem)

    @Delete
    suspend fun delete(item: ChecklistItem)
}
