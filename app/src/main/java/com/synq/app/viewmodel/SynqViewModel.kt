package com.synq.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.synq.app.data.health.HealthConnectManager
import com.synq.app.data.health.HealthConnectProviderStatus
import com.synq.app.data.location.LocationProvider
import com.synq.app.data.location.LocationResult
import com.synq.app.data.risk.MlRiskClient
import com.synq.app.data.risk.RiskEngine
import com.synq.app.data.risk.RiskInputs
import com.synq.app.data.risk.RiskResult
import com.synq.app.data.weather.CitySearchResult
import com.synq.app.data.weather.ConditionsResult
import com.synq.app.data.weather.WeatherRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class SynqViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        private const val TAG = "SynqViewModel"
    }

    private val weatherRepository = WeatherRepository()
    private val healthConnectManager = HealthConnectManager(application)
    private val locationProvider = LocationProvider(application)

    private val _uiState = MutableStateFlow(SynqUiState())
    val uiState: StateFlow<SynqUiState> = _uiState.asStateFlow()

    /** Ticks periodically so relative "synced Xm ago" labels stay fresh without a full refetch. */
    private val _clockTick = MutableStateFlow(0L)
    val clockTick: StateFlow<Long> = _clockTick.asStateFlow()

    private var lastCoords: Pair<Double, Double>? = null

    val healthConnectPermissions: Set<String> get() = healthConnectManager.permissions
    fun healthConnectPermissionContract() = healthConnectManager.requestPermissionContract()

    init {
        checkHealthConnectAvailability()
        locate()
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                _clockTick.update { it + 1 }
            }
        }
        // Re-sync Health Connect every minute while connected so steps, hydration, and
        // sleep/heart-rate data stay fresh (the user wants updates every minute). Skipped
        // while the user is in demo mode so it doesn't overwrite their simulated data.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                val st = _uiState.value
                val realConnected = (st.healthStatus as? HealthConnectUiStatus.Connected)?.isDemo == false
                if (realConnected && !st.syncingHealthConnect) {
                    syncHealthConnect(silent = true)
                }
            }
        }
    }

    // ---------------- Environment / weather ----------------

    fun locate() {
        viewModelScope.launch {
            _uiState.update { it.copy(envStatus = EnvStatus.LOCATING, envError = null) }
            when (val result = locationProvider.getCurrentLocation()) {
                is LocationResult.Success -> {
                    lastCoords = result.lat to result.lon
                    fetchConditions(result.lat, result.lon, null)
                }
                LocationResult.PermissionDenied -> {
                    _uiState.update {
                        it.copy(
                            envStatus = EnvStatus.ERROR,
                            envError = "Location permission was denied — search for a city below instead."
                        )
                    }
                }
                LocationResult.Unavailable -> {
                    _uiState.update {
                        it.copy(
                            envStatus = EnvStatus.ERROR,
                            envError = "Location access was blocked or unavailable here — search for a city below instead."
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        val coords = lastCoords ?: return
        val label = _uiState.value.locationLabel
        viewModelScope.launch { fetchConditions(coords.first, coords.second, label) }
    }

    fun onCitySearchQueryChange(query: String) {
        _uiState.update { it.copy(citySearchQuery = query) }
    }

    fun searchCity(query: String = _uiState.value.citySearchQuery) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(envStatus = EnvStatus.LOADING, envError = null) }
            when (val result = weatherRepository.searchCity(query)) {
                is CitySearchResult.Success -> {
                    lastCoords = result.lat to result.lon
                    fetchConditions(result.lat, result.lon, result.label)
                }
                is CitySearchResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            envStatus = EnvStatus.ERROR,
                            envError = "Couldn't find \"${result.query}\". Try a different spelling or a bigger nearby city."
                        )
                    }
                }
                is CitySearchResult.Error -> {
                    _uiState.update { it.copy(envStatus = EnvStatus.ERROR, envError = result.message) }
                }
            }
        }
    }

    private suspend fun fetchConditions(lat: Double, lon: Double, labelOverride: String?) {
        _uiState.update { it.copy(envStatus = EnvStatus.LOADING, envError = null) }
        when (val result = weatherRepository.fetchConditions(lat, lon, labelOverride)) {
            is ConditionsResult.Success -> {
                val s = result.snapshot
                _uiState.update {
                    it.copy(
                        envStatus = EnvStatus.READY,
                        temperatureC = s.temperatureC,
                        humidityPct = s.humidityPct,
                        uvIndex = s.uvIndex,
                        aqi = s.airQualityAqi,
                        locationLabel = s.locationLabel,
                        updatedAtMillis = s.updatedAtMillis,
                        forecast = s.forecast,
                    )
                }
                if (labelOverride == null) {
                    val reverseLabel = weatherRepository.reverseGeocodeLabel(lat, lon)
                    if (!reverseLabel.isNullOrBlank()) {
                        _uiState.update { it.copy(locationLabel = reverseLabel) }
                    }
                }
                refreshMlScore()
            }
            is ConditionsResult.Error -> {
                _uiState.update { it.copy(envStatus = EnvStatus.ERROR, envError = result.message) }
            }
        }
    }

    // ---------------- Health Connect ----------------

    fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            Log.d(TAG, "checkHealthConnectAvailability: starting check")
            _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.Checking) }
            when (healthConnectManager.providerStatus()) {
                HealthConnectProviderStatus.AVAILABLE -> {
                    if (healthConnectManager.hasAllPermissions()) {
                        Log.d(TAG, "checkHealthConnectAvailability: available with permissions, syncing")
                        syncHealthConnect()
                    } else {
                        Log.d(TAG, "checkHealthConnectAvailability: available but needs permissions")
                        _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.NeedsPermission) }
                    }
                }
                HealthConnectProviderStatus.UPDATE_REQUIRED -> {
                    Log.w(TAG, "checkHealthConnectAvailability: update required")
                    _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.UpdateRequired) }
                }
                HealthConnectProviderStatus.NOT_INSTALLED -> {
                    Log.w(TAG, "checkHealthConnectAvailability: not installed")
                    _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.NotInstalled) }
                }
                HealthConnectProviderStatus.UNSUPPORTED -> {
                    Log.w(TAG, "checkHealthConnectAvailability: unsupported")
                    _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.NotSupported) }
                }
            }
        }
    }

    /** Opens Health Connect's app's permission screen for this app as a fallback. */
    fun openPermissionSettings() {
        val success = healthConnectManager.openPermissionSettingsSafely()
        if (!success) {
            Log.w(TAG, "openPermissionSettings: all intent approaches failed")
            _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.NeedsPermission) }
        }
    }

    /** Opens the Play Store so the user can install/update the Health Connect provider app. */
    fun installHealthConnect() {
        healthConnectManager.playStoreIntent()?.let { intent ->
            val ctx = getApplication<android.app.Application>()
            ctx.startActivity(intent)
        }
    }

    /** Called by the UI right after the system permission sheet returns. */
    fun onHealthConnectPermissionResult(grantedPermissions: Set<String>) {
        Log.d(TAG, "onHealthConnectPermissionResult: granted ${grantedPermissions.size} permissions: $grantedPermissions")
        if (grantedPermissions.containsAll(healthConnectPermissions)) {
            Log.d(TAG, "onHealthConnectPermissionResult: all required permissions granted, syncing")
            viewModelScope.launch { syncHealthConnect() }
        } else {
            // The standard permission sheet returned partial/no permissions. On sideloaded
            // (non-Play-verified) builds it can silently return empty without showing a UI;
            // offer to open Health Connect's own permission screen as a reliable fallback.
            Log.w(TAG, "onHealthConnectPermissionResult: incomplete permissions, needs manual grant")
            _uiState.update { it.copy(healthStatus = HealthConnectUiStatus.NeedsPermission) }
        }
    }

    fun syncHealthConnect() = syncHealthConnect(silent = false)

    fun syncHealthConnect(silent: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncingHealthConnect = true) }
            try {
                val snapshot = healthConnectManager.readTodaySnapshot()
                if (snapshot != null) {
                    _uiState.update {
                        it.copy(
                            healthStatus = HealthConnectUiStatus.Connected(isDemo = false),
                            wearable = snapshot,
                            hydrationLoggedMl = snapshot.hydrationMl,
                            syncingHealthConnect = false,
                        )
                    }
                    refreshMlScore()
                } else {
                    _uiState.update {
                        it.copy(
                            healthStatus = if (silent) it.healthStatus
                            else HealthConnectUiStatus.Error("Health Connect is unavailable on this device."),
                            syncingHealthConnect = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        healthStatus = if (silent) it.healthStatus
                        else HealthConnectUiStatus.Error("Couldn't read Health Connect data. Try syncing again."),
                        syncingHealthConnect = false,
                    )
                }
            }
        }
    }

    fun useDemoData() {
        val steps = Random.nextInt(800, 4000)
        val demo = com.synq.app.data.health.WearableSnapshot(
            steps = steps.toLong(),
            activityMinutes = Random.nextInt(5, 40),
            sleepHours = (Random.nextDouble(5.0, 8.2) * 4).let { Math.round(it) / 4.0 },
            restingHeartRate = Random.nextInt(60, 82).toDouble(),
            hydrationMl = (Random.nextInt(200, 900) / 50) * 50,
            syncedAtMillis = System.currentTimeMillis(),
        )
        _uiState.update {
            it.copy(
                healthStatus = HealthConnectUiStatus.Connected(isDemo = true),
                wearable = demo,
                hydrationLoggedMl = demo.hydrationMl,
                manualSleepHours = null,
            )
        }
        viewModelScope.launch { refreshMlScore() }
    }

    fun disconnectHealthConnect() {
        _uiState.update {
            it.copy(
                healthStatus = HealthConnectUiStatus.NeedsPermission,
                wearable = null,
                hydrationLoggedMl = 0,
                manualSleepHours = null,
            )
        }
        viewModelScope.launch { refreshMlScore() }
    }

    fun logHydration(ml: Int) {
        val state = _uiState.value
        val isDemo = (state.healthStatus as? HealthConnectUiStatus.Connected)?.isDemo == true
        if (isDemo) {
            _uiState.update { s ->
                val w = s.wearable?.copy(
                    hydrationMl = s.hydrationLoggedMl + ml,
                    syncedAtMillis = System.currentTimeMillis()
                )
                s.copy(wearable = w, hydrationLoggedMl = s.hydrationLoggedMl + ml)
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loggingHydration = true) }
            try {
                healthConnectManager.logHydration(ml)
                syncHealthConnect()
            } finally {
                _uiState.update { it.copy(loggingHydration = false) }
            }
        }
    }

    fun saveManualSleep(hours: Double) {
        _uiState.update { it.copy(manualSleepHours = hours) }
        viewModelScope.launch { refreshMlScore() }
    }

    fun toggleBaseline(on: Boolean) {
        _uiState.update { it.copy(baselineOn = on) }
        viewModelScope.launch { refreshMlScore() }
    }

    fun setBaselineSleep(hours: Double) {
        _uiState.update { it.copy(baselineSleepHours = hours) }
        viewModelScope.launch { refreshMlScore() }
    }

    fun setBaselineRhr(bpm: Double) {
        _uiState.update { it.copy(baselineRestingHeartRate = bpm) }
        viewModelScope.launch { refreshMlScore() }
    }

    // ---------------- Derived risk ----------------

    private fun buildInputs(state: SynqUiState = _uiState.value): RiskInputs = RiskInputs(
        temperatureC = state.temperatureC,
        humidityPct = state.humidityPct,
        uvIndex = state.uvIndex,
        airQualityAqi = state.aqi,
        sleepHours = if (state.isHealthConnected) state.effectiveSleepHours else null,
        restingHeartRate = if (state.isHealthConnected) state.wearable?.restingHeartRate else null,
        activityMinutes = if (state.isHealthConnected) state.wearable?.activityMinutes?.toDouble() else null,
        baselineSleepHours = if (state.isHealthConnected && state.baselineOn) state.baselineSleepHours else null,
        baselineRestingHeartRate = if (state.isHealthConnected && state.baselineOn) state.baselineRestingHeartRate else null,
    )

    /** Asks the Python ML backend for the risk score and caches it. On failure the
     *  previous score is kept (or nulls out) so the heuristic engine takes over. */
    private suspend fun refreshMlScore() {
        val score = MlRiskClient.predict(buildInputs())
        _uiState.update { it.copy(mlRiskScore = score) }
    }

    fun computeRisk(state: SynqUiState = _uiState.value): RiskResult {
        return RiskEngine.calculate(buildInputs(state), state.mlRiskScore)
    }

    fun hydrationTargetMl(state: SynqUiState = _uiState.value): Int? {
        return RiskEngine.hydrationTargetMl(
            temperatureC = state.temperatureC,
            humidityPct = state.humidityPct,
            activityMinutes = if (state.isHealthConnected) state.wearable?.activityMinutes?.toDouble() else null,
        )
    }
}
