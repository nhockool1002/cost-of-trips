package com.nhockool1002.costoftrips

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.ui.navigation.AppBottomBar
import com.nhockool1002.costoftrips.ui.navigation.CostOfTripsNavHost
import com.nhockool1002.costoftrips.ui.navigation.Screen
import com.nhockool1002.costoftrips.ui.theme.CostOfTripsTheme

// AppCompatActivity (not plain ComponentActivity) is required here: only
// AppCompatActivity registers itself with AppCompatDelegate so that calling
// setApplicationLocales() can recreate this activity immediately on API < 33.
// With a plain ComponentActivity the locale change silently has no visible
// effect until the process is killed and relaunched.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CostOfTripsRoot()
        }
    }
}

@Composable
fun CostOfTripsRoot() {
    val app = LocalContext.current.applicationContext as CostOfTripsApp
    val themeMode by app.userPreferencesRepository.themeMode.collectAsState(initial = ThemeMode.DARK)

    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val view = LocalView.current
    val activity = LocalContext.current as AppCompatActivity
    SideEffect {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }

    CostOfTripsTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            val showBottomBar = currentRoute == Screen.TripList.route || currentRoute == Screen.Statistics.route

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        AppBottomBar(navController = navController, currentRoute = currentRoute)
                    }
                }
            ) { innerPadding ->
                CostOfTripsNavHost(
                    navController = navController,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
