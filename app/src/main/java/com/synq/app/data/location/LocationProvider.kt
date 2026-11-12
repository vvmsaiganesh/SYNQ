package com.synq.app.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

sealed class LocationResult {
    data class Success(val lat: Double, val lon: Double) : LocationResult()
    object PermissionDenied : LocationResult()
    object Unavailable : LocationResult()
}

/** Thin coroutine wrapper around the Play Services fused location provider. */
class LocationProvider(private val application: Application) {

    private val client = LocationServices.getFusedLocationProviderClient(application)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResult {
        val fine = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            application, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return LocationResult.PermissionDenied

        return try {
            // getCurrentLocation can wait indefinitely for a fresh fix (especially on a
            // cold emulator), so cap it and fall back to the last known position.
            val fresh = withTimeoutOrNull(5_000) {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            }
            val location = fresh ?: withTimeoutOrNull(3_000) { client.lastLocation.await() }
            if (location != null) {
                LocationResult.Success(location.latitude, location.longitude)
            } else {
                LocationResult.Unavailable
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        } catch (e: Exception) {
            LocationResult.Unavailable
        }
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T? =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { value -> if (continuation.isActive) continuation.resume(value) }
        addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
        addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
    }
