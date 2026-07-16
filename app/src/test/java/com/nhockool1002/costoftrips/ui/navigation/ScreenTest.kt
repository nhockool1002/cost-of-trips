package com.nhockool1002.costoftrips.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTest {

    @Test
    fun `static routes have their expected literal route`() {
        assertEquals("splash", Screen.Splash.route)
        assertEquals("trip_list", Screen.TripList.route)
        assertEquals("statistics", Screen.Statistics.route)
        assertEquals("create_trip", Screen.CreateTrip.route)
        assertEquals("settings", Screen.Settings.route)
        assertEquals("about", Screen.About.route)
    }

    @Test
    fun `TripDetail createRoute embeds the trip id`() {
        assertEquals("trip_detail/42", Screen.TripDetail.createRoute(42L))
    }

    @Test
    fun `AddExpense createRoute embeds the trip id`() {
        assertEquals("add_expense/7", Screen.AddExpense.createRoute(7L))
    }
}
