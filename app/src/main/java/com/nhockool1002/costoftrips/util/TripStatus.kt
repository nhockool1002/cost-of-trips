package com.nhockool1002.costoftrips.util

enum class TripStatus { UPCOMING, ONGOING, COMPLETED }

fun tripStatus(startDate: Long, endDate: Long, now: Long = System.currentTimeMillis()): TripStatus = when {
    now < startDate -> TripStatus.UPCOMING
    now > endDate -> TripStatus.COMPLETED
    else -> TripStatus.ONGOING
}
