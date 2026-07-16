package com.nhockool1002.costoftrips.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TripStatusTest {

    @Test
    fun `now before start is upcoming`() {
        assertEquals(TripStatus.UPCOMING, tripStatus(startDate = 1000L, endDate = 2000L, now = 500L))
    }

    @Test
    fun `now after end is completed`() {
        assertEquals(TripStatus.COMPLETED, tripStatus(startDate = 1000L, endDate = 2000L, now = 2500L))
    }

    @Test
    fun `now between start and end is ongoing`() {
        assertEquals(TripStatus.ONGOING, tripStatus(startDate = 1000L, endDate = 2000L, now = 1500L))
    }

    @Test
    fun `now exactly at start is ongoing`() {
        assertEquals(TripStatus.ONGOING, tripStatus(startDate = 1000L, endDate = 2000L, now = 1000L))
    }

    @Test
    fun `now exactly at end is ongoing`() {
        assertEquals(TripStatus.ONGOING, tripStatus(startDate = 1000L, endDate = 2000L, now = 2000L))
    }

    @Test
    fun `single-day trip is ongoing at that instant`() {
        assertEquals(TripStatus.ONGOING, tripStatus(startDate = 1000L, endDate = 1000L, now = 1000L))
    }
}
