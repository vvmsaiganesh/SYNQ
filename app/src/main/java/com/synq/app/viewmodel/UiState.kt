package com.synq.app.viewmodel

import com.synq.app.data.health.WearableSnapshot
import com.synq.app.data.weather.ForecastCard

enum class EnvStatus { IDLE, LOCATING, LOADING, READY, ERROR }

sealed class HealthConnectUiStatus {
    object Checking : HealthConnectUiStatus()
    object NotInstalled : HealthConnectUiStatus()
    object UpdateRequired : HealthConnectUiStatus()
    object NotSupported : HealthConnectUiStatus()
    object NeedsPermission : HealthConnectUiStatus()
    data class Connected(val isDemo: Boolean = false) : HealthConnectUiStatus()
    data class Error(val message: String) : HealthConnectUiStatus()
}

data class SynqUiState(
    // Environment / weather
    val envStatus: EnvStatus = EnvStatus.IDLE,
    val temperatureC: Double? = null,
    val humidityPct: Double? = null,
    val uvIndex: Double? = null,
    val aqi: Double? = null,
    val locationLabel: String? = null,
    val updatedAtMillis: Long? = null,
    val envError: String? = null,
    val forecast: List<ForecastCard> = emptyList(),
    val citySearchQuery: String = "",

    // Health Connect / wearable
    val healthStatus: HealthConnectUiStatus = HealthConnectUiStatus.Checking,
    val wearable: WearableSnapshot? = null,
    val manualSleepHours: Double? = null,
    val hydrationLoggedMl: Int = 0,
    val loggingHydration: Boolean = false,
    val syncingHealthConnect: Boolean = false,

    // Personal baseline
    val baselineOn: Boolean = false,
    val baselineSleepHours: Double = 7.5,
    val baselineRestingHeartRate: Double = 65.0,

    // ML risk score from the Python backend (null until fetched / when unreachable)
    val mlRiskScore: Double? = null,
) {
    val effectiveSleepHours: Double?
        get() = manualSleepHours ?: wearable?.sleepHours

    val isHealthConnected: Boolean
        get() = healthStatus is HealthConnectUiStatus.Connected
}
