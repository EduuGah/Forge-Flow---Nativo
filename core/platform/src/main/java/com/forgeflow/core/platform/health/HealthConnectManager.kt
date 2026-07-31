package com.forgeflow.core.platform.health

import androidx.activity.result.contract.ActivityResultContract
import com.forgeflow.core.model.HealthConnectDataType
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails
import java.time.Instant

enum class HealthConnectAvailability {
    AVAILABLE,
    UPDATE_REQUIRED,
    UNAVAILABLE,
}

data class HealthConnectStatus(
    val availability: HealthConnectAvailability,
    val hasPermissions: Boolean = false,
)

data class HealthConnectSyncResult(
    val syncedCount: Int,
    val failedCount: Int,
)

data class HealthConnectWeightSample(
    val recordId: String,
    val kilograms: Double,
    val measuredAt: Instant,
    val sourcePackage: String,
)

data class HealthConnectReadResult(
    val startTime: Instant,
    val endTime: Instant,
    val steps: Long? = null,
    val distanceMeters: Double? = null,
    val caloriesKilocalories: Double? = null,
    val averageHeartRate: Long? = null,
    val exerciseSessionCount: Int? = null,
    val exerciseDurationMinutes: Long? = null,
    val sleepMinutes: Long? = null,
    val weightSamples: List<HealthConnectWeightSample> = emptyList(),
)

interface HealthConnectManager {
    val writePermissions: Set<String>

    fun permissionsFor(dataTypes: Set<HealthConnectDataType>): Set<String>

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>>

    suspend fun status(requiredPermissions: Set<String> = writePermissions): HealthConnectStatus

    suspend fun readData(
        dataTypes: Set<HealthConnectDataType>,
        startTime: Instant,
        endTime: Instant,
    ): HealthConnectReadResult?

    suspend fun readDataOrigins(
        dataTypes: Set<HealthConnectDataType>,
        startTime: Instant,
        endTime: Instant,
    ): Set<String>

    suspend fun syncWorkout(
        workout: WorkoutDetails,
        weightUnit: WeightUnit,
    ): HealthConnectSyncResult

    suspend fun syncWorkouts(
        workouts: List<WorkoutDetails>,
        weightUnit: WeightUnit,
    ): HealthConnectSyncResult

    suspend fun deleteWorkout(workoutId: String): Boolean

    fun openHealthConnect()

    fun openInstallOrUpdate()
}
