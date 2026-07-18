package com.nhockool1002.costoftrips

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private lateinit var updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var updateReady by mutableStateOf(false)

    // IMMEDIATE updates block the user with Play's own full-screen UI until they update,
    // which is what "require the update" means here. FLEXIBLE is only a fallback for the
    // rare case where Play doesn't allow an immediate update for this release; that one
    // downloads in the background and needs an explicit restart, surfaced via the banner
    // driven by updateReady below.
    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            updateReady = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        updateResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // No-op: onResume() re-checks appUpdateInfo and resumes a stalled/cancelled
            // immediate update automatically, so nothing needs to happen here.
        }

        setContent {
            CostOfTripsRoot(
                showUpdateReadyBanner = updateReady,
                onRestartToUpdate = { appUpdateManager.completeUpdate() }
            )
        }

        appUpdateManager.registerListener(installStateListener)
        checkForAppUpdate()
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            when {
                info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                    startUpdateFlow(info)
                info.installStatus() == InstallStatus.DOWNLOADED -> updateReady = true
            }
        }
    }

    override fun onDestroy() {
        appUpdateManager.unregisterListener(installStateListener)
        super.onDestroy()
    }

    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                startUpdateFlow(info)
            }
        }
    }

    private fun startUpdateFlow(info: AppUpdateInfo) {
        val type = when {
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> AppUpdateType.IMMEDIATE
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> AppUpdateType.FLEXIBLE
            else -> return
        }
        appUpdateManager.startUpdateFlowForResult(
            info,
            updateResultLauncher,
            AppUpdateOptions.newBuilder(type).build()
        )
    }
}

@Composable
fun CostOfTripsRoot(
    showUpdateReadyBanner: Boolean = false,
    onRestartToUpdate: () -> Unit = {}
) {
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
                val content: @Composable () -> Unit = {
                    MainNavigation(
                        showUpdateReadyBanner = showUpdateReadyBanner,
                        onRestartToUpdate = onRestartToUpdate
                    )
                }
                if (appLockEnabled) {
                    AppLockGate(content = content)
                } else {
                    content()
                }
            }
        }
    }
}

@Composable
private fun MainNavigation(
    showUpdateReadyBanner: Boolean,
    onRestartToUpdate: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == Screen.TripList.route || currentRoute == Screen.Statistics.route

    val snackbarHostState = remember { SnackbarHostState() }
    val updateReadyMessage = stringResource(R.string.update_ready_message)
    val updateReadyAction = stringResource(R.string.update_ready_restart)
    LaunchedEffect(showUpdateReadyBanner) {
        if (showUpdateReadyBanner) {
            val result = snackbarHostState.showSnackbar(
                message = updateReadyMessage,
                actionLabel = updateReadyAction,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRestartToUpdate()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
