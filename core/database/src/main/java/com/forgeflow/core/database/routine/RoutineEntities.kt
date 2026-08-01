package com.forgeflow.core.database.routine

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.forgeflow.core.database.exercise.ExerciseEntity

@Entity(
    tableName = "routines",
    indices = [
        Index(value = ["name"]),
        Index(value = ["folder_id"]),
    ],
)
data class RoutineEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,
    val name: String,
    val description: String,
    val position: Int = 0,
    @ColumnInfo(name = "compare_history_within_folder")
    val compareHistoryWithinFolder: Boolean = false,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
    @ColumnInfo(name = "archived_at_epoch_millis")
    val archivedAtEpochMillis: Long?,
)

@Entity(
    tableName = "routine_folders",
    indices = [Index(value = ["position"])],
)
data class RoutineFolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val position: Int,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = RoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
        ),
    ],
    indices = [
        Index(value = ["routine_id"]),
        Index(value = ["exercise_id"]),
        Index(value = ["routine_id", "position"], unique = true),
    ],
)
data class RoutineExerciseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "routine_id")
    val routineId: String,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,
    val position: Int,
    val notes: String,
    @ColumnInfo(name = "default_rest_seconds")
    val defaultRestSeconds: Int,
    @ColumnInfo(name = "planned_repetitions_minimum")
    val plannedRepetitionsMinimum: Int?,
    @ColumnInfo(name = "planned_repetitions_maximum")
    val plannedRepetitionsMaximum: Int?,
    @ColumnInfo(name = "planned_sets")
    val plannedSets: Int,
    @ColumnInfo(name = "planned_warm_up_sets")
    val plannedWarmUpSets: Int = 0,
)
