package com.synq.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.synq.app.ui.HomeScreen
import com.synq.app.ui.theme.SynqTheme
import com.synq.app.viewmodel.SynqViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SynqTheme {
                val viewModel: SynqViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val clockTick by viewModel.clockTick.collectAsStateWithLifecycle()

                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                ) { granted ->
                    if (granted.values.any { it }) viewModel.locate()
                }

                // Track whether the health connect launcher is currently active so we can
                // detect when the system silently drops the permission request (common on
                // Android 15 real devices where the status bar blinks but no dialog appears).
                var healthConnectLauncherActive by remember { mutableStateOf(false) }

                val healthConnectLauncher = rememberLauncherForActivityResult(
                    contract = viewModel.healthConnectPermissionContract(),
                ) { granted ->
                    healthConnectLauncherActive = false
                    Log.d(TAG, "healthConnectLauncher callback: granted=$granted")
                    viewModel.onHealthConnectPermissionResult(granted)
                    // If the standard sheet didn't actually grant everything (which can
                    // silently happen for sideloaded apps), open Health Connect's own
                    // permission screen so the user can still grant access manually.
                    if (!granted.containsAll(viewModel.healthConnectPermissions)) {
                        Log.d(TAG, "healthConnectLauncher: incomplete grants, opening settings fallback")
                        viewModel.openPermissionSettings()
                    }
                }

                // If the launcher was invoked but the dialog never appeared (status bar
                // blinked on Android 15), the callback may never fire or fire after a delay.
                // This effect watches for the state staying NeedsPermission for >4 seconds
                // after launch and automatically opens the fallback settings screen.
                LaunchedEffect(healthConnectLauncherActive) {
                    if (healthConnectLauncherActive) {
                        delay(4_000)
                        // If still in NeedsPermission after 4s, the dialog likely failed
                        if (healthConnectLauncherActive) {
                            Log.w(TAG, "healthConnectLauncher: dialog likely failed (4s timeout), opening settings")
                            healthConnectLauncherActive = false
                            viewModel.openPermissionSettings()
                        }
                    }
                }

                HomeScreen(
                    state = uiState,
                    clockTick = clockTick,
                    onLocate = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                            )
                        )
                    },
                    onRefresh = viewModel::refresh,
                    onCityQueryChange = viewModel::onCitySearchQueryChange,
                    onSearchCity = { viewModel.searchCity(it) },
                    onConnectHealthConnect = {
                        Log.d(TAG, "onConnectHealthConnect: launching permission request")
                        healthConnectLauncherActive = true
                        healthConnectLauncher.launch(viewModel.healthConnectPermissions)
                    },
                    onInstallHealthConnect = viewModel::installHealthConnect,
                    onUseDemoData = viewModel::useDemoData,
                    onSyncHealthConnect = viewModel::syncHealthConnect,
                    onDisconnectHealthConnect = viewModel::disconnectHealthConnect,
                    onLogHydration = viewModel::logHydration,
                    onSaveManualSleep = viewModel::saveManualSleep,
                    onToggleBaseline = viewModel::toggleBaseline,
                    onBaselineSleepChange = viewModel::setBaselineSleep,
                    onBaselineRhrChange = viewModel::setBaselineRhr,
                )

                LaunchedEffect(Unit) {
                    viewModel.checkHealthConnectAvailability()
                }

                // Re-check Health Connect each time the app resumes — e.g. after the user
                // installs/updates the provider from the Play Store or grants permissions in
                // the Health Connect app. This keeps the UI in sync with what they just did.
                LifecycleResumeEffect(Unit) {
                    viewModel.checkHealthConnectAvailability()
                    onPauseOrDispose { }
                }
            }
        }
    }
}
