package com.synq.app.data.health

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Volume
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

enum class HealthConnectProviderStatus {
    AVAILABLE,
    UPDATE_REQUIRED,
    NOT_INSTALLED,
    UNSUPPORTED,
}

/** A single day's worth of health signals used by the risk engine. */
data class WearableSnapshot(
    val steps: Long,
    val activityMinutes: Int,
    val sleepHours: Double?,
    val restingHeartRate: Double?,
    val hydrationMl: Int,
    val syncedAtMillis: Long,
)

/**
 * Reads and writes Health Connect data. Health Connect is the app's only "account":
 * the system permission sheet is the sign-in.
 */
class HealthConnectManager(private val application: Application) {

    private val healthConnectPackage = "com.google.android.apps.healthdata"

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
    )

    private val client: HealthConnectClient?
        get() = runCatching { HealthConnectClient.getOrCreate(application) }.getOrNull()

    fun requestPermissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    fun providerStatus(): HealthConnectProviderStatus {
        if (Build.VERSION.SDK_INT < 26) return HealthConnectProviderStatus.UNSUPPORTED
        return when (HealthConnectClient.getSdkStatus(application)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectProviderStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectProviderStatus.UPDATE_REQUIRED
            else ->
                if (isHealthConnectInstalled()) {
                    HealthConnectProviderStatus.UPDATE_REQUIRED
                } else {
                    HealthConnectProviderStatus.NOT_INSTALLED
                }
        }
    }

    suspend fun hasAllPermissions(): Boolean {
        val healthClient = client ?: return false
        return runCatching {
            healthClient.permissionController.getGrantedPermissions().containsAll(permissions)
        }.getOrDefault(false)
    }

    suspend fun readTodaySnapshot(): WearableSnapshot? {
        val healthClient = client ?: return null
        if (!hasAllPermissions()) return null

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val now = Instant.now()
        val startOfDay = today.atStartOfDay(zone).toInstant()
        val sleepStart = today.minusDays(1).atTime(LocalTime.of(18, 0)).atZone(zone).toInstant()

        val aggregate = healthClient.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL, HydrationRecord.VOLUME_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now),
            )
        )
        val steps = (aggregate[StepsRecord.COUNT_TOTAL] as? Long) ?: 0L
        val hydrationLiters = (aggregate[HydrationRecord.VOLUME_TOTAL] as? Double) ?: 0.0
        val hydrationMl = (hydrationLiters * 1000.0).toInt().coerceAtLeast(0)

        val exerciseRecords = healthClient.readRecords(
            ReadRecordsRequest(
                ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now),
            )
        ).records
        val activityMinutes = exerciseRecords.sumOf { record ->
            val millis = record.endTime.toEpochMilli() - record.startTime.toEpochMilli()
            (millis / 60_000L).toInt().coerceAtLeast(0)
        }

        val sleepRecords = healthClient.readRecords(
            ReadRecordsRequest(
                SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(sleepStart, now),
            )
        ).records
        val sleepMillis = sleepRecords.sumOf { record ->
            val start = maxOf(record.startTime.toEpochMilli(), sleepStart.toEpochMilli())
            val end = minOf(record.endTime.toEpochMilli(), now.toEpochMilli())
            (end - start).coerceAtLeast(0L)
        }
        val sleepHours = if (sleepMillis > 0L) sleepMillis / 3_600_000.0 else null

        val restingHeartRate = try {
            val records = healthClient.readRecords(
                ReadRecordsRequest(
                    RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        today.minusDays(1).atStartOfDay(zone).toInstant(),
                        now,
                    ),
                )
            ).records
            records.maxByOrNull { it.time }?.beatsPerMinute?.toDouble()
        } catch (e: Exception) {
            null
        } ?: try {
            val records = healthClient.readRecords(
                ReadRecordsRequest(
                    HeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        now.minusSeconds(6 * 60 * 60),
                        now,
                    ),
                )
            ).records
            records.flatMap { it.samples }.map { it.beatsPerMinute.toDouble() }
                .takeIf { it.isNotEmpty() }?.average()
        } catch (e: Exception) {
            null
        }

        return WearableSnapshot(
            steps = steps,
            activityMinutes = activityMinutes,
            sleepHours = sleepHours,
            restingHeartRate = restingHeartRate,
            hydrationMl = hydrationMl,
            syncedAtMillis = System.currentTimeMillis(),
        )
    }

    suspend fun logHydration(ml: Int) {
        val healthClient = client ?: return
        if (ml <= 0) return
        val now = Instant.now()
        val offset = ZoneOffset.systemDefault().rules.getOffset(now)
        val record = HydrationRecord(
            startTime = now,
            startZoneOffset = offset,
            endTime = now,
            endZoneOffset = offset,
            volume = Volume.liters(ml / 1000.0),
            metadata = Metadata.manualEntry(),
        )
        healthClient.insertRecords(listOf(record))
    }

    /** Opens the Health Connect app's per-app permission screen. Returns true if launched. */
    fun openPermissionSettingsSafely(): Boolean {
        val candidates = buildList {
            add(Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS"))
            add(Intent("android.health.connect.action.MANAGE_HEALTH_PERMISSIONS"))
            add(Intent("androidx.health.ACTION_HEALTH_CONNECT_SETTINGS").setPackage(healthConnectPackage))
            add(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$healthConnectPackage"),
                )
            )
            add(Intent(Settings.ACTION_SETTINGS))
        }
        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resolvable = intent.resolveActivity(application.packageManager) != null
            if (!resolvable) continue
            return runCatching {
                application.startActivity(intent)
                true
            }.getOrDefault(false)
        }
        return false
    }

    fun playStoreIntent(): Intent? {
        val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$healthConnectPackage"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (market.resolveActivity(application.packageManager) != null) return market
        return Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$healthConnectPackage"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun isHealthConnectInstalled(): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.packageManager.getPackageInfo(
                healthConnectPackage,
                PackageManager.PackageInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            application.packageManager.getPackageInfo(healthConnectPackage, 0)
        }
        true
    }.getOrDefault(false)
}
