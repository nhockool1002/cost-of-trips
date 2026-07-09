package com.nhockool1002.costoftrips.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val destination: String,
    val startDate: Long,
    val endDate: Long,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
