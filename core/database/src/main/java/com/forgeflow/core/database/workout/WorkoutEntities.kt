package com.forgeflow.core.database.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.forgeflow.core.database.exercise.ExerciseEntity
import com.forgeflow.core.database.routine.RoutineEntity

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["routine_id"]),
        Index(value = ["status"]),
        Index(value = ["finished_at_epoch_millis"]),
    ],
)
data class WorkoutSessionEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "routine_id")
    val routineId: String?,
    val name: String,
    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,
    @ColumnInfo(name = "finished_at_epoch_millis")
    val finishedAtEpochMillis: Long?,
    val status: String,
    val notes: String,
    @ColumnInfo(name = "location_latitude")
    val locationLatitude: Double? = null,
    @ColumnInfo(name = "location_longitude")
    val locationLongitude: Double? = null,
    @ColumnInfo(name = "location_accuracy_meters")
    val locationAccuracyMeters: Float? = null,
    @ColumnInfo(name = "location_captured_at_epoch_millis")
    val locationCapturedAtEpochMillis: Long? = null,
    @ColumnInfo(name = "location_label")
    val locationLabel: String? = null,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "workout_session_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["exercise_id"]),
        Index(value = ["session_id", "position"], unique = true),
    ],
)
data class WorkoutSessionExerciseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: String?,
    @ColumnInfo(name = "exercise_name_snapshot")
    val exerciseNameSnapshot: String,
    @ColumnInfo(name = "muscle_group_snapshot")
    val muscleGroupSnapshot: String,
    @ColumnInfo(name = "media_uri_snapshot")
    val mediaUriSnapshot: String? = null,
    @ColumnInfo(name = "media_type_snapshot")
    val mediaTypeSnapshot: String? = null,
    @ColumnInfo(name = "media_thumbnail_uri_snapshot")
    val mediaThumbnailUriSnapshot: String? = null,
    val position: Int,
    val notes: String,
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["session_exercise_id"]),
        Index(value = ["session_exercise_id", "position"], unique = true),
    ],
)
data class WorkoutSetEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_exercise_id")
    val sessionExerciseId: String,
    val position: Int,
    @ColumnInfo(name = "set_type")
    val setType: String,
    @ColumnInfo(name = "weight_grams")
    val weightGrams: Long,
    val repetitions: Int,
    val rpe: Int?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "completed_at_epoch_millis")
    val completedAtEpochMillis: Long?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
