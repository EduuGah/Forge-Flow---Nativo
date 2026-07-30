package com.forgeflow.core.platform.health

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import com.forgeflow.core.model.WeightUnit
import com.forgeflow.core.model.WorkoutDetails
import com.forgeflow.core.model.WorkoutSessionStatus
import com.forgeflow.core.model.gramsIn
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZoneId
import javax.inject.Inject

class AndroidHealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthConnectManager {
    override val requiredPermissions: Set<String> = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
    )

    override fun createPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    override suspend fun status(): HealthConnectStatus {
        val availability = availability()
        if (availability != HealthConnectAvailability.AVAILABLE) {
            return HealthConnectStatus(availability = availability)
        }
        val granted = runCatching {
            client().permissionController.getGrantedPermissions()
        }.getOrDefault(emptySet())
        return HealthConnectStatus(
            availability = availability,
            hasPermissions = granted.containsAll(requiredPermissions),
        )
    }

    override suspend fun syncWorkout(
        workout: WorkoutDetails,
        weightUnit: WeightUnit,
    ): HealthConnectSyncResult = syncWorkouts(listOf(workout), weightUnit)

    override suspend fun syncWorkouts(
        workouts: List<WorkoutDetails>,
        weightUnit: WeightUnit,
    ): HealthConnectSyncResult {
        val currentStatus = status()
        if (
            currentStatus.availability != HealthConnectAvailability.AVAILABLE ||
            !currentStatus.hasPermissions
        ) {
            return HealthConnectSyncResult(syncedCount = 0, failedCount = workouts.size)
        }
        val records = workouts
            .distinctBy { it.session.id.value }
            .mapNotNull { it.asExerciseSessionRecord(weightUnit) }
        var syncedCount = 0
        var failedCount = workouts.size - records.size
        records.forEach { record ->
            runCatching {
                client().insertRecords(listOf(record))
            }.onSuccess {
                syncedCount += 1
            }.onFailure {
                failedCount += 1
            }
        }
        return HealthConnectSyncResult(
            syncedCount = syncedCount,
            failedCount = failedCount,
        )
    }

    override suspend fun deleteWorkout(workoutId: String): Boolean {
        val currentStatus = status()
        if (
            currentStatus.availability != HealthConnectAvailability.AVAILABLE ||
            !currentStatus.hasPermissions
        ) {
            return false
        }
        return runCatching {
            client().deleteRecords(
                recordType = ExerciseSessionRecord::class,
                recordIdsList = emptyList(),
                clientRecordIdsList = listOf(clientRecordId(workoutId)),
            )
        }.isSuccess
    }

    override fun openHealthConnect() {
        val intent = Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
            .onFailure { openInstallOrUpdate() }
    }

    override fun openInstallOrUpdate() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            "market://details?id=$HEALTH_CONNECT_PACKAGE".toUri(),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(marketIntent)
        } catch (_: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=$HEALTH_CONNECT_PACKAGE".toUri(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
    }

    private fun availability(): HealthConnectAvailability {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return HealthConnectAvailability.UNAVAILABLE
        }
        return when (HealthConnectClient.getSdkStatus(context)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.UNAVAILABLE
        }
    }

    private fun client(): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    private fun WorkoutDetails.asExerciseSessionRecord(
        weightUnit: WeightUnit,
    ): ExerciseSessionRecord? {
        val endTime = session.finishedAt ?: return null
        if (
            session.status != WorkoutSessionStatus.COMPLETED ||
            completedSetCount == 0 ||
            !endTime.isAfter(session.startedAt)
        ) {
            return null
        }
        val zoneRules = ZoneId.systemDefault().rules
        val exerciseNames = exercises
            .filter { details -> details.sets.any { it.isCompleted } }
            .map { it.sessionExercise.exerciseNameSnapshot }
            .distinct()
        val unitLabel = when (weightUnit) {
            WeightUnit.KILOGRAM -> "kg"
            WeightUnit.POUND -> "lb"
        }
        val volume = totalVolumeGrams.gramsIn(weightUnit).toCleanString()
        val summary = buildString {
            append("$completedSetCount séries concluídas")
            append(" • volume total: $volume $unitLabel")
            if (exerciseNames.isNotEmpty()) {
                append("\nExercícios: ")
                append(exerciseNames.joinToString())
            }
            session.location?.label?.let { label ->
                append("\nLocal: $label")
            }
        }
        return ExerciseSessionRecord(
            startTime = session.startedAt,
            startZoneOffset = zoneRules.getOffset(session.startedAt),
            endTime = endTime,
            endZoneOffset = zoneRules.getOffset(endTime),
            metadata = Metadata.activelyRecorded(
                device = Device(
                    type = Device.TYPE_PHONE,
                    manufacturer = Build.MANUFACTURER,
                    model = Build.MODEL,
                ),
                clientRecordId = clientRecordId(session.id.value),
                clientRecordVersion = session.updatedAt.toEpochMilli().coerceAtLeast(0),
            ),
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
            title = session.name,
            notes = summary,
        )
    }

    private fun Double.toCleanString(): String =
        BigDecimal.valueOf(this)
            .setScale(1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()

    private companion object {
        const val HEALTH_CONNECT_PACKAGE = "com.google.android.apps.healthdata"

        fun clientRecordId(workoutId: String): String = "forgeflow-workout-$workoutId"
    }
}
