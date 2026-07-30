package com.forgeflow.core.platform.health

import androidx.activity.result.contract.ActivityResultContract
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails

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

interface HealthConnectManager {
    val requiredPermissions: Set<String>

    fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>>

    suspend fun status(): HealthConnectStatus

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
