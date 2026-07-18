package com.nhockool1002.costoftrips

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nhockool1002.costoftrips.data.preferences.AppCurrency
import com.nhockool1002.costoftrips.data.preferences.ThemeMode
import com.nhockool1002.costoftrips.ui.navigation.AppBottomBar
import com.nhockool1002.costoftrips.ui.navigation.CostOfTripsNavHost
import com.nhockool1002.costoftrips.ui.navigation.Screen
import com.nhockool1002.costoftrips.ui.theme.CostOfTripsTheme
import com.nhockool1002.costoftrips.util.LocalCurrency

// AppCompatActivity (not plain ComponentActivity) is required here: only
// AppCompatActivity registers itself with AppCompatDelegate so that calling
// setApplicationLocales() can recreate this activity immediately on API < 33.
// With a plain ComponentActivity the locale change silently has no visible
// effect until the process is killed and relaunched. AppCompatActivity also
// extends FragmentActivity, which BiometricPrompt requires for the app lock.
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
    val currency by app.userPreferencesRepository.currency.collectAsState(initial = AppCurrency.VND)
    val appLockEnabled by app.userPreferencesRepository.appLockEnabled.collectAsState(initial = false)

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
        CompositionLocalProvider(LocalCurrency provides currency) {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (appLockEnabled) {
                    AppLockGate { MainNavigation() }
                } else {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
private fun MainNavigation() {
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

/**
 * Gates [content] behind a biometric/PIN check. Unlocking only lasts for the current
 * foreground session — backgrounding the app (ON_STOP) resets it, so the prompt reappears
 * next time the app is brought back, not just on cold start.
 */
@Composable
private fun AppLockGate(content: @Composable () -> Unit) {
    var isUnlocked by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) isUnlocked = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (isUnlocked) {
        content()
    } else {
        LockedScreen(onUnlocked = { isUnlocked = true })
    }
}

@Composable
private fun LockedScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val activity = context as AppCompatActivity
    val promptTitle = stringResource(R.string.applock_prompt_title)

    fun showPrompt() {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onUnlocked()
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    LaunchedEffect(Unit) { showPrompt() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔒", style = MaterialTheme.typography.displayMedium)
        Text(
            stringResource(R.string.applock_locked_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            stringResource(R.string.applock_locked_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Button(onClick = { showPrompt() }, modifier = Modifier.padding(top = 24.dp)) {
            Text(stringResource(R.string.applock_unlock_button))
        }
    }
}
