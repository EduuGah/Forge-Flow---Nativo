package com.forgeflow.core.platform.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.forgeflow.core.model.WorkoutLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class AndroidCurrentLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : CurrentLocationProvider {
    private val locationManager: LocationManager
        get() = context.getSystemService(LocationManager::class.java)

    @SuppressLint("MissingPermission")
    override suspend fun captureCurrentLocation(): WorkoutLocation? {
        if (!hasLocationPermission()) return null
        val provider = availableProviders().firstOrNull()
            ?: return lastKnownLocation()?.asWorkoutLocation()
        val current = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            requestCurrentLocation(provider)
        }
        return (current ?: lastKnownLocation())?.asWorkoutLocation()
    }

    private fun availableProviders(): List<String> = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
    ).filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(provider: String): Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()
            continuation.invokeOnCancellation { cancellationSignal.cancel() }
            runCatching {
                LocationManagerCompat.getCurrentLocation(
                    locationManager,
                    provider,
                    cancellationSignal,
                    ContextCompat.getMainExecutor(context),
                ) { location ->
                    if (continuation.isActive) continuation.resume(location)
                }
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? = availableProviders()
        .mapNotNull { provider ->
            runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
        }
        .maxByOrNull(Location::getTime)

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun Location.asWorkoutLocation(): WorkoutLocation = WorkoutLocation(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else null,
        capturedAt = Instant.ofEpochMilli(time.takeIf { it > 0 } ?: System.currentTimeMillis()),
    )

    private companion object {
        const val LOCATION_TIMEOUT_MILLIS = 8_000L
    }
}
