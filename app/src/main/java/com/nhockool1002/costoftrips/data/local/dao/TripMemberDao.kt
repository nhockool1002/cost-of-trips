package com.nhockool1002.costoftrips.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.nhockool1002.costoftrips.data.local.entity.TripMember
import kotlinx.coroutines.flow.Flow

@Dao
interface TripMemberDao {
    @Query("SELECT * FROM trip_members WHERE tripId = :tripId ORDER BY id ASC")
    fun observeMembersForTrip(tripId: Long): Flow<List<TripMember>>

    @Query("SELECT * FROM trip_members WHERE tripId = :tripId ORDER BY id ASC")
    suspend fun getMembersForTrip(tripId: Long): List<TripMember>

    @Query("SELECT * FROM trip_members")
    suspend fun getAllMembers(): List<TripMember>

    @Insert
    suspend fun insert(member: TripMember): Long

    @Delete
    suspend fun delete(member: TripMember)

    // paidByMemberId has no FK constraint (kept as a plain column to avoid a
    // disruptive table-rebuild migration), so it must be cleared by hand
    // before the member row disappears.
    @Query("UPDATE expenses SET paidByMemberId = NULL WHERE paidByMemberId = :memberId")
    suspend fun clearPaidByForMember(memberId: Long)

    @Transaction
    suspend fun deleteAndClear(member: TripMember) {
        clearPaidByForMember(member.id)
        delete(member)
    }
}
