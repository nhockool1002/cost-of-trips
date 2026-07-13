package com.nhockool1002.costoftrips.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nhockool1002.costoftrips.ui.screens.about.AboutScreen
import com.nhockool1002.costoftrips.ui.screens.addexpense.AddExpenseScreen
import com.nhockool1002.costoftrips.ui.screens.createtrip.CreateTripScreen
import com.nhockool1002.costoftrips.ui.screens.settings.SettingsScreen
import com.nhockool1002.costoftrips.ui.screens.splash.SplashScreen
import com.nhockool1002.costoftrips.ui.screens.statistics.StatisticsScreen
import com.nhockool1002.costoftrips.ui.screens.tripdetail.TripDetailScreen
import com.nhockool1002.costoftrips.ui.screens.triplist.TripListScreen

@Composable
fun CostOfTripsNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.Splash.route, modifier = modifier) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Screen.TripList.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.TripList.route) {
            TripListScreen(
                onTripClick = { tripId -> navController.navigate(Screen.TripDetail.createRoute(tripId)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }
        composable(Screen.CreateTrip.route) {
            CreateTripScreen(
                onTripCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.TripDetail.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            TripDetailScreen(
                tripId = tripId,
                onBack = { navController.popBackStack() },
                onAddExpenseClick = { navController.navigate(Screen.AddExpense.createRoute(tripId)) }
            )
        }
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: 0L
            AddExpenseScreen(
                tripId = tripId,
                onExpenseAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAboutClick = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.About.route) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
