package com.nhockool1002.costoftrips

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.compose.rememberNavController
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.ui.navigation.CostOfTripsNavHost
import com.nhockool1002.costoftrips.ui.theme.CostOfTripsTheme

class MainActivity : ComponentActivity() {
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
    val activity = LocalContext.current as ComponentActivity
    SideEffect {
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme
    }

    CostOfTripsTheme(darkTheme = darkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            CostOfTripsNavHost(navController = navController)
        }
    }
}
