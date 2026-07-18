package com.nhockool1002.costoftrips.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nhockool1002.costoftrips.data.local.entity.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY sortOrder ASC, startDate DESC")
    fun observeTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun observeTrip(tripId: Long): Flow<Trip?>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTrip(tripId: Long): Trip?

    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<Trip>

    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("UPDATE trips SET sortOrder = :sortOrder WHERE id = :tripId")
    suspend fun updateSortOrder(tripId: Long, sortOrder: Int)

    @Transaction
    suspend fun reorder(orderedTripIds: List<Long>) {
        orderedTripIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }
}
