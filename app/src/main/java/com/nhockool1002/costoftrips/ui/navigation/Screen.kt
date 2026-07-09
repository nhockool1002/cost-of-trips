package com.nhockool1002.costoftrips.ui.navigation

sealed class Screen(val route: String) {
    data object TripList : Screen("trip_list")
    data object CreateTrip : Screen("create_trip")
    data object Settings : Screen("settings")

    data object TripDetail : Screen("trip_detail/{tripId}") {
        fun createRoute(tripId: Long) = "trip_detail/$tripId"
    }

    data object AddExpense : Screen("add_expense/{tripId}") {
        fun createRoute(tripId: Long) = "add_expense/$tripId"
    }
}
