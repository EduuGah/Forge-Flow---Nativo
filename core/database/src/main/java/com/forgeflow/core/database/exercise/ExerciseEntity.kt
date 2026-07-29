package com.forgeflow.core.database.exercise

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["primary_muscle_group"]),
    ],
)
data class ExerciseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "primary_muscle_group")
    val primaryMuscleGroup: String,
    @ColumnInfo(name = "secondary_muscle_groups")
    val secondaryMuscleGroups: String,
    val equipment: String,
    val instructions: String,
    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
